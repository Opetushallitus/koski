package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db._
import fi.oph.tor.oppija.{MockOppijaRepository, VerifiedOppijaOid}
import fi.oph.tor.organisaatio.MockOrganisaatioRepository
import fi.oph.tor.schema._
import fi.oph.tor.toruser.MockUsers
import fi.vm.sade.utils.Timer
import slick.dbio.DBIO

class TorDatabaseFixtureCreator(database: TorDatabase, repository: OpiskeluOikeusRepository) extends Futures with GlobalExecutionContext {
  val oppijat = new MockOppijaRepository

  def resetFixtures: Unit = Timer.timed("resetFixtures", 10) {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val deleteOpiskeluOikeudet = oppijat.defaultOppijat.map{oppija => OpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).delete}

    await(database.db.run(DBIO.sequence(deleteOpiskeluOikeudet)))

    defaultOpiskeluOikeudet.foreach { case (oid, oikeus) =>
      repository.createOrUpdate(VerifiedOppijaOid(oid), oikeus)(MockUsers.kalle.asTorUser)
    }
  }


  private def defaultOpiskeluOikeudet = {
    List((oppijat.eero.oid, opiskeluOikeus("1")),
      (oppijat.eerola.oid, opiskeluOikeus("1")),
      (oppijat.teija.oid, opiskeluOikeus("1")),
      (oppijat.markkanen.oid, opiskeluOikeus("3")))
  }
  private def opiskeluOikeus(oppilaitosId: String) = {
    val oppilaitos: OidOrganisaatio = MockOrganisaatioRepository.getOrganisaatio(oppilaitosId).get

    OpiskeluOikeus(
      None,
      None,
      None,
      None,
      None,
      oppilaitos,
      Suoritus(
        None,
        TutkintoKoulutustoteutus(TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", Some(4)), Some("39/011/2014")), None, None, None, None),
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