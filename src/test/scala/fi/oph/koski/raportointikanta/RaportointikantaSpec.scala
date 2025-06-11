package fi.oph.koski.raportointikanta

import fi.oph.koski.api.misc.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.db.KoskiTables.{KoskiOpiskeluOikeudet, PoistetutOpiskeluoikeudet, YtrOpiskeluOikeudet}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.ExampleData.{helsinki, suomi}
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData, YleissivistavakoulutusExampleData}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{master, masterEiKoskessa, turvakielto}
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Organisaatiotyyppi}
import fi.oph.koski.raportointikanta.AikajaksoRowBuilder.AikajaksoTyyppi
import fi.oph.koski.raportointikanta.OpiskeluoikeusLoader.isRaportointikantaanSiirrettäväOpiskeluoikeus
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.util.Wait
import fi.oph.koski.ytr.download.YtrDownloadTestMethods
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.{DefaultFormats, JNothing, JString}
import org.json4s.JsonAST.{JBool, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.Assertion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.{Date, Timestamp}
import java.time.LocalDate

class RaportointikantaSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with OpiskeluoikeusTestMethodsAmmatillinen
    with RaportointikantaTestMethods
    with DatabaseTestMethods
    with DirtiesFixtures
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods
    with YtrDownloadTestMethods {
  override implicit val formats = DefaultFormats

  override protected def alterFixture(): Unit = {
    createOrUpdate(KoskiSpecificMockOppijat.slaveMasterEiKoskessa.henkilö, defaultOpiskeluoikeus)
    reloadRaportointikanta()
  }

  "Raportointikanta" - {
    "Opiskeluoikeudet on ladattu" in {
      opiskeluoikeusCount should be > 30
    }
    "Henkilöt on ladattu" in {
      val mockOppija = KoskiSpecificMockOppijat.eero
      henkiloCount should be > 30
      val henkilo = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.filter(_.hetu === mockOppija.hetu.get).result)
      henkilo should equal(Seq(RHenkilöRow(
        mockOppija.oid,
        mockOppija.oid,
        mockOppija.hetu,
        mockOppija.sukupuoli,
        Some(Date.valueOf("1901-01-01")),
        mockOppija.sukunimi,
        mockOppija.etunimet,
        Some("fi"),
        None,
        false,
        None,
        None,
        None,
        true
      )))
    }
    "Vain YTR-datassa esiintyvät henkilöt on ladattu" in {
      val mockHetu = "080380-2432"
      val mockOppija = KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(mockHetu).get
      val expectedHenkilöRow = RHenkilöRow(
        mockOppija.oid,
        mockOppija.oid,
        mockOppija.hetu,
        None,
        Some(Date.valueOf("1980-03-08")),
        mockOppija.sukunimi,
        mockOppija.etunimet,
        Some("fi"),
        None,
        false,
        None,
        None,
        None,
        true
      )
      val henkilo = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.filter(_.hetu === mockHetu).result)
      henkilo should equal(Seq(expectedHenkilöRow))
    }
    "Oppija, jolla on vain mitätöityjä opiskeluoikeuksia, löytyy r_henkilo-taulusta" in {
      val mockHetu = "010106A492V"
      val mockOppija = KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(mockHetu).get
      val expectedHenkilöRow = RHenkilöRow(
        mockOppija.oid,
        mockOppija.oid,
        mockOppija.hetu,
        None,
        Some(Date.valueOf("2006-01-01")),
        mockOppija.sukunimi,
        mockOppija.etunimet,
        Some("fi"),
        None,
        false,
        Some("091"),
        Some("Helsinki"),
        Some("Helsingfors"),
        true
      )
      val henkilo = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.filter(_.hetu === mockHetu).result)
      henkilo should equal(Seq(expectedHenkilöRow))
    }
    "Master oidia ei löydy koskesta" in {
      val slaveOppija = KoskiSpecificMockOppijat.slaveMasterEiKoskessa.henkilö
      val henkilot = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.filter(_.hetu === slaveOppija.hetu.get).result).toSet
      henkilot should equal(Set(
        RHenkilöRow(slaveOppija.oid, masterEiKoskessa.oid, masterEiKoskessa.hetu, None, Some(Date.valueOf("1966-03-27")), masterEiKoskessa.sukunimi, masterEiKoskessa.etunimet, None, None, false, Some("179"), Some("Jyväskylä"), Some("Jyväskylä"), true),
        RHenkilöRow(masterEiKoskessa.oid, masterEiKoskessa.oid, masterEiKoskessa.hetu, None, Some(Date.valueOf("1966-03-27")), masterEiKoskessa.sukunimi, masterEiKoskessa.etunimet, None, None, false, Some("179"), Some("Jyväskylä"), Some("Jyväskylä"), true)
      ))
    }
    "Organisaatiot on ladattu" in {
      organisaatioCount should be > 10
      val organisaatio = mainRaportointiDb.runDbSync(mainRaportointiDb.ROrganisaatiot.filter(_.organisaatioOid === MockOrganisaatiot.aapajoenKoulu).result)
      organisaatio should equal(Seq(ROrganisaatioRow(MockOrganisaatiot.aapajoenKoulu, "Aapajoen koulu", "Aapajoen koulu", "OPPILAITOS", Some("11"), Some("04044"), Some("851"), None, Some("1.2.246.562.10.25412665926"), None)))
    }
    "Oppilaitosten opetuskielet on ladattu" in {
      val oppilaitoksetJaKielet = List(
        (MockOrganisaatiot.aapajoenKoulu, "suomi", "finska", "1"),
        (MockOrganisaatiot.yrkehögskolanArcada, "ruotsi", "svenska", "2")
      )
      oppilaitoksetJaKielet.foreach{ case(oppilaitosOid, kieli, kieliSv, kielikoodi) =>
        val oppilaitos = mainRaportointiDb.runDbSync(
          mainRaportointiDb.ROrganisaatiot.filter(_.organisaatioOid === oppilaitosOid).result
        ).head
        mainRaportointiDb.oppilaitoksenKielet(oppilaitos.organisaatioOid).shouldEqual(
          Set(RKoodistoKoodiRow("oppilaitoksenopetuskieli", kielikoodi, kieli, kieliSv))
        )
      }
    }
    "Koodistot on ladattu" in {
      koodistoKoodiCount should be > 500
      val koodi = mainRaportointiDb.runDbSync(mainRaportointiDb.RKoodistoKoodit.filter(_.koodistoUri === "opiskeluoikeudentyyppi").filter(_.koodiarvo === "korkeakoulutus").result)
      koodi should equal(Seq(RKoodistoKoodiRow("opiskeluoikeudentyyppi", "korkeakoulutus", "Korkeakoulutus", "Högskoleutbildning")))
    }
    "Status-rajapinta" in {
      authGet("api/raportointikanta/status") {
        verifyResponseStatusOk()
        JsonMethods.parse(body) \ "public" \ "isComplete" should equal(JBool(true))
      }
    }

    "Peräkkäinen load-kutsu ei tee mitään" in {
      authGet("api/test/raportointikanta/load")(verifyResponseStatusOk())
      Wait.until(isLoading)
      val loadStarted = getLoadStartedTime
      authGet("api/test/raportointikanta/load")(verifyResponseStatusOk())
      loadStarted should equal(getLoadStartedTime)
      Wait.until(loadComplete)
    }

    "Force load" in {
      authGet("api/test/raportointikanta/load?fullReload=true")(verifyResponseStatusOk())
      Wait.until(isLoading)
      val loadStarted = getLoadStartedTime
      authGet("api/test/raportointikanta/load?force=true&fullReload=true")(verifyResponseStatusOk())
      loadStarted before getLoadStartedTime should be(true)

      // Varmista, että raportointikanta ei jää epämääräiseen virhetilaan ennen muita testejä. Ilman sleeppiä
      // näin voi generointivirheiden vuoksi käydä.
      Thread.sleep(5000)
      KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)
    }
  }

  "YTR-opiskeluoikeuksien lataus" - {
    "YTR-opiskeluoikeuksia ei ladata raportointikantaan, jos YTR-lataus ei ole päällä" in {
      val loadResult = KoskiApplicationForTests.raportointikantaService.loadRaportointikanta(force = false, enableYtr = false)
      loadResult should be(true)
      Wait.until { KoskiApplicationForTests.raportointikantaService.isLoadComplete }

      val ytrOotRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.filter(_.koulutusmuoto === "ylioppilastutkinto").result
      )
      ytrOotRaportointikannassa should have length(0)

      val ootRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.result
      )
      ootRaportointikannassa.length should be > 0

      resetFixtures()
    }
    "Opiskeluoikeudet ladataan raportointikantaan" in {
      verifioiYtrOpiskeluoikeudet()
    }
    "Opiskeluoikeudet säilyvät raportointikannassa, kun tehdään inkrementaalinen päivitys" in {
      päivitäRaportointikantaInkrementaalisesti()

      verifioiYtrOpiskeluoikeudet()
    }
    "Organisaatiot ovat linkitetty oikein" in {
      def verifyOrg(parentOid: Option[String], childOid: Option[String]) = {
        (parentOid, childOid) match {
          case (Some(parent), Some(child)) =>
            KoskiApplicationForTests.organisaatioRepository
              .getChildOids(parent)
              .getOrElse(Set.empty) should contain(child)
          case (_, None) =>
            throw new AssertionError("childOid ei voi olla None")
          case (None, _) =>
            throw new AssertionError("parentOid ei voi olla None")
        }
      }

      mainRaportointiDb.runDbSync(mainRaportointiDb.ROrganisaatiot.result)
        .foreach { row: ROrganisaatioRow =>
          val tyypit = row.organisaatiotyypit.split(',')
          if (tyypit.contains(Organisaatiotyyppi.TOIMIPISTE)) {
            verifyOrg(row.oppilaitos, Some(row.organisaatioOid))
            verifyOrg(row.koulutustoimija, row.oppilaitos)
          } else if (
            tyypit.contains(Organisaatiotyyppi.OPPILAITOS) ||
            tyypit.contains(Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA) ||
            tyypit.contains(Organisaatiotyyppi.OPPISOPIMUSTOIMIPISTE)
          ) {
            row.oppilaitos should equal(None)
            verifyOrg(row.koulutustoimija, Some(row.organisaatioOid))
          } else {
            row.oppilaitos should equal(None)
            row.koulutustoimija should equal(None)
          }
        }
    }

    def verifioiYtrOpiskeluoikeudet() = {
      val ytrOotRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.filter(_.koulutusmuoto === "ylioppilastutkinto").sortBy(_.opiskeluoikeusOid).result
      )
      val ytrPäätasonSuorituksetRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RPäätasonSuoritukset.filter(_.suorituksenTyyppi === "ylioppilastutkinto").sortBy(_.päätasonSuoritusId).result
      )
      val ytrTutkintokokonaisuudenSuorituksetRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RYtrTutkintokokonaisuudenSuoritukset.sortBy(_.ytrTutkintokokonaisuudenSuoritusId).result
      )
      val ytrTutkintokerranSuorituksetRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RYtrTutkintokerranSuoritukset.sortBy(_.ytrTutkintokerranSuoritusId).result
      )
      val ytrKokeenSuorituksetRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RYtrKokeenSuoritukset.sortBy(_.ytrKokeenSuoritusId).result
      )
      val ytrTutkintokokonaisuudenKokeenSuorituksetRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RYtrTutkintokokonaisuudenKokeenSuoritukset.sortBy(s => (s.ytrTutkintokokonaisuudenSuoritusId, s.ytrKokeenSuoritusId, s.sisällytetty)).result
      )
      val ytrTutkintokokonaisuudenKokeenSisällytetytSuorituksetRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RYtrTutkintokokonaisuudenKokeenSuoritukset.filter(_.sisällytetty === true).result
      )

      ytrOotRaportointikannassa.length should be >= 4
      ytrPäätasonSuorituksetRaportointikannassa.length should be(ytrOotRaportointikannassa.length)
      ytrTutkintokokonaisuudenSuorituksetRaportointikannassa.length should be >= ytrPäätasonSuorituksetRaportointikannassa.length
      ytrTutkintokerranSuorituksetRaportointikannassa.length should be >= ytrTutkintokokonaisuudenSuorituksetRaportointikannassa.length
      ytrKokeenSuorituksetRaportointikannassa.length should be >= ytrTutkintokerranSuorituksetRaportointikannassa.length
      ytrTutkintokokonaisuudenKokeenSuorituksetRaportointikannassa.length should be(
        ytrKokeenSuorituksetRaportointikannassa.length + ytrTutkintokokonaisuudenKokeenSisällytetytSuorituksetRaportointikannassa.length
      )

      val expectedOo = getYtrOppija(KoskiSpecificMockOppijat.ylioppilasUusiApi.oid, MockUsers.ophkatselija).opiskeluoikeudet(0)
        .asInstanceOf[YlioppilastutkinnonOpiskeluoikeus]
      val expectedPts = expectedOo.suoritukset(0)
      val expectedTutkintokokonaisuudet = expectedOo.lisätiedot.get.tutkintokokonaisuudet.get
      val expectedTutkintokerrat = expectedTutkintokokonaisuudet.flatMap(_.tutkintokerrat).sortBy(_.tutkintokerta.koodiarvo)
      val expectedKokeet = expectedPts.osasuoritukset.get.sortBy(os => os.koulutusmoduuli.tunniste.koodiarvo)
      val expectedAiemminSuoritetutKokeet = expectedTutkintokokonaisuudet.flatMap(_.aiemminSuoritetutKokeet.getOrElse(List.empty))

      val actualOo: ROpiskeluoikeusRow =
        ytrOotRaportointikannassa.filter(_.oppijaOid === KoskiSpecificMockOppijat.ylioppilasUusiApi.oid)(0)
      val actualPts: RPäätasonSuoritusRow =
        ytrPäätasonSuorituksetRaportointikannassa.filter(_.opiskeluoikeusOid === actualOo.opiskeluoikeusOid)(0)
      val actualTutkintokokonaisuudet: Seq[RYtrTutkintokokonaisuudenSuoritusRow] =
        ytrTutkintokokonaisuudenSuorituksetRaportointikannassa.filter(_.opiskeluoikeusOid === actualOo.opiskeluoikeusOid)
      val actualTutkintokerrat: Seq[RYtrTutkintokerranSuoritusRow] =
        ytrTutkintokerranSuorituksetRaportointikannassa.filter(_.opiskeluoikeusOid === actualOo.opiskeluoikeusOid).sortBy(_.tutkintokertaKoodiarvo)
      val actualKokeet: Seq[RYtrKokeenSuoritusRow] =
        ytrKokeenSuorituksetRaportointikannassa.filter(_.päätasonSuoritusId === actualPts.päätasonSuoritusId).sortBy(_.koulutusmoduuliKoodiarvo)
      val actualTutkintokokonaisuudenKokeet: Seq[RYtrTutkintokokonaisuudenKokeenSuoritusRow] =
        ytrTutkintokokonaisuudenKokeenSuorituksetRaportointikannassa.filter(s => actualTutkintokokonaisuudet.map(_.ytrTutkintokokonaisuudenSuoritusId).contains(s.ytrTutkintokokonaisuudenSuoritusId))

      verifyOo(KoskiSpecificMockOppijat.ylioppilasUusiApi, expectedOo, actualOo)
      verifyPts(expectedOo, expectedPts, actualPts)
      verifyTutkintokokonaisuus(expectedOo, expectedTutkintokokonaisuudet, actualPts, actualTutkintokokonaisuudet)
      verifyTutkintokerrat(expectedOo, expectedTutkintokerrat, actualPts, actualTutkintokokonaisuudet, actualTutkintokerrat)
      verifyKokeet(expectedOo, expectedKokeet, expectedAiemminSuoritetutKokeet, actualPts, actualTutkintokokonaisuudet, actualTutkintokerrat, actualKokeet, actualTutkintokokonaisuudenKokeet)
    }

    def verifyOo(expectedOppija: LaajatOppijaHenkilöTiedot, expectedOo: YlioppilastutkinnonOpiskeluoikeus, actualOo: ROpiskeluoikeusRow) = {
      actualOo.opiskeluoikeusOid should equal(expectedOo.oid.get)
      actualOo.alkamispäivä should equal(None)
      actualOo.päättymispäivä should equal(None)
      actualOo.oppijaOid should equal(expectedOppija.oid)
      actualOo.sisältyyOpiskeluoikeuteenOid should equal(None)
      actualOo.koulutusmuoto should equal("ylioppilastutkinto")
      actualOo.koulutustoimijaOid should equal(expectedOo.koulutustoimija.get.oid)
      actualOo.koulutustoimijaNimi should equal(expectedOo.koulutustoimija.get.nimi.get.get("fi"))
      actualOo.koulutustoimijaNimiSv should equal(expectedOo.koulutustoimija.get.nimi.get.get("sv"))
      actualOo.lisätiedotHenkilöstökoulutus should equal(false)
      actualOo.lisätiedotKoulutusvienti should equal(false)
      actualOo.lähdejärjestelmäId should equal(expectedOo.lähdejärjestelmänId.flatMap(_.id))
      actualOo.lähdejärjestelmäKoodiarvo should equal(expectedOo.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo))
      actualOo.oppilaitosOid should equal(expectedOo.oppilaitos.get.oid)
      actualOo.oppilaitosnumero should equal(None)
      actualOo.oppilaitosNimi should equal(expectedOo.oppilaitos.get.nimi.get.get("fi"))
      actualOo.oppilaitosNimiSv should equal(expectedOo.oppilaitos.get.nimi.get.get("sv"))
      actualOo.oppilaitosKotipaikka should equal(None)
      actualOo.oppivelvollisuudenSuorittamiseenKelpaava should equal(false)
      actualOo.tuvaJärjestämislupa should equal(None)
      actualOo.versionumero should equal(expectedOo.versionumero.get)
      actualOo.viimeisinTila should equal(None)

      actualOo.data \ "suoritukset" should equal(JNothing)
      actualOo.data \ "tyyppi" should equal(JNothing)
      actualOo.data \ "oppilaitosSuorituspäivänä" \ "oid" should equal(JString(YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu.oid))
      actualOo.data \ "oppilaitosSuorituspäivänä" \ "nimi" \ "fi" should equal(JString(YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu.nimi.get.get("fi")))
    }

    def verifyPts(expectedOo: YlioppilastutkinnonOpiskeluoikeus, expectedPts: YlioppilastutkinnonSuoritus, actualPts: RPäätasonSuoritusRow) = {
      actualPts.opiskeluoikeusOid should equal(expectedOo.oid.get)
      actualPts.toimipisteOid should equal(expectedPts.toimipiste.oid)
      actualPts.toimipisteNimi should equal(expectedPts.toimipiste.nimi.get.get("fi"))
      actualPts.toimipisteNimiSv should equal(expectedPts.toimipiste.nimi.get.get("sv"))
      actualPts.alkamispäivä should equal(None)
      actualPts.arviointiArvosanaKoodiarvo should equal(None)
      actualPts.arviointiPäivä should equal(None)
      actualPts.arviointiHyväksytty should equal(None)
      actualPts.arviointiArvosanaKoodisto should equal(None)
      actualPts.koulutusmoduuliKoodiarvo should equal(expectedPts.koulutusmoduuli.tunniste.koodiarvo)
      actualPts.koulutusmoduuliKoodisto should equal(Some(expectedPts.koulutusmoduuli.tunniste.koodistoUri))
      actualPts.koulutusmoduuliNimi should equal(Some(expectedPts.koulutusmoduuli.tunniste.nimi.get.get("fi")))
      actualPts.koulutusmoduuliKoulutustyyppi should equal(expectedPts.koulutusmoduuli.koulutustyyppi)
      actualPts.koulutusmoduuliLaajuusYksikkö should equal(expectedPts.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo))
      actualPts.koulutusmoduuliLaajuusArvo should equal(expectedPts.koulutusmoduuli.laajuus.map(_.arvo))
      actualPts.koulutusModuulinLaajuusYksikköNimi should equal(expectedPts.koulutusmoduuli.laajuus.map(_.yksikkö.nimi.get.get("fi")))
      actualPts.toimipisteOid should equal(expectedPts.toimipiste.oid)
      actualPts.toimipisteNimi should equal(expectedPts.toimipiste.nimi.get.get("fi"))
      actualPts.toimipisteNimiSv should equal(expectedPts.toimipiste.nimi.get.get("sv"))
      actualPts.suorituksenTyyppi should equal(expectedPts.tyyppi.koodiarvo)
      actualPts.suorituskieliKoodiarvo should equal(None)
      actualPts.oppimääräKoodiarvo should equal(None)
      actualPts.sisältyyOpiskeluoikeuteenOid should equal(None)
      actualPts.vahvistusPäivä.map(_.toLocalDate) should equal(expectedPts.vahvistus.map(_.päivä))

      actualPts.data \ "pakollisetKokeetSuoritettu" should equal(JBool(expectedPts.pakollisetKokeetSuoritettu))
    }

    def verifyTutkintokokonaisuus(
      expectedOo: YlioppilastutkinnonOpiskeluoikeus,
      expectedTutkintokokonaisuudet: Seq[YlioppilastutkinnonTutkintokokonaisuudenLisätiedot],
      actualPts: RPäätasonSuoritusRow,
      actualTutkintokokonaisuudet: Seq[RYtrTutkintokokonaisuudenSuoritusRow]
    ) = {
      expectedTutkintokokonaisuudet.size shouldBe actualTutkintokokonaisuudet.size
      actualTutkintokokonaisuudet.zip(expectedTutkintokokonaisuudet).foreach{ case(actualTutkintokokonaisuus, expectedTutkintokokonaisuus) =>
        actualTutkintokokonaisuus.opiskeluoikeusOid should equal(expectedOo.oid.get)
        actualTutkintokokonaisuus.päätasonSuoritusId should equal(actualPts.päätasonSuoritusId)
        actualTutkintokokonaisuus.suorituskieliKoodiarvo should equal(expectedTutkintokokonaisuus.suorituskieli.map(_.koodiarvo))
        actualTutkintokokonaisuus.tyyppiKoodiarvo should equal(expectedTutkintokokonaisuus.tyyppi.map(_.koodiarvo))
        actualTutkintokokonaisuus.tilaKoodiarvo should equal(expectedTutkintokokonaisuus.tila.map(_.koodiarvo))
        actualTutkintokokonaisuus.hyväksytystiValmistunutTutkinto should equal(
          expectedTutkintokokonaisuus.tyyppi.zip(expectedTutkintokokonaisuus.tila).headOption match {
            case Some((tyyppi, tila)) => Some(tyyppi.koodiarvo == "candidate" && tila.koodiarvo == "graduated")
            case _ => Some(false)
          }
        )
        actualTutkintokokonaisuus.data \ "tyyppi" \ "koodistoUri" should equal(JString("ytrtutkintokokonaisuudentyyppi"))
      }
    }

    def verifyTutkintokerrat(
      expectedOo: YlioppilastutkinnonOpiskeluoikeus,
      expectedTutkintokerrat: List[YlioppilastutkinnonTutkintokerranLisätiedot],
      actualPts: RPäätasonSuoritusRow,
      actualTutkintokokonaisuudet: Seq[RYtrTutkintokokonaisuudenSuoritusRow],
      actualTutkintokerrat: Seq[RYtrTutkintokerranSuoritusRow]
    )  = {
      actualTutkintokerrat.length should equal(expectedTutkintokerrat.length)
      actualTutkintokerrat.zip(expectedTutkintokerrat).foreach { case (actualTutkintokerta, expectedTutkintokerta) =>
        actualTutkintokerta.opiskeluoikeusOid should equal(expectedOo.oid.get)
        actualTutkintokerta.päätasonSuoritusId should equal(actualPts.päätasonSuoritusId)
        actualTutkintokokonaisuudet.map(_.ytrTutkintokokonaisuudenSuoritusId) should contain(actualTutkintokerta.ytrTutkintokokonaisuudenSuoritusId)
        actualTutkintokerta.tutkintokertaKoodiarvo should equal(expectedTutkintokerta.tutkintokerta.koodiarvo)

        actualTutkintokerta.vuosi should equal(expectedTutkintokerta.tutkintokerta.vuosi)
        actualTutkintokerta.vuodenaikaKoodiarvo should equal(expectedTutkintokerta.tutkintokerta.koodiarvo.takeRight(1))
        actualTutkintokerta.koulutustaustaKoodiarvo should equal(expectedTutkintokerta.koulutustausta.map(_.koodiarvo))
        actualTutkintokerta.oppilaitosOid should equal(expectedTutkintokerta.oppilaitos.map(_.oid))
        actualTutkintokerta.oppilaitosNimi should equal(expectedTutkintokerta.oppilaitos.flatMap(_.nimi.map(_.get("fi"))))
        actualTutkintokerta.oppilaitosNimiSv should equal(expectedTutkintokerta.oppilaitos.flatMap(_.nimi.map(_.get("sv"))))
        actualTutkintokerta.oppilaitosKotipaikka should equal(expectedTutkintokerta.oppilaitos.flatMap(_.kotipaikka.map(_.koodiarvo)))
        actualTutkintokerta.oppilaitosnumero should equal(expectedTutkintokerta.oppilaitos.flatMap(_.oppilaitosnumero.map(_.koodiarvo)))

        actualTutkintokerta.data \ "tutkintokerta" \ "vuodenaika" \ "fi" should equal(JString(expectedTutkintokerta.tutkintokerta.vuodenaika.get("fi")))
      }
    }

    def verifyKokeet(
      expectedOo: YlioppilastutkinnonOpiskeluoikeus,
      expectedKokeet: Seq[YlioppilastutkinnonKokeenSuoritus],
      expectedSisältyvätKokeet: Seq[YlioppilastutkinnonSisältyväKoe],
      actualPts: RPäätasonSuoritusRow,
      actualTutkintokokonaisuudet: Seq[RYtrTutkintokokonaisuudenSuoritusRow],
      actualTutkintokerrat: Seq[RYtrTutkintokerranSuoritusRow],
      actualKokeet: Seq[RYtrKokeenSuoritusRow],
      actualTutkintokokonaisuudenKokeet: Seq[RYtrTutkintokokonaisuudenKokeenSuoritusRow]
    ) = {
      actualKokeet.length should equal(expectedKokeet.length)
      actualKokeet.zip(expectedKokeet).foreach { case (actualKoe, expectedKoe) =>
        val actualTutkintokokonaisuus = actualTutkintokokonaisuudet.find(_.ytrTutkintokokonaisuudenSuoritusId == actualKoe.ytrTutkintokokonaisuudenSuoritusId).get
        val actualTutkintokerta = actualTutkintokerrat.find(_.ytrTutkintokerranSuoritusId == actualKoe.ytrTutkintokerranSuoritusId).get
        val onExpectedSisältyväKoe = expectedSisältyvätKokeet.exists( sisältyväKoe =>
          sisältyväKoe.tutkintokerta.koodiarvo == expectedKoe.tutkintokerta.koodiarvo &&
            sisältyväKoe.koulutusmoduuli.tunniste.koodiarvo == expectedKoe.koulutusmoduuli.tunniste.koodiarvo
        )

        actualKoe.päätasonSuoritusId should equal(actualPts.päätasonSuoritusId)
        actualKoe.ytrTutkintokerranSuoritusId should equal(actualTutkintokerta.ytrTutkintokerranSuoritusId)
        actualKoe.ytrTutkintokokonaisuudenSuoritusId should equal(actualTutkintokokonaisuus.ytrTutkintokokonaisuudenSuoritusId)
        actualKoe.päätasonSuoritusId should equal(actualPts.päätasonSuoritusId)
        actualKoe.opiskeluoikeusOid should equal(expectedOo.oid.get)
        actualKoe.suorituksenTyyppi should equal(expectedKoe.tyyppi.koodiarvo)
        actualKoe.koulutusmoduuliKoodisto should equal(Some(expectedKoe.koulutusmoduuli.tunniste.koodistoUri))
        actualKoe.koulutusmoduuliKoodiarvo should equal(expectedKoe.koulutusmoduuli.tunniste.koodiarvo)
        actualKoe.koulutusmoduuliNimi should equal(Some(expectedKoe.koulutusmoduuli.tunniste.nimi.get.get("fi")))
        actualKoe.arviointiArvosanaKoodiarvo should equal(expectedKoe.arviointi.map(_.last).map(_.arvosana.koodiarvo))
        actualKoe.arviointiArvosanaKoodisto should equal(expectedKoe.arviointi.map(_.last).map(_.arvosana.koodistoUri))
        actualKoe.arviointiHyväksytty should equal(expectedKoe.arviointi.map(_.last).map(_.hyväksytty))
        actualKoe.arviointiPisteet should equal(expectedKoe.arviointi.map(_.last).flatMap(_.pisteet))
        actualKoe.keskeytynyt should equal(expectedKoe.keskeytynyt)
        actualKoe.maksuton should equal(expectedKoe.maksuton)

        actualKoe.data \ "tutkintokerta" \ "vuodenaika" \ "fi" should equal(JString(expectedKoe.tutkintokerta.vuodenaika.get("fi")))

        actualTutkintokokonaisuudenKokeet should contain(RYtrTutkintokokonaisuudenKokeenSuoritusRow(
          ytrTutkintokokonaisuudenSuoritusId = actualTutkintokokonaisuus.ytrTutkintokokonaisuudenSuoritusId,
          ytrKokeenSuoritusId = actualKoe.ytrKokeenSuoritusId,
          ytrTutkintokerranSuoritusId = actualTutkintokerta.ytrTutkintokerranSuoritusId,
          sisällytetty = false
        ))

        // Jos koe on sisältyvä koe, on siitä lisätty rivi myös sisällyttävään tutkintokokonaisuuden suoritukseen linkitettynä
        if(onExpectedSisältyväKoe){
          actualTutkintokokonaisuudet.size shouldBe 2
          actualTutkintokokonaisuudenKokeet should contain(RYtrTutkintokokonaisuudenKokeenSuoritusRow(
            ytrTutkintokokonaisuudenSuoritusId =
              actualTutkintokokonaisuudet
                .find(_.ytrTutkintokokonaisuudenSuoritusId != actualKoe.ytrTutkintokokonaisuudenSuoritusId)
                .get
                .ytrTutkintokokonaisuudenSuoritusId,
            ytrKokeenSuoritusId = actualKoe.ytrKokeenSuoritusId,
            ytrTutkintokerranSuoritusId = actualTutkintokerta.ytrTutkintokerranSuoritusId,
            sisällytetty = true
          ))
        }
      }
    }
  }

  "Opiskeluoikeuksien lataus" - {
    implicit val context: ExtractionContext = strictDeserialization

    val ammatillinenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/ammatillinen-perustutkinto_2024-08-13.json")
    val oid = "1.2.246.562.15.123456"
    val ammatillinenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](ammatillinenJson).right.get.opiskeluoikeudet.head.asInstanceOf[AmmatillinenOpiskeluoikeus].copy(oid = Some(oid))
    val perusopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/perusopetuksenoppimaara-paattotodistus_2021-12-21.json")
    val perusopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](perusopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[PerusopetuksenOpiskeluoikeus].copy(oid = Some(oid))
    val esiopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/esiopetusvalmis_2022-09-26.json")
    val esiopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](esiopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[EsiopetuksenOpiskeluoikeus].copy(oid = Some(oid))
    val lukionJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/lukio-paattotodistus_2022-09-22.json")
    val lukionOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](lukionJson).right.get.opiskeluoikeudet.head.asInstanceOf[LukionOpiskeluoikeus].copy(oid = Some(oid))
    val vstJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/vapaasivistystyo-oppivelvollisillesuunnattukoulutus_2021-07-27.json")
    val vstOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](vstJson).right.get.opiskeluoikeudet.head.asInstanceOf[VapaanSivistystyönOpiskeluoikeus].copy(oid = Some(oid))

    val Läsnä = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
    val Loma =  Koodistokoodiviite("loma", "koskiopiskeluoikeudentila")
    val Valmistunut = Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")

    "Aikajaksorivien rakennus" - {
      "Opiskeluoikeusjaksot, kesken" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
        ))
      }
      "Opiskeluoikeusjaksot, päättynyt" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 6, 1), tila = Loma),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 9, 1), tila = Läsnä, opintojenRahoitus = Some(Koodistokoodiviite("2", "opintojenrahoitus"))),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 12, 16), tila = Valmistunut)
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-05-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-06-01"), Date.valueOf("2016-08-31"), "loma", Date.valueOf("2016-06-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-09-01"), Date.valueOf("2016-12-15"), "lasna", Date.valueOf("2016-09-01"), opintojenRahoitus = Some("2")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-12-16"), Date.valueOf("2016-12-16"), "valmistunut", Date.valueOf("2016-12-16"), opiskeluoikeusPäättynyt = true)
        ))
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, yksinkertainen tapaus" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            erityinenTuki = Some(List(Aikajakso(LocalDate.of(2016, 2, 1), Some(LocalDate.of(2016, 2, 28)))))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-28"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-29"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
        ))
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, monimutkainen 1" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            erityinenTuki = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 1), Some(LocalDate.of(2016, 2, 10))),
              Aikajakso(LocalDate.of(2016, 4, 1), Some(LocalDate.of(2016, 4, 20)))
            )),
            osaAikaisuusjaksot = Some(List(
              OsaAikaisuusJakso(LocalDate.of(2016, 3, 1), Some(LocalDate.of(2016, 3, 31)), 80)
            )),
            vankilaopetuksessa = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 5), Some(LocalDate.of(2016, 4, 10)))
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-04"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-05"), Date.valueOf("2016-02-10"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true, vankilaopetuksessa = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-11"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), vankilaopetuksessa = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", Date.valueOf("2016-01-15"), vankilaopetuksessa = true, osaAikaisuus = 80),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf("2016-04-10"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true, vankilaopetuksessa = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-11"), Date.valueOf("2016-04-20"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-21"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
        ))
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, monimutkainen 2" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            opiskeluvalmiuksiaTukevatOpinnot = Some(List(
              OpiskeluvalmiuksiaTukevienOpintojenJakso(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 29), LocalizedString.finnish("Kuvaus"))
            )),
            vaikeastiVammainen = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 29), Some(LocalDate.of(2016, 3, 31)))
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-28"), "lasna", Date.valueOf("2016-01-15"), opiskeluvalmiuksiaTukevatOpinnot = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-29"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), opiskeluvalmiuksiaTukevatOpinnot = true, vaikeastiVammainen = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", Date.valueOf("2016-01-15"), vaikeastiVammainen = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
        ))
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, majoitustiedot" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            majoitus = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 1), Some(LocalDate.of(2016, 2, 29)))
            )),
            sisäoppilaitosmainenMajoitus = Some(List(
              Aikajakso(LocalDate.of(2016, 3, 1), Some(LocalDate.of(2016, 3, 31)))
            )),
            vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = Some(List(
              Aikajakso(LocalDate.of(2016, 4, 1), Some(LocalDate.of(2016, 4, 30)))
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), majoitus = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", Date.valueOf("2016-01-15"), sisäoppilaitosmainenMajoitus = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf("2016-04-30"), "lasna", Date.valueOf("2016-01-15"), vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-05-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
        ))
      }
      "Ammatillisen opiskeluoikeuden järjestämismuodot" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy()
        val aikajaksoRows = AikajaksoRowBuilder.buildAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows.length should equal(1)
        aikajaksoRows should equal(
          Seq(
            RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow(
              "1.2.246.562.15.123456",
              "Oppilaitosmuotoinen",
              Date.valueOf("2013-09-01"),
              None,
              None,
              None,
              None
            )
          )
        )
      }
      "Ulkomaanjaksot" in {
        val alku = LocalDate.parse("2022-01-01")
        val loppu = LocalDate.parse("2023-01-01")
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(lisätiedot = Some(
          AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = Some(Hojks(Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila"), None, None)),
            ulkomaanjaksot = Some(List(Ulkomaanjakso(alku = alku, loppu = Some(loppu), maa = ExampleData.suomi, kuvaus = LocalizedString.finnish("Foo bar"))))
          )
        ))
        val aikajaksoRows = AikajaksoRowBuilder.buildAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows.length should equal(1)
        aikajaksoRows should equal(
          Seq(RAikajaksoRow(opiskeluoikeus.oid.get, AikajaksoTyyppi.ulkomaanjakso, Date.valueOf(alku), Some(Date.valueOf(loppu))))
        )
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, hojks" - {
        "Ei alku/loppupäivää" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
            tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
              AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
            )),
            lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
              hojks = Some(Hojks(Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila"), None, None))
            ))
          )
          val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), hojks = true)
          ))
        }
        "Alku/loppupäivä" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
            tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
              AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
            )),
            lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
              hojks = Some(Hojks(Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila"), Some(LocalDate.of(2016, 2, 1)), Some(LocalDate.of(2016, 2, 29))))
            ))
          )
          val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), hojks = true),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
          ))
        }
      }
      "Oppisopimus" - {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus]
        val oppisopimus = Oppisopimus(Yritys(LocalizedString.finnish("Autokorjaamo Oy"), "1234567-8"))
        val järjestämismuoto = OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", Some(LocalizedString.finnish("jarjestamismuoto_20")), "jarjestamismuoto"), oppisopimus)
        val osaamisenHankkimistapa = OppisopimuksellinenOsaamisenHankkimistapa(Koodistokoodiviite("oppisopimus", "osaamisenhankkimistapa"), oppisopimus)

        "järjestämismuodot-kentässä" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
            tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
              AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
            )),
            suoritukset = List(
              suoritus.copy(
                järjestämismuodot = Some(List(
                  Järjestämismuotojakso(LocalDate.of(2016, 2, 1), None, järjestämismuoto)
                )),
                osaamisenHankkimistavat = None
              )
            )
          )

          val opiskeluoikeusAikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          opiskeluoikeusAikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), oppisopimusJossainPäätasonSuorituksessa = true)
          ))

          val ammatillinenAikajaksoRows = AikajaksoRowBuilder.buildAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows(oid, opiskeluoikeus)
          ammatillinenAikajaksoRows should equal(
            Seq(
              RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow(oid, "jarjestamismuoto_20", Date.valueOf("2016-02-01"), None, Some("1234567-8"), None, None)
            )
          )

          val osaamisenHankkimistapaAikajaksoRows = AikajaksoRowBuilder.buildOsaamisenHankkimistapaAikajaksoRows(oid, opiskeluoikeus)
          osaamisenHankkimistapaAikajaksoRows should equal(
            Seq()
          )
        }
        "osaamisenHankkimistavat-kentässä" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
            tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
              AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
            )),
            suoritukset = List(
              suoritus.copy(
                järjestämismuodot = None,
                osaamisenHankkimistavat = Some(List(
                  OsaamisenHankkimistapajakso(LocalDate.of(2001, 1, 1), Some(LocalDate.of(2016, 1, 31)), osaamisenHankkimistapa)
                ))
              )
            )
          )
          val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15"), oppisopimusJossainPäätasonSuorituksessa = true),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
          ))

          val osaamisenHankkimistapaRows = AikajaksoRowBuilder.buildOsaamisenHankkimistapaAikajaksoRows(oid, opiskeluoikeus)
          osaamisenHankkimistapaRows should equal(
            Seq(
              ROsaamisenhankkimistapaAikajaksoRow(oid,"oppisopimus", Date.valueOf("2001-01-01"), Some(Date.valueOf("2016-01-31")), Some("1234567-8"),None,None,None,None,None)
            )
          )
        }

      }
      "Oppilaitosmuotoinen koulutus" - {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus]
        val osaamisenHankkimistapaIlmanLisätietoja = OsaamisenHankkimistapaIlmanLisätietoja(Koodistokoodiviite("oppilaitosmuotoinenkoulutus", "osaamisenhankkimistapa"))

        "osaamisenHankkimistavat-kentässä" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
            tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
              AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
            )),
            suoritukset = List(
              suoritus.copy(
                järjestämismuodot = None,
                osaamisenHankkimistavat = Some(List(
                  OsaamisenHankkimistapajakso(LocalDate.of(2001, 1, 1), Some(LocalDate.of(2016, 1, 31)), osaamisenHankkimistapaIlmanLisätietoja)
                ))
              )
            )
          )

          val osaamisenHankkimistapaRows = AikajaksoRowBuilder.buildOsaamisenHankkimistapaAikajaksoRows(oid, opiskeluoikeus)
          osaamisenHankkimistapaRows should equal(
            Seq(
              ROsaamisenhankkimistapaAikajaksoRow(oid, "oppilaitosmuotoinenkoulutus", Date.valueOf("2001-01-01"), Some(Date.valueOf("2016-01-31")), None, None, None, None, None, None)
            )
          )
        }
      }
      "Koulutussopimus" - {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus]
        val koulutussopimusjakso = Koulutussopimusjakso(
          alku = LocalDate.of(2020, 1, 1),
          loppu = None,
          työssäoppimispaikka = Some(LocalizedString.finnish("Maansiirto")),
          työssäoppimispaikanYTunnus = Some("1234567-8"), paikkakunta = helsinki, maa = suomi, työtehtävät = None
        )

        "koulutussopimukset-kentässä" in {
          val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
            tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
              AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
            )),
            suoritukset = List(
              suoritus.copy(
                järjestämismuodot = None,
                osaamisenHankkimistavat = None,
                koulutussopimukset = Some(List(koulutussopimusjakso))
              )
            )
          )

          val osaamisenHankkimistapaAikajaksoRows = AikajaksoRowBuilder.buildOsaamisenHankkimistapaAikajaksoRows(oid, opiskeluoikeus)
          osaamisenHankkimistapaAikajaksoRows should equal(
            Seq(
              ROsaamisenhankkimistapaAikajaksoRow(oid, "koulutussopimus", Date.valueOf("2020-01-01"), None, None, None, None, Some("091"), Some("246"), Some("1234567-8"))
            )
          )
        }
      }
      "Perusopetuksen opiskeluoikeuden lisätiedot" in {
        val opiskeluoikeus = perusopetuksenOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.of(2017, 1, 1), tila = Läsnä)
          )),
          lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
            vaikeastiVammainen = Some(List(
              Aikajakso(LocalDate.of(2017, 1, 1), Some(LocalDate.of(2017, 3, 31)))
            )),
            tuenPäätöksenJaksot = Some(List(
              Tukijakso(Some(LocalDate.of(2017, 2, 28)), Some(LocalDate.of(2017, 4, 30)))
            )),
            opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = Some(List(
              Aikajakso(LocalDate.of(2017, 3, 15), Some(LocalDate.of(2017, 4, 30)))
            )),
            toimintaAlueittainOpiskelu = Some(List(
              Aikajakso(LocalDate.of(2017, 3, 15), Some(LocalDate.of(2017, 4, 30)))
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-01-01"), Date.valueOf("2017-02-27"), "lasna", Date.valueOf("2017-01-01"), vaikeastiVammainen = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-02-28"), Date.valueOf("2017-03-14"), "lasna", Date.valueOf("2017-01-01"), vaikeastiVammainen = true, tuenPäätöksenJakso = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-03-15"), Date.valueOf("2017-03-31"), "lasna", Date.valueOf("2017-01-01"),
            vaikeastiVammainen = true, tuenPäätöksenJakso = true, opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = true, toimintaAlueittainOpiskelu = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-04-01"), Date.valueOf("2017-04-30"), "lasna", Date.valueOf("2017-01-01"),
            tuenPäätöksenJakso = true, opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = true, toimintaAlueittainOpiskelu = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-05-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2017-01-01"))
        ))
      }
      "Erityisen koulutustehtävän jakso" in {
        val opiskeluoikeus = lukionOpiskeluoikeus
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)

        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2012-09-01"), Date.valueOf("2013-09-01"), "lasna", Date.valueOf("2012-09-01"), erityisenKoulutusTehtävänJaksoTehtäväKoodiarvo = Some("103"), opintojenRahoitus = Some("1"), sisäoppilaitosmainenMajoitus = true, ulkomaanjakso = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2013-09-02"), Date.valueOf("2016-08-07"), "lasna", Date.valueOf("2012-09-01"), opintojenRahoitus = Some("1")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-08-08"), Date.valueOf("2016-08-08"), "valmistunut", Date.valueOf("2016-08-08"), opintojenRahoitus = Some("1"), opiskeluoikeusPäättynyt = true)
        ))
      }
      "Esiopetuksen opiskeluoikeuden lisätiedot" in {
        val aikajakso = Aikajakso(LocalDate.of(2000, 1, 1), Some(LocalDate.of(2000, 2, 2)))
        val erityisenTuenPäätös = ErityisenTuenPäätös(
          alku = Some(LocalDate.of(2000, 1, 1)),
          loppu = Some(LocalDate.of(2000, 2, 2)),
          opiskeleeToimintaAlueittain = false,
          erityisryhmässä = None
        )
        val opiskeluoikeus = esiopetuksenOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.of(2000, 1, 1), tila = Läsnä)
          )),
          lisätiedot = Some(EsiopetuksenOpiskeluoikeudenLisätiedot(
            pidennettyOppivelvollisuus = Some(aikajakso),
            tukimuodot = Some(List(Koodistokoodiviite("1", Some(Finnish("Osa-aikainen erityisopetus")), "perusopetuksentukimuoto"), Koodistokoodiviite("2", Some(Finnish("Erityisopetus")), "perusopetuksentukimuoto"))),
            erityisenTuenPäätös = Some(erityisenTuenPäätös),
            erityisenTuenPäätökset = Some(List(erityisenTuenPäätös.copy(opiskeleeToimintaAlueittain = true, erityisryhmässä = Some(true), alku = Some(LocalDate.of(2000, 2, 2)), loppu = Some(LocalDate.of(2000, 3, 3))))),
            vammainen = Some(List(aikajakso)),
            vaikeastiVammainen = Some(List(aikajakso)),
            majoitusetu = Some(Aikajakso(LocalDate.of(2000, 3, 3), None)),
            kuljetusetu = Some(aikajakso),
            sisäoppilaitosmainenMajoitus = Some(List(aikajakso, Aikajakso(LocalDate.of(2000, 3, 3), Some(LocalDate.of(2000, 4, 4))))),
            koulukoti = Some(List(aikajakso))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildEsiopetusOpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          EsiopetusOpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2000-01-01"), Date.valueOf("2000-02-01"), "lasna", Date.valueOf("2000-01-01"), tukimuodot = Some("1;2"),pidennettyOppivelvollisuus = true, erityisenTuenPäätös = true, vammainen = true, vaikeastiVammainen = true, kuljetusetu = true, koulukoti = true, sisäoppilaitosmainenMajoitus = true),
          EsiopetusOpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2000-02-02"), Date.valueOf("2000-02-02"), "lasna", Date.valueOf("2000-01-01"), tukimuodot = Some("1;2"), pidennettyOppivelvollisuus = true, erityisenTuenPäätös = true, vammainen = true, vaikeastiVammainen = true, kuljetusetu = true, koulukoti = true, erityisenTuenPäätösOpiskeleeToimintaAlueittain = true, erityisenTuenPäätösErityisryhmässä = true, sisäoppilaitosmainenMajoitus = true),
          EsiopetusOpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2000-02-03"), Date.valueOf("2000-03-02"), "lasna", Date.valueOf("2000-01-01"), tukimuodot = Some("1;2"), erityisenTuenPäätös = true, erityisenTuenPäätösOpiskeleeToimintaAlueittain = true, erityisenTuenPäätösErityisryhmässä = true),
          EsiopetusOpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2000-03-03"), Date.valueOf("2000-03-03"), "lasna", Date.valueOf("2000-01-01"), tukimuodot = Some("1;2"), erityisenTuenPäätös = true, erityisenTuenPäätösOpiskeleeToimintaAlueittain = true, erityisenTuenPäätösErityisryhmässä = true, majoitusetu = true, sisäoppilaitosmainenMajoitus = true),
          EsiopetusOpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2000-03-04"), Date.valueOf("2000-04-04"), "lasna", Date.valueOf("2000-01-01"), tukimuodot = Some("1;2"), majoitusetu = true, sisäoppilaitosmainenMajoitus = true),
          EsiopetusOpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2000-04-05"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2000-01-01"), tukimuodot = Some("1;2"), majoitusetu = true)
        ))
      }
      "Aikajaksot rajataan opiskeluoikeuden alku/loppupäivän väliin" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 12, 16), tila = Valmistunut)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            erityinenTuki = Some(List(
              Aikajakso(LocalDate.of(2016, 1, 1), Some(LocalDate.of(2016, 12, 15)))
            )),
            osaAikaisuusjaksot = Some(List(
              OsaAikaisuusJakso(LocalDate.of(2016, 3, 1), Some(LocalDate.of(2016, 12, 31)), 80)
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-12-15"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = true, osaAikaisuus = 80),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-12-16"), Date.valueOf("2016-12-16"), "valmistunut", Date.valueOf("2016-12-16"), osaAikaisuus = 80, opiskeluoikeusPäättynyt = true)
        ))
      }
      "Maksuttomuus, maksullisuus ja oikeutta maksuttomuuteen pidennetty" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä),
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            maksuttomuus = Some(List(
              Maksuttomuus(LocalDate.of(2016, 1, 15), Some(LocalDate.of(2017, 1, 14)), true),
              Maksuttomuus(LocalDate.of(2017, 1, 15), Some(LocalDate.of(2018, 1, 14)), false),
              Maksuttomuus(LocalDate.of(2018, 1, 15), None, true)
            )),
            oikeuttaMaksuttomuuteenPidennetty = Some(List(
              OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2018, 1, 15), LocalDate.of(2019, 1, 15))
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2017-01-14"), "lasna", Date.valueOf("2016-01-15"), maksuton = Some(true), maksullinen = Some(false), oikeuttaMaksuttomuuteenPidennetty = false),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-01-15"), Date.valueOf("2018-01-14"), "lasna", Date.valueOf("2016-01-15"), maksuton = Some(false), maksullinen = Some(true), oikeuttaMaksuttomuuteenPidennetty = false),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2018-01-15"), Date.valueOf("2019-01-15"), "lasna", Date.valueOf("2016-01-15"), maksuton = Some(true), maksullinen = Some(false), oikeuttaMaksuttomuuteenPidennetty = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2019-01-16"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), maksuton = Some(true), maksullinen = Some(false), oikeuttaMaksuttomuuteenPidennetty = false),
        ))
      }
      "Vapaan sivistystyön opiskeluoikeuden lisätiedot" in {
        val opiskeluoikeus = vstOpiskeluoikeus.copy(
          tila = VapaanSivistystyönOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(
            VapaanSivistystyönOpiskeluoikeudenLisätiedot(
              maksuttomuus = Some(List(Maksuttomuus(LocalDate.of(2017, 1, 15), Some(LocalDate.of(2018, 1, 15)), maksuton = true)))
            )
          )
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)

        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2017-01-14"), "lasna", Date.valueOf("2016-01-15"), maksuton = None, maksullinen = None),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-01-15"), Date.valueOf("2018-01-15"), "lasna", Date.valueOf("2016-01-15"), maksuton = Some(true), maksullinen = Some(false)),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2018-01-16"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), maksuton = None, maksullinen = None)
        ))
      }
    }

    "Suoritusrivien rakennus" - {
      "Päätason suorituksen toimipiste haetaan oikein" in {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus].copy(
          toimipiste = AmmatillinenExampleData.stadinToimipiste,
          osasuoritukset = None
        )
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          suoritukset = List(suoritus)
        )
        val (ps, _, _, _) = OpiskeluoikeusLoaderRowBuilder.buildKoskiSuoritusRows(oid, None, opiskeluoikeus.oppilaitos.get, opiskeluoikeus.suoritukset.head, JObject(), 1)
        ps.toimipisteOid should equal(AmmatillinenExampleData.stadinToimipiste.oid)
        ps.toimipisteNimi should equal(AmmatillinenExampleData.stadinToimipiste.nimi.get.get("fi"))
      }

      "Päätason suorituksen tutkintonimike haetaan oikein" in {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus].copy(
          toimipiste = AmmatillinenExampleData.stadinToimipiste,
          osasuoritukset = None
        )
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          suoritukset = List(suoritus)
        )
        val (ps, _, _, _) = OpiskeluoikeusLoaderRowBuilder.buildKoskiSuoritusRows(oid, None, opiskeluoikeus.oppilaitos.get, opiskeluoikeus.suoritukset.head, JObject(), 1)
        ps.tutkintonimike should equal(Some("Ympäristönhoitaja"))
      }

      "Päätason suorituksen luokka ja ryhmä haetaan oikein" in {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus].copy(
          toimipiste = AmmatillinenExampleData.stadinToimipiste,
          osasuoritukset = None
        )
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          suoritukset = List(suoritus)
        )
        val (ps, _, _, _) = OpiskeluoikeusLoaderRowBuilder.buildKoskiSuoritusRows(oid, None, opiskeluoikeus.oppilaitos.get, opiskeluoikeus.suoritukset.head, JObject(), 1)
        ps.luokkaTaiRyhmä should equal(Some("YMP14SN"))
      }


      "Päätason suorituksella on alkamispäivä" in {
        val suoritus = ammatillinenOpiskeluoikeus.suoritukset.head.asInstanceOf[AmmatillisenTutkinnonSuoritus].copy(
          osasuoritukset = None,
          alkamispäivä = Some(LocalDate.of(2016, 1, 1))
        )
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          suoritukset = List(suoritus)
        )
        val (ps, _, _, _) = OpiskeluoikeusLoaderRowBuilder.buildKoskiSuoritusRows(oid, None, opiskeluoikeus.oppilaitos.get, opiskeluoikeus.suoritukset.head, JObject(), 1)
        ps.alkamispäivä.get should equal(Date.valueOf("2016-1-1"))
      }
    }
  }

  "Mitätöityjen opiskeluoikeuksien lataus" - {
    "Kaikki mitätöidyt poistamattomat, mitätöidyt poistetut sekä peruttujen suostumusten opiskeluoikeudet ladataan erilliseen tauluun" in {
      val mitätöidytPoistamattomatKoskessa = runDbSync(
        KoskiOpiskeluOikeudet.filter(_.mitätöity).filterNot(_.poistettu).sortBy(_.oid).result
      )
      val mitätöidytPoistetutTaiPerututSuostumuksetKoskessa = runDbSync(
        PoistetutOpiskeluoikeudet.result
      )
      val mitätöidytRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.RMitätöidytOpiskeluoikeudet.sortBy(_.opiskeluoikeusOid).result
      )

      val orgs = KoskiApplicationForTests.organisaatioRepository
      val mitätöidytKoskessa =
        mitätöidytPoistamattomatKoskessa.map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity).map(_.right.get) ++
        mitätöidytPoistetutTaiPerututSuostumuksetKoskessa.map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity(orgs)).map(_.right.get)

      mitätöidytKoskessa.distinct.length should equal(mitätöidytKoskessa.length)
      mitätöidytRaportointikannassa.length should equal(mitätöidytKoskessa.length)

      mitätöidytRaportointikannassa.sortBy(_.opiskeluoikeusOid) should equal(
        (mitätöidytKoskessa zip mitätöidytRaportointikannassa)
          .sortBy(_._1.opiskeluoikeusOid)
          .map(t => RMitätöityOpiskeluoikeusRow(
            opiskeluoikeusOid = t._1.opiskeluoikeusOid,
            versionumero = t._1.versionumero,
            aikaleima = t._1.aikaleima,
            oppijaOid = t._1.oppijaOid,
            mitätöity = t._1.mitätöity,
            suostumusPeruttu = t._1.suostumusPeruttu,
            tyyppi = t._1.oppijaOid match {
              case KoskiSpecificMockOppijat.eero.oid => "perusopetus"
              case KoskiSpecificMockOppijat.lukiolainen.oid => "ammatillinenkoulutus"
              case KoskiSpecificMockOppijat.poistettuOpiskeluoikeus.oid => "vapaansivistystyonkoulutus"
              case KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia.oid => "perusopetus"
              case _ => "???"
            },
            päätasonSuoritusTyypit = t._1.oppijaOid match {
              case KoskiSpecificMockOppijat.eero.oid => List("perusopetuksenoppimaara", "perusopetuksenvuosiluokka")
              case KoskiSpecificMockOppijat.lukiolainen.oid => List("ammatillinentutkinto")
              case KoskiSpecificMockOppijat.poistettuOpiskeluoikeus.oid => List("vstvapaatavoitteinenkoulutus")
              case KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia.oid => List("perusopetuksenoppimaara", "perusopetuksenvuosiluokka")
              case _ => List("???")
            },
            oppilaitosOid = t._1.oppilaitosOid,
            oppilaitoksenNimi = t._1.oppilaitoksenNimi,
            koulutustoimijaOid = t._1.koulutustoimijaOid,
            koulutustoimijanNimi = t._1.koulutustoimijanNimi,
          ))
      )
    }

    "Mitätöityjä opiskeluoikeuksia ei ladata varsinaiseen opiskeluoikeudet-tauluun" in {
      val mitätöidytOpiskeluoikeusOidit = runDbSync(
        KoskiOpiskeluOikeudet.filter(_.mitätöity).sortBy(_.id).result
      ).map(_.oid)

      val opiskeluoikeusOiditRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.map(_.opiskeluoikeusOid).result
      )

      opiskeluoikeusOiditRaportointikannassa.exists(mitätöidytOpiskeluoikeusOidit.contains) should be(false)
    }

    "Poistettuja opiskeluoikeuksia ei ladata varsinaiseen opiskeluoikeudet-tauluun" in {
      val mitätöidytPoistetutTaiPerututSuostumuksetOpiskeluoikeusOidit = runDbSync(
        PoistetutOpiskeluoikeudet.result
      ).map(_.oid)

      val opiskeluoikeusOiditRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.map(_.opiskeluoikeusOid).result
      )

      opiskeluoikeusOiditRaportointikannassa
        .exists(mitätöidytPoistetutTaiPerututSuostumuksetOpiskeluoikeusOidit.contains) should be(false)
    }

    "Jo ladatun opiskeluoikeuden mitätöinti kesken latauksen ei vaikuta lopputulokseen" in {
      KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)

      val alkuperäinenOpiskeluoikeusCount = opiskeluoikeusCount

      val ensimmäinenMitätöimätönOpiskeluoikeusOidIdJärjestyksessä: String = ensimmäinenMitätöitävissäolevaOpiskeluoikeusIdJärjestyksessä.oid

      val loadResult = KoskiApplicationForTests.raportointikantaService.loadRaportointikanta(force = false, pageSize = 10, onAfterPage = (page, batch) => {
        if (page == 0) {
          // Varmista, että mitätöitävä opiskeluoikeus oli tällä sivulla
          batch.exists(_.oid == ensimmäinenMitätöimätönOpiskeluoikeusOidIdJärjestyksessä) should be(true)

          mitätöiOpiskeluoikeus(ensimmäinenMitätöimätönOpiskeluoikeusOidIdJärjestyksessä, MockUsers.paakayttaja)
        }
      })
      loadResult should be(true)
      Wait.until(isLoading)
      Wait.until(loadComplete)

      opiskeluoikeusCount should be(alkuperäinenOpiskeluoikeusCount)

      val opiskeluoikeusOiditRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.map(_.opiskeluoikeusOid).result
      )

      opiskeluoikeusOiditRaportointikannassa should contain(ensimmäinenMitätöimätönOpiskeluoikeusOidIdJärjestyksessä)
    }

    "Jo ladatun opiskeluoikeuden poisto kesken latauksen ei vaikuta lopputulokseen" in {
      KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)

      val alkuperäinenOpiskeluoikeusCount = opiskeluoikeusCount

      val ensimmäinenVSTVapaatavoitteinenOpiskeluoikeusIdJärjestyksessä = ensimmäinenPoistettavissaolevaOpiskeluoikeusIdJärjestyksessä

      val poistettavaOpiskeluoikeusOid = ensimmäinenVSTVapaatavoitteinenOpiskeluoikeusIdJärjestyksessä.oid
      val poistettavaOppijaOid = ensimmäinenVSTVapaatavoitteinenOpiskeluoikeusIdJärjestyksessä.oppijaOid

      var poistettavanVSTnSivu = -2
      var käytiinSeuraavallaDataaSisältävälläSivulla = false

      val loadResult = KoskiApplicationForTests.raportointikantaService.loadRaportointikanta(force = false, pageSize = 10, onAfterPage = (page, batch) => {
          if (batch.exists(_.oid == poistettavaOpiskeluoikeusOid)) {
            poistettavanVSTnSivu = page

            poistaOpiskeluoikeus(poistettavaOppijaOid, poistettavaOpiskeluoikeusOid)
          }

          if (page == (poistettavanVSTnSivu + 1) && batch.length > 0) {
            käytiinSeuraavallaDataaSisältävälläSivulla = true
          }
      })
      loadResult should be(true)

      Wait.until(isLoading)
      Wait.until(loadComplete)

      käytiinSeuraavallaDataaSisältävälläSivulla should be(true)

      opiskeluoikeusCount should be(alkuperäinenOpiskeluoikeusCount)

      val opiskeluoikeusOiditRaportointikannassa = mainRaportointiDb.runDbSync(
        mainRaportointiDb.ROpiskeluoikeudet.map(_.opiskeluoikeusOid).result
      )

      opiskeluoikeusOiditRaportointikannassa should contain(ensimmäinenVSTVapaatavoitteinenOpiskeluoikeusIdJärjestyksessä.oid)
    }

    "Mitätöity opiskeluoikeus päivittyy oikein inkrementaalisessa päivityksessä" in {
      KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)

      val alkuperäinenOpiskeluoikeusCount = opiskeluoikeusCount
      val alkuperäinenMitätöityOpiskeluoikeusCount = mitätöityOpiskeluoikeusCount

      mitätöiOpiskeluoikeus(ensimmäinenMitätöitävissäolevaOpiskeluoikeusIdJärjestyksessä.oid)

      päivitäRaportointikantaInkrementaalisesti()

      opiskeluoikeusCount should be(alkuperäinenOpiskeluoikeusCount - 1)
      mitätöityOpiskeluoikeusCount should be (alkuperäinenMitätöityOpiskeluoikeusCount + 1)
    }

    "Poistettu opiskeluoikeus päivittyy oikein inkrementaalisessa päivityksessä" in {
      KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)

      val alkuperäinenOpiskeluoikeusCount = opiskeluoikeusCount
      val alkuperäinenMitätöityOpiskeluoikeusCount = mitätöityOpiskeluoikeusCount

      val poistettava = ensimmäinenPoistettavissaolevaOpiskeluoikeusIdJärjestyksessä
      poistaOpiskeluoikeus(poistettava.oppijaOid, poistettava.oid)

      päivitäRaportointikantaInkrementaalisesti()

      opiskeluoikeusCount should be(alkuperäinenOpiskeluoikeusCount - 1)
      mitätöityOpiskeluoikeusCount should be (alkuperäinenMitätöityOpiskeluoikeusCount + 1)
    }

    "Mitätöinnin peruutus päivittyy oikein inkrementaalisessa päivityksessä" in {
      KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)
      val opiskeluoikeus = ensimmäinenMitätöitävissäolevaOpiskeluoikeusIdJärjestyksessä

      mitätöiOpiskeluoikeus(opiskeluoikeus.oid)
      päivitäRaportointikantaInkrementaalisesti()

      päivitäOpiskeluoikeus(opiskeluoikeus) // Palauta alkuperäiseen tilaan

      val alkuperäinenOpiskeluoikeusCount = opiskeluoikeusCount
      val alkuperäinenMitätöityOpiskeluoikeusCount = mitätöityOpiskeluoikeusCount

      päivitäRaportointikantaInkrementaalisesti()

      opiskeluoikeusCount should be(alkuperäinenOpiskeluoikeusCount + 1)
      mitätöityOpiskeluoikeusCount should be (alkuperäinenMitätöityOpiskeluoikeusCount - 1)
    }
  }

  "Inkrementaalinen lataus muutetaan täyslataukseksi jos päivitettäviä rivien määrä ylittää raja-arvon" in {
    KoskiApplicationForTests.fixtureCreator.resetFixtures(reloadRaportointikanta = true, reloadYtrData = true)
    val kaikkiOpiskeluoikeudet = runDbSync(KoskiOpiskeluOikeudet.filter(_.mitätöity === false).result).filter(isRaportointikantaanSiirrettäväOpiskeluoikeus)
    val kaikkiYtrOpiskeluoikeudet = runDbSync(YtrOpiskeluOikeudet.result)
    val incrementalLoadMaxRows = 50

    kaikkiOpiskeluoikeudet.size > incrementalLoadMaxRows shouldBe true
    kaikkiOpiskeluoikeudet
      .take(incrementalLoadMaxRows)
      .foreach(oo => KoskiApplicationForTests.päivitetytOpiskeluoikeudetJono.lisää(oo.oid))

    päivitäRaportointikantaInkrementaalisesti(incrementalLoadMaxRows)
    val päivitetytRivit = mainRaportointiDb.runDbSync(
      mainRaportointiDb.RaportointikantaStatus.filter(_.name === "opiskeluoikeudet").map(_.count).result
    ).head

    päivitetytRivit shouldBe kaikkiOpiskeluoikeudet.size + kaikkiYtrOpiskeluoikeudet.size
  }

  private def opiskeluoikeusCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.ROpiskeluoikeudet.length.result)
  private def mitätöityOpiskeluoikeusCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.RMitätöidytOpiskeluoikeudet.length.result)
  private def henkiloCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.length.result)
  private def organisaatioCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.ROrganisaatiot.length.result)
  private def koodistoKoodiCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.RKoodistoKoodit.length.result)

  private def isLoading = authGet("api/raportointikanta/status") {
    (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
  }

  private def getLoadStartedTime: Timestamp = authGet("api/raportointikanta/status") {
    JsonSerializer.extract[Timestamp](JsonMethods.parse(body) \ "etl" \ "startedTime")
  }

  def päivitäRaportointikantaInkrementaalisesti(incrementalLoadMaxRows: Int = 50000) = {
    val loadResult = KoskiApplicationForTests.raportointikantaService.loadRaportointikanta(
      force = false,
      skipUnchangedData = true,
      incrementalLoadMaxRows = incrementalLoadMaxRows
    )
    loadResult should be(true)
    Wait.until(isLoading)
    Wait.until(loadComplete)

    withClue("Päivitysjono on inkrementaalisen päivityksen jälkeen tyhjä") {
      KoskiApplicationForTests.päivitetytOpiskeluoikeudetJono.kaikki.isEmpty should equal(true)
    }
  }
}

