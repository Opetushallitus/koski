package fi.oph.koski.opiskeluoikeus

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSession, RequiresVirkailijaOrPalvelukäyttäjä}
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

trait OpiskeluoikeusQueries extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with GlobalExecutionContext with ObservableSupport with ContentEncodingSupport with Pagination {
  def application: KoskiApplication
  def query = OpiskeluoikeusQueryContext(request)(koskiSession, application).query(params, paginationSettings) match {
    case Right(observable) => observable
    case Left(status) => haltWithStatus(status)
  }
}

/**
  *  Operating context for data streaming in queries. Operates outside the lecixal scope of OpiskeluoikeusQueries to ensure that none of the
  *  Scalatra threadlocals are used.
  */
case class OpiskeluoikeusQueryContext(request: HttpServletRequest)(implicit koskiSession: KoskiSession, application: KoskiApplication) extends Logging {
  def query(params: Map[String, String], paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])]] = {
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
    streamingQueryGroupedByOid(filters, paginationSettings)
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]): Observable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluoikeusRow])] = streamingQueryGroupedByOid(filters, paginationSettings)
    oikeudetPerOppijaOid.tumblingBuffer(10).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluoikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, TäydellisetHenkilötiedot] = application.henkilöRepository.findByOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val oppijat: Iterable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
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

  private def streamingQueryGroupedByOid(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]): Observable[(Oid, List[(OpiskeluoikeusRow)])] = {
    val rows = application.opiskeluoikeusQueryRepository.opiskeluoikeusQuery(filters, Some(Ascending("oppijaOid")), paginationSettings)

    val groupedByPerson: Observable[List[(OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])]] = rows
      .tumblingBuffer(rows.map(_._1.oppijaOid).distinctUntilChanged.drop(1))
      .map(_.toList)

    groupedByPerson.flatMap {
      case oikeudet@(firstRow :: _) =>
        val oppijaOid = firstRow._1.oppijaOid
        assert(oikeudet.map(_._1.oppijaOid).toSet == Set(oppijaOid), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + oppijaOid + ": " + oikeudet)
        Observable.just((oppijaOid, oikeudet.toList.map(_._1)))
      case _ =>
        Observable.empty
    }
  }
}

object OpiskeluoikeusQueryContext {
  def queryForAuditLog(params: Map[String, String]) =
    params.toList.sortBy(_._1).map { case (p,v) => p + "=" + v }.mkString("&")
}
