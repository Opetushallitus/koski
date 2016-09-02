package fi.oph.koski.fixture

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.documentation._
import fi.oph.koski.json.Json
import fi.oph.koski.koski.KoskiValidator
import fi.oph.koski.koskiuser.{AccessType, KoskiUser, MockUsers}
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.oppija.{MockOppijat, OppijaRepository, VerifiedOppijaOid}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import java.time.LocalDate.{of => date}

import slick.dbio.DBIO

class KoskiDatabaseFixtureCreator(database: KoskiDatabase, repository: OpiskeluOikeusRepository, oppijaRepository: OppijaRepository, validator: KoskiValidator) extends Futures with Timing {
  implicit val user = KoskiUser.systemUser
  implicit val accessType = AccessType.write

  def resetFixtures: Unit = timed("resetFixtures", 10) {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val oppijat: List[HenkilötiedotJaOid] = oppijaRepository.findOppijat("")
    val deleteOpiskeluOikeudet = oppijat.map{oppija => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oppija.oid).delete}
    val deleteTiedonsiirrot = TiedonsiirtoWithAccessCheck.delete

    await(database.db.run(DBIO.sequence(deleteOpiskeluOikeudet)))
    await(database.db.run(deleteTiedonsiirrot))

    validatedOpiskeluoikeudet.foreach {
      case (oid, oppija) => repository.createOrUpdate(VerifiedOppijaOid(oid), oppija.tallennettavatOpiskeluoikeudet(0))
    }
  }

  // cached for performance boost
  private lazy val validatedOpiskeluoikeudet: List[(Oid, Oppija)] = defaultOpiskeluOikeudet.map { case (oid, oikeus) =>
    validator.validateAsJson(Oppija(OidHenkilö(oid), List(oikeus))) match {
      case Right(oppija) => (oid, oppija)
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + oid +  " with data " + Json.write(oikeus) + ": " + status)
    }
  }

  private def defaultOpiskeluOikeudet = {
    List((MockOppijat.eero.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.omnia)),
      (MockOppijat.koululainen.oid, ExamplesPerusopetus.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.koululainen.oid, ExamplesPerusopetukseenValmistavaOpetus.opiskeluoikeus),
      (MockOppijat.toimintaAlueittainOpiskelija.oid, ExamplesPerusopetus.toimintaAlueittainOpiskelija.opiskeluoikeudet.head),
      (MockOppijat.oppiaineenKorottaja.oid, ExamplesPerusopetus.aineopiskelija.opiskeluoikeudet.head),
      (MockOppijat.kymppiluokkalainen.oid, ExamplesPerusopetuksenLisaopetus.lisäopetuksenPäättötodistus.opiskeluoikeudet.head),
      (MockOppijat.lukiolainen.oid, ExamplesPerusopetus.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.lukiolainen.oid, ExamplesLukio.päättötodistus.opiskeluoikeudet.head),
      (MockOppijat.luva.oid, ExamplesLukioonValmistavaKoulutus.luvaTodistus.opiskeluoikeudet.head),
      (MockOppijat.ammattilainen.oid, AmmatillinenPerustutkintoExample.todistus.opiskeluoikeudet.head),
      (MockOppijat.valma.oid, ExamplesValma.valmaTodistus.opiskeluoikeudet.head),
      (MockOppijat.telma.oid, ExamplesTelma.telmaTodistus.opiskeluoikeudet.head),
      (MockOppijat.erikoisammattitutkinto.oid, AmmattitutkintoExample.opiskeluoikeus)
    )
  }
}

object OpiskeluOikeusTestData {
  def opiskeluOikeus(oppilaitosId: String, koulutusKoodi: Int = 351301): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = oppilaitos,
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some("39/011/2014")),
        tila = Koodistokoodiviite("KESKEN", "suorituksentila"),
        toimipiste = oppilaitos
      )),
      tavoite = AmmatillinenExampleData.tavoiteTutkinto,
      alkamispäivä = Some(date(2000, 1, 1)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2000, 1, 1), ExampleData.opiskeluoikeusLäsnä, None)))
    )
  }
}