package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db._
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.oppija.{MockOppijat, VerifiedOppijaOid}
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.schema._
import fi.oph.tor.toruser.MockUsers
import fi.vm.sade.utils.Timer
import slick.dbio.DBIO

class TorDatabaseFixtureCreator(database: TorDatabase, repository: OpiskeluOikeusRepository, opiskeluOikeusTestData: OpiskeluOikeusTestData) extends Futures with GlobalExecutionContext {
  def resetFixtures: Unit = Timer.timed("resetFixtures", 10) {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")
    implicit val user = MockUsers.kalle.asTorUser

    val deleteOpiskeluOikeudet = MockOppijat.defaultOppijat.map{oppija => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oppija.oid).delete}

    await(database.db.run(DBIO.sequence(deleteOpiskeluOikeudet)))

    defaultOpiskeluOikeudet.foreach { case (oid, oikeus) =>
      repository.createOrUpdate(VerifiedOppijaOid(oid), oikeus)
    }
  }

  private def defaultOpiskeluOikeudet = {
    List((MockOppijat.eero.oid, opiskeluOikeusTestData.opiskeluOikeus("1")),
      (MockOppijat.eerola.oid, opiskeluOikeusTestData.opiskeluOikeus("1")),
      (MockOppijat.teija.oid, opiskeluOikeusTestData.opiskeluOikeus("1")),
      (MockOppijat.markkanen.oid, opiskeluOikeusTestData.opiskeluOikeus("3")))
  }
}

class OpiskeluOikeusTestData(organisaatioRepository: OrganisaatioRepository, koodistoViitePalvelu: KoodistoViitePalvelu) {
  def opiskeluOikeus(oppilaitosId: String, koulutusKoodi: Int = 351301) = {
    val oppilaitos: OidOrganisaatio = organisaatioRepository.getOrganisaatio(oppilaitosId).get
    val koulutusKoodiViite = koodistoViitePalvelu.getKoodistoKoodiViite("koulutus", koulutusKoodi.toString).get

    OpiskeluOikeus(
      None,
      None,
      None,
      None,
      None,
      None,
      oppilaitos,
      Suoritus(
        None,
        TutkintoKoulutustoteutus(TutkintoKoulutus(koulutusKoodiViite, Some("39/011/2014")), None, None, None, None),
        None,
        None,
        None,
        oppilaitos,
        None,
        None,
        None
      ),
      hojks = None,
      None,
      None,
      None
    )
  }
}