package fi.oph.koski.opiskeluoikeus

import javax.servlet.http.HttpServletRequest
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.SensitiveDataFilter
import fi.oph.koski.koskiuser.{HasKoskiSession, KoskiSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.servlet.{ApiServlet, ObservableSupport}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{Pagination, PaginationSettings}
import org.scalatra._
import rx.lang.scala.Observable

trait OpiskeluoikeusQueries extends ApiServlet with Logging with GlobalExecutionContext with ObservableSupport with ContentEncodingSupport with Pagination with HasKoskiSession {
  def application: KoskiApplication
  def performOpiskeluoikeusQuery: Observable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])] = OpiskeluoikeusQueryContext(request)(koskiSession, application).query(params, paginationSettings) match {
    case Right(observable) =>
      observable.map(x => (application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(x._1), x._2))
    case Left(status) => haltWithStatus(status)
  }

  def queryAndStreamOpiskeluoikeudet = {
    val serialize = SensitiveDataFilter(koskiSession).rowSerializer
    streamResponse(performOpiskeluoikeusQuery.map(serialize), koskiSession)
  }
}

/**
  *  Operating context for data streaming in queries. Operates outside the lecixal scope of OpiskeluoikeusQueries to ensure that none of the
  *  Scalatra threadlocals are used.
  */
case class OpiskeluoikeusQueryContext(request: HttpServletRequest)(implicit koskiSession: KoskiSession, application: KoskiApplication) extends Logging {
  def query(params: Map[String, String], paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[(OppijaHenkilö, List[OpiskeluoikeusRow])]] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryFilter.parse(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
      case Right(filters) =>
        AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(params))))
        Right(query(filters, paginationSettings))
      case Left(status) =>
        Left(status)
    }
  }

  def queryWithoutHenkilötiedotRaw(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings], queryForAuditLog: String): Observable[(Oid, List[OpiskeluoikeusRow])] = {
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> queryForAuditLog)))
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings)
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]): Observable[(OppijaHenkilö, List[OpiskeluoikeusRow])] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluoikeusRow])] = OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings)
    oikeudetPerOppijaOid.tumblingBuffer(10).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluoikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, OppijaHenkilö] = application.henkilöRepository.findByOidsNoSlaveOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val oppijat: Iterable[(OppijaHenkilö, List[OpiskeluoikeusRow])] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
          henkilöt.get(oid) match {
            case Some(henkilö) =>
              Some((henkilö, opiskeluOikeudet))
            case None =>
              logger(koskiSession).warn("Oppijaa " + oid + " ei löydy henkilöpalvelusta")
              None
          }
        }
        Observable.from(oppijat)
    }
  }
}

object OpiskeluoikeusQueryContext {
  def queryForAuditLog(params: Map[String, String]) =
    params.toList.sortBy(_._1).map { case (p,v) => p + "=" + v }.mkString("&")

  def streamingQueryGroupedByOid(application: KoskiApplication, filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings])(implicit koskiSession: KoskiSession): Observable[(Oid, List[(OpiskeluoikeusRow)])] = {
    val rows = application.opiskeluoikeusQueryRepository.opiskeluoikeusQuery(filters, Some(Ascending("oppijaOid")), paginationSettings)

    val groupedByPerson: Observable[List[(OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])]] = rows
      .tumblingBuffer(rows.map(masterOid).distinctUntilChanged.drop(1))
      .map(_.toList)

    groupedByPerson.flatMap {
      case oikeudet@(row :: _) =>
        val oppijanOidit = oikeudet.flatMap { case (_, h, m) => h.oid :: m.map(_.oid).toList }.toSet
        assert(oikeudet.map(_._1.oppijaOid).toSet.subsetOf(oppijanOidit), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + oppijanOidit + ": " + oikeudet.map(_._1.oppijaOid).toSet)
        Observable.just((masterOid(row), oikeudet.map(_._1)))
      case _ =>
        Observable.empty
    }
  }

  private def masterOid(row: (OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])): String = {
    val (_, henkilö, masterHenkilö) = row
    masterHenkilö.map(_.oid).getOrElse(henkilö.oid)
  }
}
