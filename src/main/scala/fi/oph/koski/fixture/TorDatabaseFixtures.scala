package fi.oph.koski.fixture

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db._
import fi.oph.koski.documentation._
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.oppija.{MockOppijat, OppijaRepository, VerifiedOppijaOid}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.koski.KoskiValidator
import fi.oph.koski.koskiuser.{AccessType, MockUsers}
import fi.oph.koski.util.Timing
import slick.dbio.DBIO

class KoskiDatabaseFixtureCreator(database: KoskiDatabase, repository: OpiskeluOikeusRepository, oppijaRepository: OppijaRepository, validator: KoskiValidator) extends Futures with Timing {
  def resetFixtures: Unit = timed("resetFixtures", 10) {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")
    implicit val user = MockUsers.kalle.asKoskiUser
    implicit val accessType = AccessType.write

    val oppijat: List[TaydellisetHenkilötiedot] = oppijaRepository.findOppijat("")
    val deleteOpiskeluOikeudet = oppijat.map{oppija => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oppija.oid).delete}

    await(database.db.run(DBIO.sequence(deleteOpiskeluOikeudet)))

    defaultOpiskeluOikeudet.foreach { case (oid, oikeus) =>
      validator.validateAsJson(Oppija(OidHenkilö(oid), List(oikeus))) match {
        case Right(oppija) => repository.createOrUpdate(VerifiedOppijaOid(oid), oppija.tallennettavatOpiskeluoikeudet(0))
      }
    }
  }

  private def defaultOpiskeluOikeudet = {
    List((MockOppijat.eero.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.omnomnia)),
      (MockOppijat.koululainen.oid, ExamplesPerusopetus.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.toimintaAlueittainOpiskelija.oid, ExamplesPerusopetus.toimintaAlueittainOpiskelija.opiskeluoikeudet.head),
      (MockOppijat.oppiaineenKorottaja.oid, ExamplesPerusopetus.aineopiskelija.opiskeluoikeudet.head),
      (MockOppijat.kymppiluokkalainen.oid, ExamplesPerusopetuksenLisaopetus.lisäopetuksenPäättötodistus.opiskeluoikeudet.head),
      (MockOppijat.lukiolainen.oid, ExamplesPerusopetus.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.lukiolainen.oid, ExamplesLukio.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.luva.oid, ExamplesLukioonValmistavaKoulutus.luvaTodistus.opiskeluoikeudet.head),
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
        None
      )),
      hojks = None,
      Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi"),
      None,
      None
    )
  }
}