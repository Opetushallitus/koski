package fi.oph.tor.fixture

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db._
import fi.oph.tor.documentation.{ExamplesLukio, AmmatillinenTodistusExample, ExamplesAmmatillinen, ExamplesPerusopetus}
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.tor.oppija.{MockOppijat, OppijaRepository, VerifiedOppijaOid}
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._
import fi.oph.tor.tor.TorValidator
import fi.oph.tor.toruser.{AccessType, MockUsers}
import fi.oph.tor.util.Timing
import slick.dbio.DBIO

class TorDatabaseFixtureCreator(database: TorDatabase, repository: OpiskeluOikeusRepository, oppijaRepository: OppijaRepository, validator: TorValidator) extends Futures with Timing {
  def resetFixtures: Unit = timed("resetFixtures", 10) {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")
    implicit val user = MockUsers.kalle.asTorUser
    implicit val accessType = AccessType.write

    val oppijat: List[TaydellisetHenkilötiedot] = oppijaRepository.findOppijat("")
    val deleteOpiskeluOikeudet = oppijat.map{oppija => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oppija.oid).delete}

    await(database.db.run(DBIO.sequence(deleteOpiskeluOikeudet)))

    defaultOpiskeluOikeudet.foreach { case (oid, oikeus) =>
      validator.validateAsJson(Oppija(OidHenkilö(oid), List(oikeus))) match {
        case Right(oppija) => repository.createOrUpdate(VerifiedOppijaOid(oid), oppija.opiskeluoikeudet(0))
      }
    }
  }

  private def defaultOpiskeluOikeudet = {
    List((MockOppijat.eero.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.omnomnia)),
      (MockOppijat.koululainen.oid, ExamplesPerusopetus.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.oppiaineenKorottaja.oid, ExamplesPerusopetus.aineopiskelija.opiskeluoikeudet.head),
      (MockOppijat.lukiolainen.oid, ExamplesLukio.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.ammattilainen.oid, AmmatillinenTodistusExample.todistus.opiskeluoikeudet.head))
  }
}

object OpiskeluOikeusTestData {
  def opiskeluOikeus(oppilaitosId: String, koulutusKoodi: Int = 351301): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      None,
      None,
      None,
      None,
      None,
      None,
      oppilaitos, None,
      List(AmmatillisenTutkinnonSuoritus(
        AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some("39/011/2014")),
        None,
        None,
        None,
        None,
        None,
        None,
        Koodistokoodiviite("KESKEN", "suorituksentila"),
        None,
        oppilaitos,
        None,
        None,
        None
      )),
      hojks = None,
      Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi"),
      None,
      None
    )
  }
}