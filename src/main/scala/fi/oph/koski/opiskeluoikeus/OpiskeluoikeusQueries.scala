package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{HasKoskiSession, KoskiSession}
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.servlet.{ApiServlet, ObservableSupport}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{Pagination, PaginationSettings}
import javax.servlet.http.HttpServletRequest
import org.scalatra._
import rx.lang.scala.Observable

trait OpiskeluoikeusQueries extends ApiServlet with Logging with GlobalExecutionContext with ObservableSupport with ContentEncodingSupport with Pagination with HasKoskiSession {
  def application: KoskiApplication

  def queryOpiskeluoikeudet: Either[HttpStatus, Observable[(LaajatOppijaHenkilöTiedot, List[KoskeenTallennettavaOpiskeluoikeus])]] =
    OpiskeluoikeusQueryContext(request)(koskiSession, application).queryOpiskeluoikeudet(multiParams, paginationSettings)
}

/**
  *  Operating context for data streaming in queries. Operates outside the lecixal scope of OpiskeluoikeusQueries to ensure that none of the
  *  Scalatra threadlocals are used.
  */
case class OpiskeluoikeusQueryContext(request: HttpServletRequest)(implicit koskiSession: KoskiSession, application: KoskiApplication) extends Logging {
  def queryWithoutHenkilötiedotRaw(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings], queryForAuditLog: String): Observable[(Oid, List[Opiskeluoikeus])] = {
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> queryForAuditLog)))
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings).map { oppija =>
      (oppija.henkilö.oid, oppija.opiskeluoikeudet)
    }
  }

  def queryOpiskeluoikeudet(params: MultiParams, paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[(LaajatOppijaHenkilöTiedot, List[KoskeenTallennettavaOpiskeluoikeus])]] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryFilter.parse(params)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession).map { filters =>
      AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(params))))
      query(filters, paginationSettings)
    }
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]): Observable[(LaajatOppijaHenkilöTiedot, List[KoskeenTallennettavaOpiskeluoikeus])] = {
    val oikeudetPerOppijaOid: Observable[QueryOppija] = OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings)

    oikeudetPerOppijaOid.tumblingBuffer(10).flatMap { ops =>
      val henkilöt: Map[Oid, LaajatOppijaHenkilöTiedot] = application.opintopolkuHenkilöFacade.findMasterOppijat(ops.toList.map(_.henkilö.oid))

      val oppijat: List[(LaajatOppijaHenkilöTiedot, List[KoskeenTallennettavaOpiskeluoikeus])] = ops.toList.flatMap { oppija =>
        henkilöt.get(oppija.henkilö.oid) match {
          case Some(henkilö) =>
            Some((henkilö.copy(linkitetytOidit = oppija.henkilö.linkitetytOidit), oppija.opiskeluoikeudet))
          case None =>
            logger(koskiSession).warn("Oppijaa " + oppija.henkilö.oid + " ei löydy henkilöpalvelusta")
            None
        }
      }
      Observable.from(oppijat)
    }
  }
}

object OpiskeluoikeusQueryContext {
  def queryForAuditLog(params: MultiParams) =
    params.toList.sortBy(_._1).map { case (p,values) => values.map(v => p + "=" + v).mkString("&") }.mkString("&")

  def streamingQueryGroupedByOid(application: KoskiApplication, filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings])(implicit koskiSession: KoskiSession): Observable[QueryOppija] = {
    application.opiskeluoikeusQueryRepository.opiskeluoikeusQuery(filters, paginationSettings)
  }
}
