package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db._
import fi.oph.tor.oppija.MockOppijaRepository
import fi.oph.tor.organisaatio.MockOrganisaatioRepository
import fi.oph.tor.schema._
import slick.dbio.DBIO

object TorDatabaseFixtures extends Futures with GlobalExecutionContext {
  private val oppijat = new MockOppijaRepository

  def opiskeluOikeus(oppilaitosId: String) = {
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

  private def defaultOpiskeluOikeudet = {
    List((oppijat.eero.oid, opiskeluOikeus("1")),
         (oppijat.eerola.oid, opiskeluOikeus("1")),
         (oppijat.teija.oid, opiskeluOikeus("1")),
         (oppijat.markkanen.oid, opiskeluOikeus("3")))
  }

  def resetFixtures(database: TorDatabase): Unit = {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val deleteOpiskeluOikeudet = oppijat.defaultOppijat.map{oppija => OpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).delete}

    await(database.db.run(DBIO.sequence(
      deleteOpiskeluOikeudet ++ List(OpiskeluOikeudet ++= defaultOpiskeluOikeudet.map{case (oid, oikeus) => new OpiskeluOikeusRow(oid, oikeus, 1)})
    )))
  }
}