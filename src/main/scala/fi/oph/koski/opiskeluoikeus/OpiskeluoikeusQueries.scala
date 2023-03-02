package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{HenkilöRow, KoskiOpiskeluoikeusRow}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{HasKoskiSpecificSession, KoskiSpecificSession}
import fi.oph.koski.log.KoskiAuditLogMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, ObservableSupport}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{Pagination, PaginationSettings, QueryPagination}
import javax.servlet.http.HttpServletRequest
import org.scalatra._
import rx.lang.scala.Observable

trait OpiskeluoikeusQueries extends KoskiSpecificApiServlet with Logging with ObservableSupport with ContentEncodingSupport with Pagination with HasKoskiSpecificSession {
  def application: KoskiApplication

  def performOpiskeluoikeudetQueryLaajoillaHenkilötiedoilla: Either[HttpStatus, Observable[(LaajatOppijaHenkilöTiedot, List[KoskiOpiskeluoikeusRow])]] =
    OpiskeluoikeusQueryContext(request)(session, application).queryLaajoillaHenkilöTiedoilla(multiParams, paginationSettings)
}

/**
  *  Operating context for data streaming in queries. Operates outside the lecixal scope of OpiskeluoikeusQueries to ensure that none of the
  *  Scalatra threadlocals are used.
  */
case class OpiskeluoikeusQueryContext(request: HttpServletRequest)(implicit koskiSession: KoskiSpecificSession, application: KoskiApplication) extends Logging {
  def queryWithoutHenkilötiedotRaw(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings], queryForAuditLog: String): Observable[(Oid, List[KoskiOpiskeluoikeusRow])] = {
    AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> queryForAuditLog)))
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings).map { case (oppijaHenkilö, opiskeluoikeudet) =>
      (oppijaHenkilö.oid, opiskeluoikeudet)
    }
  }

  def queryLaajoillaHenkilöTiedoilla(params: MultiParams, paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[(LaajatOppijaHenkilöTiedot, List[KoskiOpiskeluoikeusRow])]] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryFilter.parse(params)(application.koodistoViitePalvelu, application.organisaatioService, koskiSession).map { filters =>
      AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(params))))
      query(filters, paginationSettings)
    }
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]): Observable[(LaajatOppijaHenkilöTiedot, List[KoskiOpiskeluoikeusRow])] = {
    val oikeudetPerOppijaOid: Observable[(QueryOppijaHenkilö, List[KoskiOpiskeluoikeusRow])] = OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings)

    oikeudetPerOppijaOid.tumblingBuffer(10).flatMap { oppijatJaOidit: Seq[(QueryOppijaHenkilö, List[KoskiOpiskeluoikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1.oid).toList
        val henkilöt: Map[Oid, LaajatOppijaHenkilöTiedot] = application.opintopolkuHenkilöFacade.findMasterOppijat(oids)

        val oppijat: Iterable[(LaajatOppijaHenkilöTiedot, List[KoskiOpiskeluoikeusRow])] = oppijatJaOidit.flatMap { case (oppijaHenkilö, opiskeluOikeudet) =>
          henkilöt.get(oppijaHenkilö.oid) match {
            case Some(henkilö) =>
              Some((henkilö.copy(linkitetytOidit = oppijaHenkilö.linkitetytOidit), opiskeluOikeudet))
            case None =>
              logger(koskiSession).warn("Oppijaa " + oppijaHenkilö.oid + " ei löydy henkilöpalvelusta")
              None
          }
        }
        Observable.from(oppijat)
    }
  }
}

object OpiskeluoikeusQueryContext {
  val pagination = QueryPagination(lookaheadBufferSize = 50)
  def queryForAuditLog(params: MultiParams) =
    params.toList.sortBy(_._1).map { case (p,values) => values.map(v => p + "=" + v).mkString("&") }.mkString("&")


  def streamingQueryGroupedByOid(application: KoskiApplication, filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings])(implicit koskiSession: KoskiSpecificSession): Observable[(QueryOppijaHenkilö, List[(KoskiOpiskeluoikeusRow)])] = {
    var streamedOpiskeluoikeusCount: Int = 0
    def opiskeluoikeusCountWithinPageSize(row: (QueryOppijaHenkilö, List[KoskiOpiskeluoikeusRow])): Boolean = paginationSettings match {
      case None => true
      case Some(settings) if settings.size > streamedOpiskeluoikeusCount =>
        streamedOpiskeluoikeusCount = streamedOpiskeluoikeusCount + row._2.length
        true
      case _ => false
    }

    val groupedByPerson: Observable[List[(KoskiOpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])]] = application
      .opiskeluoikeusQueryRepository.opiskeluoikeusQuery(filters, Some(Ascending("oppijaOid")), paginationSettings, pagination)
      .publish(groupByPerson)

    val stream: Observable[(QueryOppijaHenkilö, List[KoskiOpiskeluoikeusRow])] = groupedByPerson.flatMap {
      case oikeudet@(row :: _) =>
        val oppijanOidit = oikeudet.flatMap { case (_, h, m) => h.oid :: m.map(_.oid).toList }.toSet
        assert(oikeudet.map(_._1.oppijaOid).toSet.subsetOf(oppijanOidit), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + oppijanOidit + ": " + oikeudet.map(_._1.oppijaOid).toSet)
        Observable.just((QueryOppijaHenkilö(masterOid(row), oppijanOidit), oikeudet.map(_._1)))
      case _ =>
        Observable.empty
    }

    stream.takeWhile(opiskeluoikeusCountWithinPageSize)
  }

  private def masterOid(row: (KoskiOpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])): String = {
    val (_, henkilö, masterHenkilö) = row
    masterHenkilö.map(_.oid).getOrElse(henkilö.oid)
  }

  private type OpiskeluoikeusQueryRows = (KoskiOpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])
  private val groupByPerson: Observable[OpiskeluoikeusQueryRows] => Observable[List[OpiskeluoikeusQueryRows]] = {
    rows => rows.tumblingBuffer(rows.map(masterOid).distinctUntilChanged.drop(1)).map(_.toList)
  }
}

object QueryOppijaHenkilö {
  def apply(masterOid: Oid, kaikkiOidit: Set[Oid]): QueryOppijaHenkilö = QueryOppijaHenkilö(masterOid, kaikkiOidit.filterNot(_ == masterOid).toList.sorted)
}
case class QueryOppijaHenkilö(oid: Oid, linkitetytOidit: List[Oid])
