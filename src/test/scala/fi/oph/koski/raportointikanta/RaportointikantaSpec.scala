package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.{master, masterEiKoskessa}
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.util.Wait
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JBool, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class RaportointikantaSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with BeforeAndAfterAll {
  implicit val formats = DefaultFormats

  private lazy val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  override def beforeAll(): Unit = {
    LocalJettyHttpSpecification.setup(this)
    createOrUpdate(MockOppijat.slaveMasterEiKoskessa.henkilö, defaultOpiskeluoikeus)
    authGet("api/raportointikanta/load?force=true")(verifyResponseStatusOk())
    Wait.until(loadComplete)
  }

  override def afterAll(): Unit = resetFixtures

  "Raportointikanta" - {
    "Opiskeluoikeudet on ladattu" in {
      opiskeluoikeusCount should be > 30
    }
    "Henkilöt on ladattu" in {
      val mockOppija = MockOppijat.eero
      henkiloCount should be > 30
      val henkilo = raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.filter(_.hetu === mockOppija.hetu.get).result)
      henkilo should equal(Seq(RHenkilöRow(
        mockOppija.oid,
        mockOppija.oid,
        mockOppija.hetu,
        None,
        Some(Date.valueOf("1901-01-01")),
        mockOppija.sukunimi,
        mockOppija.etunimet,
        Some("fi"),
        None,
        false
      )))
    }
    "Huomioi linkitetyt oidit" in {
      val slaveOppija = MockOppijat.slave.henkilö
      val hakuOidit = Set(master.oid, slaveOppija.oid)
      val henkilot = raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.filter(_.oppijaOid inSet(hakuOidit)).result).toSet
      henkilot should equal (Set(
        RHenkilöRow(slaveOppija.oid, master.oid, master.hetu, None, Some(Date.valueOf("1997-10-10")), master.sukunimi, master.etunimet, Some("fi"), None, false),
        RHenkilöRow(master.oid, master.oid, master.hetu, None, Some(Date.valueOf("1997-10-10")), master.sukunimi, master.etunimet, Some("fi"), None, false)
      ))
    }
    "Master oidia ei löydy koskesta" in {
      val slaveOppija = MockOppijat.slaveMasterEiKoskessa.henkilö
      val henkilot = raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.filter(_.hetu === slaveOppija.hetu.get).result).toSet
      henkilot should equal(Set(
        RHenkilöRow(slaveOppija.oid, masterEiKoskessa.oid, masterEiKoskessa.hetu, None, Some(Date.valueOf("1966-03-27")), masterEiKoskessa.sukunimi, masterEiKoskessa.etunimet, None, None, false),
        RHenkilöRow(masterEiKoskessa.oid, masterEiKoskessa.oid, masterEiKoskessa.hetu, None, Some(Date.valueOf("1966-03-27")), masterEiKoskessa.sukunimi, masterEiKoskessa.etunimet, None, None, false)
      ))
    }
    "Organisaatiot on ladattu" in {
      organisaatioCount should be > 10
      val organisaatio = raportointiDatabase.runDbSync(raportointiDatabase.ROrganisaatiot.filter(_.organisaatioOid === MockOrganisaatiot.aapajoenKoulu).result)
      organisaatio should equal(Seq(ROrganisaatioRow(MockOrganisaatiot.aapajoenKoulu, "Aapajoen koulu", "OPPILAITOS", Some("11"), Some("04044"), Some("851"), None)))
    }
    "Koodistot on ladattu" in {
      koodistoKoodiCount should be > 500
      val koodi = raportointiDatabase.runDbSync(raportointiDatabase.RKoodistoKoodit.filter(_.koodistoUri === "opiskeluoikeudentyyppi").filter(_.koodiarvo === "korkeakoulutus").result)
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
    import fi.oph.koski.schema.KoskiSchema.deserializationContext
    val ammatillinenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/ammatillinen-perustutkinto_2018-02-14.json")
    val oid = "1.2.246.562.15.123456"
    val ammatillinenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](ammatillinenJson).right.get.opiskeluoikeudet.head.asInstanceOf[AmmatillinenOpiskeluoikeus].copy(oid = Some(oid))
    val perusopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/perusopetuksenoppimaara-paattotodistus_2018-02-14.json")
    val perusopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](perusopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[PerusopetuksenOpiskeluoikeus].copy(oid = Some(oid))

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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-28"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-29"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-04"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-05"), Date.valueOf("2016-02-10"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1, vankilaopetuksessa = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-11"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), vankilaopetuksessa = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", Date.valueOf("2016-01-15"), vankilaopetuksessa = 1, osaAikaisuus = 80),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf("2016-04-10"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1, vankilaopetuksessa = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-11"), Date.valueOf("2016-04-20"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-21"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-28"), "lasna", Date.valueOf("2016-01-15"), opiskeluvalmiuksiaTukevatOpinnot = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-29"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), opiskeluvalmiuksiaTukevatOpinnot = 1, vaikeastiVammainen = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", Date.valueOf("2016-01-15"), vaikeastiVammainen = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), majoitus = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", Date.valueOf("2016-01-15"), sisäoppilaitosmainenMajoitus = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf("2016-04-30"), "lasna", Date.valueOf("2016-01-15"), vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-05-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
          val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), hojks = 1)
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
          val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), hojks = 1),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
          val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"), oppisopimusJossainPäätasonSuorituksessa = 1)
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
          val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
          aikajaksoRows should equal(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15"), oppisopimusJossainPäätasonSuorituksessa = 1),
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2016-01-15"))
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-01-01"), Date.valueOf("2017-03-31"), "lasna", Date.valueOf("2017-01-01"), vaikeastiVammainen = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-04-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", Date.valueOf("2017-01-01"))
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
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-02-29"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-12-15"), "lasna", Date.valueOf("2016-01-15"), erityinenTuki = 1, osaAikaisuus = 80),
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
        val (ps, _) = OpiskeluoikeusLoader.buildSuoritusRows(oid, opiskeluoikeus.oppilaitos.get, opiskeluoikeus.suoritukset.head, JObject(), 1)
        ps.toimipisteOid should equal(AmmatillinenExampleData.stadinToimipiste.oid)
        ps.toimipisteNimi should equal(AmmatillinenExampleData.stadinToimipiste.nimi.get.get("fi"))
      }
    }
  }

  private def opiskeluoikeusCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.ROpiskeluoikeudet.length.result)
  private def henkiloCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.length.result)
  private def organisaatioCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.ROrganisaatiot.length.result)
  private def koodistoKoodiCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.RKoodistoKoodit.length.result)

  private def isLoading = authGet("api/raportointikanta/status") {
    (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
  }

  private def getLoadStartedTime: Timestamp = authGet("api/raportointikanta/status") {
    JsonSerializer.extract[Timestamp](JsonMethods.parse(body) \ "etl" \ "startedTime")
  }

  private def loadComplete = authGet("api/raportointikanta/status") {
    val isComplete = (JsonMethods.parse(body) \ "public" \ "isComplete").extract[Boolean]
    val isLoading = (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
    isComplete && !isLoading
  }
}

