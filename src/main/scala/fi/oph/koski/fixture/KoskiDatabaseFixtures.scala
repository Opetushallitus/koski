package fi.oph.koski.fixture

import java.time.LocalDate.{of => date}

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.documentation.ExampleData.suomenKieli
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.{HenkilöRepository, MockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusRepository
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedot, OpiskeluoikeudenPerustiedotIndexer}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import fi.oph.koski.validation.KoskiValidator
import slick.dbio.DBIO

class KoskiDatabaseFixtureCreator(database: KoskiDatabase, repository: OpiskeluoikeusRepository, henkilöRepository: HenkilöRepository, perustiedot: OpiskeluoikeudenPerustiedotIndexer, validator: KoskiValidator) extends KoskiDatabaseMethods with Timing {
  implicit val user = KoskiSession.systemUser
  val db = database.db
  implicit val accessType = AccessType.write

  def resetFixtures: Unit = {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val oids = MockOppijat.oids

    val deleteOpiskeluOikeudet = oids.map{oid => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oid).delete}

    runDbSync(DBIO.sequence(deleteOpiskeluOikeudet))

    perustiedot.deleteByOppijaOids(oids)

    val henkilöOids: List[Oid] = oids
    runDbSync(Tables.Henkilöt.filter(_.oid inSetBind henkilöOids).delete)
    runDbSync(DBIO.sequence(henkilöOids.flatMap(henkilöRepository.findByOid).map{ henkilö => Henkilöt += HenkilöRow(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi) }))

    runDbSync(Preferences.delete)

    validatedOpiskeluoikeudet.foreach { case (henkilö, opiskeluoikeus) =>
      val id = repository.createOrUpdate(VerifiedHenkilöOid(henkilö), opiskeluoikeus, false).right.get.id
      perustiedot.update(OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, henkilö))
    }
  }

  // cached for performance boost
  private lazy val validatedOpiskeluoikeudet: List[(TäydellisetHenkilötiedot, KoskeenTallennettavaOpiskeluoikeus)] = defaultOpiskeluOikeudet.map { case (henkilö, oikeus) =>
    validator.validateAsJson(Oppija(henkilö, List(oikeus))) match {
      case Right(oppija) => (henkilö, oppija.tallennettavatOpiskeluoikeudet(0))
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + henkilö.oid +  " with data " + Json.write(oikeus) + ": " + status)
    }
  }

  private def defaultOpiskeluOikeudet: List[(TäydellisetHenkilötiedot, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (MockOppijat.eero, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia)),
      (MockOppijat.eskari, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.ysiluokkalainen, ExamplesPerusopetus.ysinOpiskeluoikeusKesken),
      (MockOppijat.hetuton, ExamplesPerusopetus.ysinOpiskeluoikeusKesken),
      (MockOppijat.monessaKoulussaOllut, ExamplesPerusopetus.ysinOpiskeluoikeusKesken),
      (MockOppijat.monessaKoulussaOllut, ExamplesPerusopetus.seiskaTuplattuOpiskeluoikeus),
      (MockOppijat.koululainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (MockOppijat.koululainen, ExamplesPerusopetukseenValmistavaOpetus.opiskeluoikeus),
      (MockOppijat.toimintaAlueittainOpiskelija, ExamplesPerusopetus.toimintaAlueittainOpiskelija.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.oppiaineenKorottaja, ExamplesPerusopetus.aineopiskelija.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.kymppiluokkalainen, ExamplesPerusopetuksenLisaopetus.lisäopetuksenPäättötodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.lukiolainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "B")),
      (MockOppijat.lukiolainen, ExamplesLukio.päättötodistus()),
      (MockOppijat.lukioKesken, ExamplesLukio.lukioKesken),
      (MockOppijat.lukionAineopiskelija, ExamplesLukio.aineopiskelija),
      (MockOppijat.luva, ExamplesLukioonValmistavaKoulutus.luvaTodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.ammattilainen, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis()),
      (MockOppijat.amis, AmmatillinenExampleData.perustutkintoOpiskeluoikeusKesken()),
      (MockOppijat.valma, ExamplesValma.valmaTodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.telma, ExamplesTelma.telmaTodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.erikoisammattitutkinto, AmmattitutkintoExample.opiskeluoikeus),
      (MockOppijat.osittainenammattitutkinto, AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus),
      (MockOppijat.paikallinenTunnustettu, AmmatillinenPerustutkintoExample.tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus),
      (MockOppijat.omattiedot, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "D")),
      (MockOppijat.omattiedot, ExamplesLukio.päättötodistus()),
      (MockOppijat.ibFinal, ExamplesIB.opiskeluoikeus),
      (MockOppijat.ibPredicted, ExamplesIB.opiskeluoikeusPredictedGrades)
    )
  }
}

object OpiskeluoikeusTestData {
  def opiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some("39/011/2014")),
        tila = Koodistokoodiviite("KESKEN", "suorituksentila"),
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli
      )),
      alkamispäivä = Some(date(2000, 1, 1)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2000, 1, 1), ExampleData.opiskeluoikeusLäsnä, None)))
    )
  }
}