package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.{AuditLog, KoskiMessageField, Logging}
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedot, ValpasKuntailmoitusLaajatTiedotJaOppijaOid, ValpasKuntailmoitusQueryService, ValpasKuntailmoitusSuppeatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntailmoitusService(
  application: KoskiApplication
) extends Logging with Timing {

  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)
  private val queryService = new ValpasKuntailmoitusQueryService(application)
  private val oppijaService = application.valpasOppijaService

  def createKuntailmoitus
    (kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    val organisaatioOid = kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio.oid

    accessResolver.organisaatiohierarkiaOids(Set(organisaatioOid))
      .left.map(_ => ValpasErrorCategory.forbidden.organisaatio("Käyttäjällä ei ole oikeutta tehdä kuntailmoitusta annetun organisaation nimissä"))
      .flatMap(_ => oppijaService.getOppijaLaajatTiedot(kuntailmoitusInput.oppijaOid))
      .flatMap(oppija =>
        accessResolver.withOppijaAccessAsOrganisaatio(organisaatioOid)(oppija)
          .left.map(_ => ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta"))
      )
      .flatMap(_ => queryService.create(kuntailmoitusInput))
      .map(_.kuntailmoitus)
      .map(withAuditLogOppijaKuntailmoitus(kuntailmoitusInput.oppijaOid))
  }

  private def withAuditLogOppijaKuntailmoitus(oppijaOid: String)
                                             (result: ValpasKuntailmoitusLaajatTiedot)
                                             (implicit session: ValpasSession): ValpasKuntailmoitusLaajatTiedot = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KUNTAILMOITUS,
      Map(KoskiMessageField.oppijaHenkiloOid -> oppijaOid) // TODO: pitäisikö olla muutakin dataa kuin oppijan oid? Ts. pitäisikö auditlogista näkyä, että mikä oppilaitos/kunta on tehnyt ilmoituksen mihin kuntaan?
    ))
    result
  }
}
