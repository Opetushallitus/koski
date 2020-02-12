package fi.oph.koski.fixture

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusMitätöity, suomenKieli}
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.aikuistenOppiaine
import fi.oph.koski.documentation.ExamplesPerusopetus.ysinOpiskeluoikeusKesken
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat, OppijaHenkilö, VerifiedHenkilöOid}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.perustiedot.{OpiskeluoikeudenOsittaisetTiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import slick.dbio.DBIO

class KoskiDatabaseFixtureCreator(application: KoskiApplication) extends KoskiDatabaseMethods with Timing {
  implicit val user = KoskiSession.systemUser
  val database = application.masterDatabase
  val db = database.db
  implicit val accessType = AccessType.write
  var fixtureCacheCreated = false
  var cachedPerustiedot: Option[Seq[OpiskeluoikeudenOsittaisetTiedot]] = None

  def resetFixtures: Unit = {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    application.perustiedotSyncScheduler.sync

    val henkilöOids = MockOppijat.oids.sorted

    runDbSync(DBIO.sequence(Seq(
      OpiskeluOikeudet.filter(_.oppijaOid inSetBind (henkilöOids)).delete,
      Tables.Henkilöt.filter(_.oid inSetBind henkilöOids).delete,
      Preferences.delete,
      Tables.PerustiedotSync.delete,
      Tables.SuoritusJako.delete,
      Tables.ValtuudetSession.delete
    ) ++ MockOppijat.defaultOppijat.map(application.henkilöCache.addHenkilöAction)))

    application.perustiedotIndexer.deleteByOppijaOids(henkilöOids)

    if (!fixtureCacheCreated) {
      cachedPerustiedot = Some(opiskeluoikeudet.map { case (henkilö, opiskeluoikeus) =>
        val id = application.opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(henkilö), opiskeluoikeus, false).right.get.id
        OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, application.henkilöRepository.opintopolku.withMasterInfo(henkilö))
      })
      application.perustiedotIndexer.updatePerustiedot(cachedPerustiedot.get, true)
      val henkilöOidsIn = henkilöOids.map("'" + _ + "'").mkString(",")
      runDbSync(DBIO.seq(
        sqlu"drop table if exists opiskeluoikeus_fixture",
        sqlu"create table opiskeluoikeus_fixture as select * from opiskeluoikeus where oppija_oid in (#$henkilöOidsIn)",
        sqlu"drop table if exists opiskeluoikeushistoria_fixture",
        sqlu"create table opiskeluoikeushistoria_fixture as select * from opiskeluoikeushistoria where opiskeluoikeus_id in (select id from opiskeluoikeus_fixture)"
      ))
      fixtureCacheCreated = true
    } else {
      runDbSync(DBIO.seq(
        sqlu"alter table opiskeluoikeus disable trigger update_opiskeluoikeus_aikaleima",
        sqlu"insert into opiskeluoikeus select * from opiskeluoikeus_fixture",
        sqlu"insert into opiskeluoikeushistoria select * from opiskeluoikeushistoria_fixture",
        sqlu"alter table opiskeluoikeus enable trigger update_opiskeluoikeus_aikaleima"
      ))
      application.perustiedotIndexer.updatePerustiedot(cachedPerustiedot.get, true)
    }
  }

  private lazy val opiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = validatedOpiskeluoikeudet.map { case (henkilö, oikeus) => henkilö.hetu match {
    case MockOppijat.organisaatioHistoria.hetu => (henkilö, oikeus.asInstanceOf[AmmatillinenOpiskeluoikeus].copy(organisaatiohistoria = Some(AmmatillinenExampleData.opiskeluoikeudenOrganisaatioHistoria)))
    case MockOppijat.tunnisteenKoodiarvoPoistettu.hetu => (henkilö, oikeus.asInstanceOf[AmmatillinenOpiskeluoikeus].copy(suoritukset = oikeus.suoritukset.map(s => s.asInstanceOf[AmmatillisenTutkinnonSuoritus].copy(koulutusmoduuli = s.koulutusmoduuli.asInstanceOf[AmmatillinenTutkintoKoulutus].copy(s.koulutusmoduuli.tunniste.asInstanceOf[Koodistokoodiviite].copy(koodiarvo = "123456")))).asInstanceOf[List[AmmatillinenPäätasonSuoritus]]))
    case _ => (henkilö, oikeus)
  }}

  private lazy val validatedOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = defaultOpiskeluOikeudet.map { case (henkilö, oikeus) =>
    application.validator.validateAsJson(Oppija(henkilö.toHenkilötiedotJaOid, List(oikeus))) match {
      case Right(oppija) => (henkilö, oppija.tallennettavatOpiskeluoikeudet(0))
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + henkilö.etunimet + " " + henkilö.sukunimi +  " with data " + JsonSerializer.write(oikeus) + ": " + status)
    }
  }

  private def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    List(
      (MockOppijat.eero, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.syntymäajallinen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia).copy(ostettu = true)),
      (MockOppijat.eskari, ExamplesEsiopetus.esioppilas.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.ysiluokkalainen, ysinOpiskeluoikeusKesken),
      (MockOppijat.hetuton, ysinOpiskeluoikeusKesken),
      (MockOppijat.monessaKoulussaOllut, ysinOpiskeluoikeusKesken),
      (MockOppijat.monessaKoulussaOllut, ExamplesPerusopetus.seiskaTuplattuOpiskeluoikeus),
      (MockOppijat.koululainen, ExamplesEsiopetus.opiskeluoikeusHelsingissä),
      (MockOppijat.koululainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus()),
      (MockOppijat.koululainen, ExamplesPerusopetukseenValmistavaOpetus.perusopetukseenValmistavaOpiskeluoikeus),
      (MockOppijat.luokallejäänyt, PerusopetusExampleData.päättötodistusLuokanTuplauksellaOpiskeluoikeus()),
      (MockOppijat.vuosiluokkalainen, PerusopetusExampleData.vuosiluokanOpiskeluoikeus()),
      (MockOppijat.toimintaAlueittainOpiskelija, ExamplesPerusopetus.toimintaAlueittainOpiskelija.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.oppiaineenKorottaja, ExamplesAikuistenPerusopetus.oppiaineenOppimääräOpiskeluoikeus),
      (MockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa, ExamplesAikuistenPerusopetus.montaOppiaineenOppimääränSuoritustaOpiskeluoikeus),
      (MockOppijat.aikuisOpiskelija, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen),
      (MockOppijat.kymppiluokkalainen, ExamplesPerusopetuksenLisaopetus.lisäopetuksenPäättötodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.lukiolainen, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "B")),
      (MockOppijat.lukiolainen, ExamplesLukio.päättötodistus()),
      (MockOppijat.lukioKesken, ExamplesLukio.lukioKesken),
      (MockOppijat.lukionAineopiskelija, ExamplesLukio.aineopiskelija),
      (MockOppijat.lukionAineopiskelijaAktiivinen, ExamplesLukio.aineOpiskelijaAktiivinen),
      (MockOppijat.lukionEiTiedossaAineopiskelija, ExamplesLukio.aineOpiskelijaEiTiedossaOppiaineella),
      (MockOppijat.luva, ExamplesLukioonValmistavaKoulutus.luvaTodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.ammattilainen, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis()),
      (MockOppijat.tutkinnonOsaaPienempiKokonaisuus, TutkinnonOsaaPienempiKokonaisuusExample.opiskeluoikeus),
      (MockOppijat.muuAmmatillinen, MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusOpiskeluoikeus),
      (MockOppijat.muuAmmatillinenKokonaisuuksilla, MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusKokonaisuuksillaOpiskeluoikeus),
      (MockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinen, MuunAmmatillisenKoulutuksenExample.ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeus),
      (MockOppijat.amis, AmmatillinenExampleData.perustutkintoOpiskeluoikeusKesken()),
      (MockOppijat.liiketalous, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 331101, diaariNumero = "59/011/2014")),
      (MockOppijat.valma, ExamplesValma.valmaTodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.telma, ExamplesTelma.telmaTodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.ylioppilasLukiolainen, ExamplesLukio.päättötodistus()),
      (MockOppijat.erikoisammattitutkinto, AmmattitutkintoExample.opiskeluoikeus),
      (MockOppijat.reformitutkinto, ReforminMukainenErikoisammattitutkintoExample.opiskeluoikeus),
      (MockOppijat.osittainenammattitutkinto, AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus),
      (MockOppijat.ammatillisenOsittainenRapsa,AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus.copy(suoritukset = List(AmmatillinenExampleData.ammatillisenTutkinnonOsittainenAutoalanSuoritus))),
      (MockOppijat.paikallinenTunnustettu, AmmatillinenPerustutkintoExample.tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus),
      (MockOppijat.tiedonsiirto, AmmatillinenOpiskeluoikeusTestData.lähdejärjestelmällinenOpiskeluoikeus),
      (MockOppijat.perusopetuksenTiedonsiirto, PerusopetuksenOpiskeluoikeusTestData.lähdejärjestelmällinenOpiskeluoikeus),
      (MockOppijat.omattiedot, PerusopetusExampleData.päättötodistusOpiskeluoikeus(luokka = "D")),
      (MockOppijat.omattiedot, ExamplesLukio.päättötodistus()),
      (MockOppijat.omattiedotSlave.henkilö, AmmatillinenOldExamples.uusi.tallennettavatOpiskeluoikeudet(0)),
      (MockOppijat.ibFinal, ExamplesIB.opiskeluoikeus),
      (MockOppijat.ibPredicted, ExamplesIB.opiskeluoikeusPredictedGrades),
      (MockOppijat.dia, ExamplesDIA.opiskeluoikeus),
      (MockOppijat.eero, AmmatillinenOpiskeluoikeusTestData.mitätöityOpiskeluoikeus),
      (MockOppijat.master, ExamplesPerusopetus.päättötodistus.tallennettavatOpiskeluoikeudet.head),
      (MockOppijat.slave.henkilö, ExamplesLukio.päättötodistus()),
      (MockOppijat.turvakielto, ExamplesLukio.päättötodistus()),
      (MockOppijat.erkkiEiperusteissa, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 334117, diaariNumero = "22/011/2004")),
      (MockOppijat.internationalschool, ExamplesInternationalSchool.opiskeluoikeus),
      (MockOppijat.organisaatioHistoria, AmmatillinenExampleData.opiskeluoikeus()),
      (MockOppijat.montaKoulutuskoodiaAmis, AmmatillinenExampleData.puuteollisuusOpiskeluoikeusKesken()),
      (MockOppijat.tunnisteenKoodiarvoPoistettu, AmmatillinenExampleData.opiskeluoikeus())
    )
  }
}

object AmmatillinenOpiskeluoikeusTestData {
  def opiskeluoikeus(oppilaitosId: String, koulutusKoodi: Int = 351301, diaariNumero: String = "39/011/2014"): AmmatillinenOpiskeluoikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = Koodistokoodiviite(koulutusKoodi.toString, None, "koulutus", None)

    AmmatillinenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      suoritukset = List(AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(koulutusKoodiViite, Some(diaariNumero)),
        toimipiste = oppilaitos,
        suorituskieli = suomenKieli,
        suoritustapa = AmmatillinenExampleData.suoritustapaOps
      )),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2000, 1, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
        hojks = None,
        erityinenTuki = Some(List(Aikajakso(date(2001, 1, 1), None))),
        vaikeastiVammainen = Some(List(Aikajakso(date(2001, 1, 1), None))),
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

object PerusopetuksenOpiskeluoikeusTestData {
  lazy val lähdejärjestelmällinenOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    PerusopetusExampleData.päättötodistusOpiskeluoikeus(oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, None, None)).copy(
      lähdejärjestelmänId = Some(AmmatillinenExampleData.primusLähdejärjestelmäId)
    )
}
