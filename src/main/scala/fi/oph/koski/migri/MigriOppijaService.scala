package fi.oph.koski.migri

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.WithWarnings

class MigriOppijaService(application: KoskiApplication) {
  // YKI-tietoja ei vielä luovuteta tuotannossa: ykitiedot-parametri jätetään siellä huomiotta.
  protected def ykitiedotKäytössä: Boolean = !Environment.isProdEnvironment(application.config)

  // Migrin käyttöoikeusryhmällä ei ole kielitutkintojen lukuoikeutta, joten YKI-opiskeluoikeudet haetaan
  // järjestelmäsessiolla kuten Kela- ja SDG-rajapinnoissa. Kutsujan sessiota käytetään henkilöhakuun ja auditlokiin.
  private implicit val katselija: KoskiSpecificSession = KoskiSpecificSession.systemKatselijaUser

  def findByOid(oid: String, ykitiedot: Boolean)(koskiSession: KoskiSpecificSession): Either[HttpStatus, MigriOppija] =
    if (ykitiedot && ykitiedotKäytössä) {
      ykiOppija(application.henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true))(koskiSession)
    } else {
      koskiOppija(application.oppijaFacade.findOppija(oid, findMasterIfSlaveOid = true, useVirta = true, useYtr = true)(koskiSession))
    }

  def findByHetu(hetu: String, ykitiedot: Boolean)(koskiSession: KoskiSpecificSession): Either[HttpStatus, MigriOppija] =
    if (ykitiedot && ykitiedotKäytössä) {
      ykiOppija(application.henkilöRepository.opintopolku.findByHetu(hetu))(koskiSession)
    } else {
      koskiOppija(application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = true, useYtr = true)(koskiSession))
    }

  private def koskiOppija(oppija: Either[HttpStatus, WithWarnings[Oppija]]): Either[HttpStatus, MigriOppija] =
    oppija
      .flatMap(_.warningsToLeft)
      .flatMap(o => ConvertMigriSchema.convert(o).toRight(notFound))

  private def ykiOppija(henkilö: Option[LaajatOppijaHenkilöTiedot])(koskiSession: KoskiSpecificSession): Either[HttpStatus, MigriOppija] =
    henkilö
      .toRight(notFound)
      .flatMap { h =>
        val opiskeluoikeudet = application.opiskeluoikeusRepository.findByOppija(h, useVirta = false, useYtr = false).getIgnoringWarnings
        val täydellisetHenkilötiedot = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(h)
        ConvertMigriSchema.convertYki(täydellisetHenkilötiedot, opiskeluoikeudet).toRight(notFound)
      }
      .map { oppija =>
        auditLogOpiskeluoikeusKatsominen(oppija.henkilö.oid)(koskiSession)
        oppija
      }

  private def auditLogOpiskeluoikeusKatsominen(oppijaOid: String)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(
      KoskiOperation.OPISKELUOIKEUS_KATSOMINEN,
      koskiSession,
      Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid)
    ))

  private def notFound: HttpStatus = KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()
}
