package fi.oph.koski.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.common.log._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter}
import fi.oph.koski.schema.{Henkilö, Oppija}
import org.json4s.JsonAST.JValue
import rx.lang.scala.Observable

class KelaService(application: KoskiApplication) extends Logging {
  def findKelaOppijaByHetu(hetu: String)(implicit koskiSession: KoskiSession): Either[HttpStatus, KelaOppija] = {
    //Kela ei ole kiinnostunut tässä tapauksessa korkeakoulujen opiskeluoikeuksista
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = false, useYtr = true)
      .flatMap(_.warningsToLeft)
      .flatMap(KelaOppijaConverter.convertOppijaToKelaOppija)
  }

  def streamOppijatByHetu(hetut: List[String])(implicit koskiSession: KoskiSession): Observable[JValue] = {
    val user = koskiSession // take current session so it can be used in observable
    val henkilot = application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut)
    val oidToHenkilo: Map[Henkilö.Oid, OppijaHenkilö] = henkilot.map(h => h.oid -> h).toMap
    val masterOids = henkilot.map(_.oid)
    val queryFilters = List(OpiskeluoikeusQueryFilter.OppijaOidHaku(masterOids ++ application.henkilöCache.resolveLinkedOids(masterOids)))

    streamingQuery(queryFilters)
      .map(t => Oppija(oidToHenkilo(t._1.oid).toHenkilötiedotJaOid, t._2.map(_.toOpiskeluoikeus)))
      .map(KelaOppijaConverter.convertOppijaToKelaOppija)
      .collect {
        case Right(kelaOppija) => kelaOppija
        case Left(status) if (status.statusCode != 404) => throw new RuntimeException("Error while streaming KelaOppija")
      }
      .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(user))
      .map(JsonSerializer.serializeWithUser(user))
  }

  def opiskeluoikeudenHistoria(opiskeluoikeusOid: String)(implicit koskiSession: KoskiSession): Option[List[OpiskeluoikeusHistoryPatch]] = {
    val history: Option[List[OpiskeluoikeusHistoryPatch]] = application.historyRepository.findByOpiskeluoikeusOid(opiskeluoikeusOid)(koskiSession)
    history.foreach { _ => auditLogHistoryView(opiskeluoikeusOid)(koskiSession)}
    history
  }

  def findKelaOppijaVersion(oppijaOid: String, opiskeluoikeusOid: String, version: Int)(implicit koskiSession: KoskiSession): Either[HttpStatus, KelaOppija] = {
   application.oppijaFacade.findVersion(oppijaOid, opiskeluoikeusOid, version)
      .map(t => Oppija(t._1.toHenkilötiedotJaOid, t._2))
      .flatMap(KelaOppijaConverter.convertOppijaToKelaOppija)
  }

  private def streamingQuery(filters: List[OpiskeluoikeusQueryFilter])(implicit koskiSession: KoskiSession) =
    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, filters, None)


  private def auditLogOpiskeluoikeusKatsominen(oppija: KelaOppija)(koskiSession: KoskiSession): Unit =
    AuditLog.log(AuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)))

  private def auditLogHistoryView(opiskeluoikeusOid: String)(koskiSession: KoskiSession): Unit =
    AuditLog.log(AuditLogMessage(KoskiOperation.MUUTOSHISTORIA_KATSOMINEN, koskiSession, Map(KoskiMessageField.opiskeluoikeusOid -> opiskeluoikeusOid)))
}
