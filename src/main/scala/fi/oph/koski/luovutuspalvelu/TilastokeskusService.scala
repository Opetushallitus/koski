package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.util.PaginationSettings
import fi.oph.koski.util.SortOrder.Ascending
import javax.servlet.http.HttpServletRequest
import org.scalatra._
import rx.lang.scala.Observable

// Taken from OpiskeluoikeusQueries.scala
// OpiskeluoikeusQueries groups study entitlements for the same student under that student, so the data is e.g.
// {
//    henkilö: ...
//    oikeudet: [{}, {}, {}, ..]
// }
// What we do here is that each study entitlement is given as its own row, so same person may be included multiple times:
// {
//    henkilö: ...
//    oikeudet: [{}]
// }
// So there's just one study entitlement per person-study entitlement entry.
class TilastokeskusService(application: KoskiApplication) {

  def performOpiskeluoikeudetQueryLaajoillaHenkilötiedoilla(request: HttpServletRequest, koskiSession: KoskiSession, multiParams: MultiParams, paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[(LaajatOppijaHenkilöTiedot, List[OpiskeluoikeusRow])]] =
    OpiskeluoikeusQueryContext(request)(koskiSession, application).queryLaajoillaHenkilöTiedoilla(multiParams, paginationSettings)
}

case class OpiskeluoikeusQueryContext(request: HttpServletRequest)(implicit koskiSession: KoskiSession, application: KoskiApplication) extends Logging {
  def queryWithoutHenkilötiedotRaw(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings], queryForAuditLog: String): Observable[(Oid, List[OpiskeluoikeusRow])] = {
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> queryForAuditLog)))
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings)
  }


  def queryLaajoillaHenkilöTiedoilla(params: MultiParams, paginationSettings: Option[PaginationSettings]): Either[HttpStatus, Observable[(LaajatOppijaHenkilöTiedot, List[OpiskeluoikeusRow])]] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryFilter.parse(params)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession).map { filters =>
      AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(params))))
      query(filters, paginationSettings)
    }
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings]) = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluoikeusRow])] = OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, paginationSettings)
    oikeudetPerOppijaOid.tumblingBuffer(10).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluoikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[Oid, LaajatOppijaHenkilöTiedot] = application.opintopolkuHenkilöFacade.findMasterOppijat(oids)

        val oppijat: Iterable[(LaajatOppijaHenkilöTiedot, List[OpiskeluoikeusRow])] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
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
  def queryForAuditLog(params: MultiParams) =
    params.toList.sortBy(_._1).map { case (p,values) => values.map(v => p + "=" + v).mkString("&") }.mkString("&")

  def streamingQueryGroupedByOid(application: KoskiApplication, filters: List[OpiskeluoikeusQueryFilter], paginationSettings: Option[PaginationSettings])(implicit koskiSession: KoskiSession): Observable[(Oid, List[(OpiskeluoikeusRow)])] = {
    val rows = application.opiskeluoikeusQueryRepository.opiskeluoikeusQuery(filters, Some(Ascending("oppijaOid")), paginationSettings)

    val groupedByPerson: Observable[List[(OpiskeluoikeusRow, HenkilöRow, Option[HenkilöRow])]] = rows
      .tumblingBuffer(rows.map(masterOid))
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
