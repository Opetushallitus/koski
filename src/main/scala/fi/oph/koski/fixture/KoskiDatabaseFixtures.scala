package fi.oph.koski.fixture

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusMitätöity, suomenKieli}
import fi.oph.koski.documentation.ExamplesPerusopetus.ysinOpiskeluoikeusKesken
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.{MockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import slick.dbio.DBIO

class KoskiDatabaseFixtureCreator(application: KoskiApplication) extends KoskiDatabaseMethods with Timing {
  implicit val user = KoskiSession.systemUser
  val database = application.masterDatabase
  val db = database.db
  implicit val accessType = AccessType.write

  def resetFixtures: Unit = {
    application.perustiedotSyncScheduler.sync

    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val oids = MockOppijat.oids.sorted

    runDbSync(OpiskeluOikeudet.filter(_.oppijaOid inSetBind (oids)).delete)
    application.perustiedotIndexer.deleteByOppijaOids(oids)

    val henkilöOids: List[Oid] = oids
    runDbSync(Tables.Henkilöt.filter(_.oid inSetBind henkilöOids).delete)
    runDbSync(Preferences.delete)
    runDbSync(Tables.PerustiedotSync.delete)

    application.perustiedotIndexer.updateBulk(validatedOpiskeluoikeudet.map { case (henkilö, opiskeluoikeus) =>
      val id = application.opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(henkilö), opiskeluoikeus, false).right.get.id
      OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, Some(application.henkilöRepository.opintopolku.withMasterInfo(henkilö)))
    }, true)
  }

  // cached for performance boost
  private lazy val validatedOpiskeluoikeudet: List[(TäydellisetHenkilötiedot, KoskeenTallennettavaOpiskeluoikeus)] = defaultOpiskeluOikeudet.map { case (henkilö, oikeus) =>
    application.validator.validateAsJson(Oppija(henkilö.henkilö, List(oikeus))) match {
      case Right(oppija) => (henkilö.henkilö, oppija.tallennettavatOpiskeluoikeudet(0))
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + henkilö.kokonimi +  " with data " + JsonSerializer.write(oikeus) + ": " + status)
    }
  }

  private def defaultOpiskeluOikeudet: List[(TäydellisetHenkilötiedotWithMasterInfo, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (MockOppijat.eero, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.syntymäajallinen, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen, OpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia)),
      (MockOppijat.eskari, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.ysiluokkalainen, ysinOpiskeluoikeusKesken),
      (MockOppijat.hetuton, ysinOpiskeluoikeusKesken),
      (MockOppijat.monessaKoulussaOllut, ysinOpiskeluoikeusKesken),
      (MockOppijat.monessaKoulussaOllut, ExamplesPerusopetus.seiskaTuplattuOpiskeluoikeus),
      (MockOppijat.koululainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (MockOppijat.koululainen, ExamplesPerusopetukseenValmistavaOpetus.perusopetukseenValmistavaOpiskeluoikeus),
      (MockOppijat.toimintaAlueittainOpiskelija, ExamplesPerusopetus.toimintaAlueittainOpiskelija.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.oppiaineenKorottaja, ExamplesAikuistenPerusopetus.aineopiskelija.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.aikuisOpiskelija, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen),
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
      (MockOppijat.reformitutkinto, ReforminMukainenErikoisammattitutkintoExample.opiskeluoikeus),
      (MockOppijat.osittainenammattitutkinto, AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus),
      (MockOppijat.paikallinenTunnustettu, AmmatillinenPerustutkintoExample.tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus),
      (MockOppijat.tiedonsiirto, OpiskeluoikeusTestData.lähdejärjestelmällinenOpiskeluoikeus),
      (MockOppijat.omattiedot, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "D")),
      (MockOppijat.omattiedot, ExamplesLukio.päättötodistus()),
      (MockOppijat.omattiedotSlave, AmmatillinenOldExamples.uusi.tallennettavatOpiskeluoikeudet(0)),
      (MockOppijat.ibFinal, ExamplesIB.opiskeluoikeus),
      (MockOppijat.ibPredicted, ExamplesIB.opiskeluoikeusPredictedGrades),
      (MockOppijat.eero, OpiskeluoikeusTestData.mitätöityOpiskeluoikeus),
      (MockOppijat.master, ExamplesPerusopetus.päättötodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.slave, ExamplesLukio.päättötodistus()),
      (MockOppijat.turvakielto, ExamplesLukio.päättötodistus())
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
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli,
        suoritustapa = AmmatillinenExampleData.suoritustapaOps
      )),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2000, 1, 1), ExampleData.opiskeluoikeusLäsnä, None))),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        vankilaopetuksessa = Some(List(Aikajakso(date(2001, 1, 1), None)))
      ))
    )
  }

  lazy val mitätöityOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    ysinOpiskeluoikeusKesken.copy(tila =
      ysinOpiskeluoikeusKesken.tila.copy(opiskeluoikeusjaksot =
        ysinOpiskeluoikeusKesken.tila.opiskeluoikeusjaksot :+ NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
      )
    )

  lazy val lähdejärjestelmällinenOpiskeluoikeus: AmmatillinenOpiskeluoikeus =
    opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto).copy(lähdejärjestelmänId = Some(AmmatillinenExampleData.winnovaLähdejärjestelmäId))
}
