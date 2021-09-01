package fi.oph.koski.raportointikanta

import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.db.KoskiTables.OpiskeluOikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{master, masterEiKoskessa}
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.util.Wait
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.JsonAST.{JBool, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

import java.sql.{Date, Timestamp}
import java.time.LocalDate

class RaportointikantaSpec
  extends FreeSpec
    with KoskiHttpSpec
    with Matchers
    with OpiskeluoikeusTestMethodsAmmatillinen
    with RaportointikantaTestMethods
    with DatabaseTestMethods
    with DirtiesFixtures {

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
        mockOppija.linkitetytOidit,
        mockOppija.hetu,
        None,
        Some(Date.valueOf("1901-01-01")),
        mockOppija.sukunimi,
        mockOppija.etunimet,
        Some("fi"),
        None,
        false,
        None,
        None,
        true
      )))
    }
    "Huomioi linkitetyt oidit" in {
      val slaveOppija = KoskiSpecificMockOppijat.slave.henkilö
      val hakuOidit = Set(master.oid, slaveOppija.oid)
      val henkilot = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.filter(_.oppijaOid inSet(hakuOidit)).result).toSet
      henkilot should equal (Set(
        RHenkilöRow(slaveOppija.oid, master.oid, List(slaveOppija.oid), master.hetu, None, Some(Date.valueOf("1997-10-10")), master.sukunimi, master.etunimet, Some("fi"), None, false, None, None, true),
        RHenkilöRow(master.oid, master.oid, List(slaveOppija.oid), master.hetu, None, Some(Date.valueOf("1997-10-10")), master.sukunimi, master.etunimet, Some("fi"), None, false, None, None, true)
      ))
    }
    "Master oidia ei löydy koskesta" in {
      val slaveOppija = KoskiSpecificMockOppijat.slaveMasterEiKoskessa.henkilö
      val henkilot = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.filter(_.hetu === slaveOppija.hetu.get).result).toSet
      henkilot should equal(Set(
        RHenkilöRow(slaveOppija.oid, masterEiKoskessa.oid, List(slaveOppija.oid), masterEiKoskessa.hetu, None, Some(Date.valueOf("1966-03-27")), masterEiKoskessa.sukunimi, masterEiKoskessa.etunimet, None, None, false, Some("179"), Some("Jyväskylä"), true),
        RHenkilöRow(masterEiKoskessa.oid, masterEiKoskessa.oid, List(slaveOppija.oid), masterEiKoskessa.hetu, None, Some(Date.valueOf("1966-03-27")), masterEiKoskessa.sukunimi, masterEiKoskessa.etunimet, None, None, false, Some("179"), Some("Jyväskylä"), true)
      ))
    }
    "Organisaatiot on ladattu" in {
      organisaatioCount should be > 10
      val organisaatio = mainRaportointiDb.runDbSync(mainRaportointiDb.ROrganisaatiot.filter(_.organisaatioOid === MockOrganisaatiot.aapajoenKoulu).result)
      organisaatio should equal(Seq(ROrganisaatioRow(MockOrganisaatiot.aapajoenKoulu, "Aapajoen koulu", "OPPILAITOS", Some("11"), Some("04044"), Some("851"), None)))
    }
    "Oppilaitosten opetuskielet on ladattu" in {
      val oppilaitoksetJaKielet = List(
        (MockOrganisaatiot.aapajoenKoulu, "suomi", "1"),
        (MockOrganisaatiot.yrkehögskolanArcada, "ruotsi", "2")
      )
      oppilaitoksetJaKielet.foreach{ case(oppilaitosOid, kieli, kielikoodi) =>
        val oppilaitos = mainRaportointiDb.runDbSync(
          mainRaportointiDb.ROrganisaatiot.filter(_.organisaatioOid === oppilaitosOid).result
        ).head
        mainRaportointiDb.oppilaitoksenKielet(oppilaitos.organisaatioOid).shouldEqual(
          Set(RKoodistoKoodiRow("oppilaitoksenopetuskieli", kielikoodi, kieli))
        )
      }
    }
    "Koodistot on ladattu" in {
      koodistoKoodiCount should be > 500
      val koodi = mainRaportointiDb.runDbSync(mainRaportointiDb.RKoodistoKoodit.filter(_.koodistoUri === "opiskeluoikeudentyyppi").filter(_.koodiarvo === "korkeakoulutus").result)
      koodi should equal(Seq(RKoodistoKoodiRow("opiskeluoikeudentyyppi", "korkeakoulutus", "Korkeakoulutus")))
    }
    "Status-rajapinta" in {
      authGet("api/raportointikanta/status") {
        verifyResponseStatusOk()
        JsonMethods.parse(body) \ "public" \ "isComplete" should equal(JBool(true))
      }
    }

    "Peräkkäinen load-kutsu ei tee mitään" in {
      authGet("api/raportointikanta/load")(verifyResponseStatusOk())
      Wait.until(isLoading)
      val loadStarted = getLoadStartedTime
      authGet("api/raportointikanta/load")(verifyResponseStatusOk())
      loadStarted should equal(getLoadStartedTime)
    }

    "Force load" in {
      authGet("api/raportointikanta/load")(verifyResponseStatusOk())
      Wait.until(isLoading)
      val loadStarted = getLoadStartedTime
      authGet("api/raportointikanta/load?force=true")(verifyResponseStatusOk())
      loadStarted before getLoadStartedTime should be(true)
    }
  }

  "Opiskeluoikeuksien lataus" - {
    implicit val context: ExtractionContext = strictDeserialization

    val ammatillinenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/ammatillinen-perustutkinto_2020-04-24.json")
    val oid = "1.2.246.562.15.123456"
    val ammatillinenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](ammatillinenJson).right.get.opiskeluoikeudet.head.asInstanceOf[AmmatillinenOpiskeluoikeus].copy(oid = Some(oid))
    val perusopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/perusopetuksenoppimaara-paattotodistus_2018-02-14.json")
    val perusopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](perusopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[PerusopetuksenOpiskeluoikeus].copy(oid = Some(oid))
    val esiopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/esiopetusvalmis_2019-12-03.json")
    val esiopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](esiopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[EsiopetuksenOpiskeluoikeus].copy(oid = Some(oid))
    val lukionJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/lukio-paattotodistus_2020-09-10.json")
    val lukionOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](lukionJson).right.get.opiskeluoikeudet.head.asInstanceOf[LukionOpiskeluoikeus].copy(oid = Some(oid))
    val aikuistenPerusopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/aikuistenperusopetuksenoppimaara2017_2020-07-13.json")
    val aikuistenPerusopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](aikuistenPerusopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[AikuistenPerusopetuksenOpiskeluoikeus].copy(oid = Some(oid))


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
        val järjestämismuoto = OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", "jarjestamismuoto"), oppisopimus)
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
          val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), oppisopimusJossainPäätasonSuorituksessa = true)
          ))
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
            ))
          ))
        )
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-01-01"), Date.valueOf("2017-03-31"), "lasna", Date.valueOf("2017-01-01"), vaikeastiVammainen = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-04-01"), Date.valueOf(AikajaksoRowBuilder.IndefiniteFuture), "lasna", Date.valueOf("2017-01-01"))
        ))
      }
      "Erityisen koulutustehtävän jakso" in {
        val opiskeluoikeus = lukionOpiskeluoikeus
        val aikajaksoRows = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)

        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2012-09-01"), Date.valueOf("2013-09-01"), "lasna", Date.valueOf("2012-09-01"), erityisenKoulutusTehtävänJaksoTehtäväKoodiarvo = Some("103"), opintojenRahoitus = Some("1"), sisäoppilaitosmainenMajoitus = true),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2013-09-02"), Date.valueOf("2016-06-07"), "lasna", Date.valueOf("2012-09-01"), opintojenRahoitus = Some("1")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-06-08"), Date.valueOf("2016-06-08"), "valmistunut", Date.valueOf("2016-06-08"), opintojenRahoitus = Some("1"), opiskeluoikeusPäättynyt = true)
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
        val (ps, _, _, _) = OpiskeluoikeusLoader.buildSuoritusRows(oid, None, opiskeluoikeus.oppilaitos.get, opiskeluoikeus.suoritukset.head, JObject(), 1)
        ps.toimipisteOid should equal(AmmatillinenExampleData.stadinToimipiste.oid)
        ps.toimipisteNimi should equal(AmmatillinenExampleData.stadinToimipiste.nimi.get.get("fi"))
      }
    }
  }

  "Mitätöityjen opiskeluoikeuksien lataus" - {
    "Kaikki mitätöidyt opiskeluoikeudet ladataan erilliseen tauluun" in {
      val mitätöidyt = runDbSync(OpiskeluOikeudet.filter(_.mitätöity).result)
      val result = mainRaportointiDb.runDbSync(mainRaportointiDb.RMitätöidytOpiskeluoikeudet.result)

      result.length should equal(mitätöidyt.length)

      result should equal(Seq(
        RMitätöityOpiskeluoikeusRow(
          opiskeluoikeusOid = mitätöidyt(0).oid,
          versionumero = mitätöidyt(0).versionumero,
          aikaleima = mitätöidyt(0).aikaleima,
          oppijaOid = KoskiSpecificMockOppijat.eero.oid,
          mitätöity = LocalDate.now(),
          tyyppi = "perusopetus",
          päätasonSuoritusTyypit = List("perusopetuksenoppimaara", "perusopetuksenvuosiluokka")
        ),
        RMitätöityOpiskeluoikeusRow(
          opiskeluoikeusOid = mitätöidyt(1).oid,
          versionumero = mitätöidyt(1).versionumero,
          aikaleima = mitätöidyt(1).aikaleima,
          oppijaOid = KoskiSpecificMockOppijat.lukiolainen.oid,
          mitätöity = LocalDate.now(),
          tyyppi = "ammatillinenkoulutus",
          päätasonSuoritusTyypit = List("ammatillinentutkinto")
        )
      ))
    }

    "Mitätöityjä opiskeluoikeuksia ei ladata varsinaiseen opiskeluoikeudet-tauluun" in {
    }
  }

  private def opiskeluoikeusCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.ROpiskeluoikeudet.length.result)
  private def henkiloCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.RHenkilöt.length.result)
  private def organisaatioCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.ROrganisaatiot.length.result)
  private def koodistoKoodiCount: Int = mainRaportointiDb.runDbSync(mainRaportointiDb.RKoodistoKoodit.length.result)

  private def isLoading = authGet("api/raportointikanta/status") {
    (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
  }

  private def getLoadStartedTime: Timestamp = authGet("api/raportointikanta/status") {
    JsonSerializer.extract[Timestamp](JsonMethods.parse(body) \ "etl" \ "startedTime")
  }
}

