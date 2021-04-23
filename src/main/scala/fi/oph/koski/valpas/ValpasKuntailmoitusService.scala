package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.{AuditLog, KoskiMessageField, Logging}
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedot, ValpasKuntailmoitusLaajatTiedotJaOppijaOid, ValpasKuntailmoitusSuppeatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntailmoitusService(
  application: KoskiApplication
) extends Logging with Timing {
  private lazy val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)
  private lazy val oppijaService = application.valpasOppijaService
  private lazy val valpasRajapäivätService = application.valpasRajapäivätService

  def createKuntailmoitus(
    kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid,
  )(implicit session: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    val organisaatioOidit = Set(kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio.oid)

    accessResolver.organisaatiohierarkiaOids(organisaatioOidit) match {
      case Right(_) => {
        oppijaService.opiskeleeOppivelvollisuusOpintojaOppilaitoksessa(
          kuntailmoitusInput.oppijaOid,
          kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio.oid
        ).flatMap(opiskelee => Either.cond(
          opiskelee,
          withAuditLogOppijaKuntailmoitus(kuntailmoitusInput.oppijaOid)(
            kuntailmoitusInput.kuntailmoitus.copy(aikaleima = Some(valpasRajapäivätService.tarkastelupäivä.atTime(8, 0))) // TODO: Oikea toteutus
          ),
          ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta")
        ))
      }
      case Left(_) => Left(ValpasErrorCategory.forbidden.organisaatio(
        "Käyttäjällä ei ole oikeutta tehdä kuntailmoitusta annetun organisaation nimissä")
      )
    }
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
