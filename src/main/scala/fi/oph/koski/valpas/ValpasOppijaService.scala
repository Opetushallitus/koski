package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.log.{AuditLog, KoskiMessageField, Logging}
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.valpas.hakukooste.ValpasHakukoosteService
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.repository._
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasOppijaService(
  application: KoskiApplication,
  hakukoosteService: ValpasHakukoosteService,
) extends Logging {
  private val dbService = new ValpasDatabaseService(application)
  private val koodisto = application.koodistoPalvelu
  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)
  private val rajapäivät: () => Rajapäivät = Rajapäivät(Environment.isLocalDevelopmentEnvironment)

  type OppijaHakutilanteilla = (ValpasOppijaLisätiedoilla, Seq[ValpasHakutilanne])

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijat(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteilla]] =
    accessResolver.organisaatiohierarkiaOids(oppilaitosOids)
      .map(oids => dbService.getPeruskoulunValvojalleNäkyvätOppijat(Some(oids.toSeq), rajapäivät()))
      .map(_.map(enrichOppija))
      .flatMap(fetchHaut)
      .map(tiedot => {
        oppilaitosOids.foreach(auditLogOppilaitoksenOppijatKatsominen)
        tiedot
      })

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppija(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Either[HttpStatus, OppijaHakutilanteilla] =
    dbService.getPeruskoulunValvojalleNäkyväOppija(oppijaOid, rajapäivät())
      .toRight(ValpasErrorCategory.forbidden.oppija())
      .flatMap(accessResolver.withOppijaAccess)
      .map(enrichOppija)
      .flatMap(fetchHaku)
      .map { case (oppija, hakutilanteet) =>
        auditLogOppijaKatsominen(oppija)
        (oppija, hakutilanteet)
      }

  private def fetchHaku(oppija: ValpasOppijaLisätiedoilla): Either[HttpStatus, (ValpasOppijaLisätiedoilla, Seq[ValpasHakutilanne])] = {
    hakukoosteService.getHakukoosteet(Set(oppija.henkilö.oid))
      .map(hakukoosteet => (oppija, hakukoosteet.map(ValpasHakutilanne.apply)))
  }

  private def fetchHaut(oppijat: Seq[ValpasOppijaLisätiedoilla]): Either[HttpStatus, Seq[OppijaHakutilanteilla]] = {
    hakukoosteService.getHakukoosteet(oppijat.map(_.henkilö.oid).toSet)
      .map(_.groupBy(_.oppijaOid))
      .map(groups => oppijat.map(oppija =>
        (oppija, groups.getOrElse(oppija.henkilö.oid, Seq()).map(ValpasHakutilanne.apply))
      ))
  }

  def enrichOppija(oppija: ValpasOppijaResult): ValpasOppijaLisätiedoilla =
    ValpasOppijaLisätiedoilla(
      oppija.copy(
        opiskeluoikeudet = oppija.opiskeluoikeudet.map(opiskeluoikeus =>
          opiskeluoikeus.copy(
            tyyppi = enrichKoodistokoodiviite(opiskeluoikeus.tyyppi),
            viimeisinTila = enrichKoodistokoodiviite(opiskeluoikeus.viimeisinTila)
          )
        )
      )
    )

  private def enrichKoodistokoodiviite(koodiviite: Koodistokoodiviite): Koodistokoodiviite =
    if (koodiviite.nimi.isDefined) {
      koodiviite
    } else {
      koodisto
        .getKoodistoKoodit(KoodistoViite(koodiviite.koodistoUri, koodiviite.koodistoVersio.getOrElse(1)))
        .find(_.koodiArvo == koodiviite.koodiarvo)
        .map(k => Koodistokoodiviite(
          koodiarvo = k.koodiArvo,
          nimi = k.nimi,
          lyhytNimi = k.lyhytNimi,
          koodistoUri = k.koodistoUri,
          koodistoVersio = Some(k.versio)
        ))
        .getOrElse(koodiviite)
    }

  private def auditLogOppijaKatsominen(oppija: ValpasOppija)(implicit session: ValpasSession): Unit =
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KATSOMINEN,
      Map(KoskiMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)
    ))

  private def auditLogOppilaitoksenOppijatKatsominen(oppilaitosOid: String)(implicit session: ValpasSession): Unit =
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      Map(KoskiMessageField.juuriOrganisaatio -> oppilaitosOid)
    ))
}
