package fi.oph.koski.todistus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.opiskeluoikeus.OidGenerator
import fi.oph.koski.organisaatio.MockOrganisaatiot.YleinenKielitutkintoOrg
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.todistus.yleinenkielitutkinto.{YleinenKielitutkintoTodistusData, YleinenKielitutkintoTodistusDataBuilder}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YleinenKielitutkintoTodistusDataBuilderSpec extends AnyFreeSpec with Matchers {
  private val app = KoskiApplicationForTests
  private val generator = new YleinenKielitutkintoTodistusDataBuilder(app)
  private val mockOppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja

  private val oidGenerator = OidGenerator(app.config)

  "YleinenKielitutkintoTodistusDataBuilder" - {
    "Osasuoritusten järjestäminen" - {
      "järjestää osasuoritukset oikeaan järjestykseen" in {
        val osasuoritukset = List(
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("tekstinymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puheenymmartaminen", "4", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("kirjoittaminen", "alle3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob()

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        // Oikea järjestys: puheenymmartaminen, puhuminen, tekstinymmartaminen, kirjoittaminen
        todistusData.suorituksetJaArvosanat.map(_.suoritus) should equal(List(
          "Puheen ymmärtäminen",
          "Puhuminen",
          "Tekstin ymmärtäminen",
          "Kirjoittaminen"
        ))
      }
    }

    "Arvioinnin valinta" - {
      "valitsee tuoreimman arvioinnin, kun osasuorituksella on useita arviointeja" in {
        val osasuoritukset = List(
          createOsakokeenSuoritusMultipleArvioinnit(
            "tekstinymmartaminen",
            List(
              ("2", LocalDate.of(2011, 1, 15)), // Vanhempi arviointi
              ("4", LocalDate.of(2011, 3, 4))   // Uudempi arviointi
            )
          ),
          createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob()

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        // Tekstinymmärtämisen arvosanan pitäisi olla "4" (uudempi), ei "2" (vanhempi)
        val tekstinymmartaminenSuoritus = todistusData.suorituksetJaArvosanat
          .find(_.suoritus == "Tekstin ymmärtäminen")

        tekstinymmartaminenSuoritus should not be empty
        tekstinymmartaminenSuoritus.get.arvosana should equal("4, keskitaso")
      }

      "valitsee tuoreimman arvioinnin oikein, kun arviointipäivät ovat käänteisessä järjestyksessä" in {
        val osasuoritukset = List(
          createOsakokeenSuoritusMultipleArvioinnit(
            "tekstinymmartaminen",
            List(
              ("5", LocalDate.of(2011, 3, 4)),   // Uudempi arviointi ensin
              ("2", LocalDate.of(2011, 1, 15))   // Vanhempi arviointi
            )
          ),
          createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob()

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        val tekstinymmartaminenSuoritus = todistusData.suorituksetJaArvosanat
          .find(_.suoritus == "Tekstin ymmärtäminen")

        tekstinymmartaminenSuoritus should not be empty
        tekstinymmartaminenSuoritus.get.arvosana should equal("5, ylin taso")
      }

      "valitsee tuoreimman arvioinnin kolmesta vaihtoehdosta" in {
        val osasuoritukset = List(
          createOsakokeenSuoritusMultipleArvioinnit(
            "tekstinymmartaminen",
            List(
              ("2", LocalDate.of(2011, 1, 15)),  // Vanhin
              ("3", LocalDate.of(2011, 2, 10)),  // Keskimmäinen
              ("5", LocalDate.of(2011, 3, 4))    // Tuorein
            )
          ),
          createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob()

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        val tekstinymmartaminenSuoritus = todistusData.suorituksetJaArvosanat
          .find(_.suoritus == "Tekstin ymmärtäminen")

        tekstinymmartaminenSuoritus should not be empty
        tekstinymmartaminenSuoritus.get.arvosana should equal("5, ylin taso")
      }
    }

    "Template-nimen lokalisointi" - {
      "palauttaa oikean template-nimen suomen kielelle" in {
        verifyTemplateName(TodistusLanguage.FI, "kielitutkinto_yleinenkielitutkinto_fi")
      }

      "palauttaa oikean template-nimen ruotsin kielelle" in {
        verifyTemplateName(TodistusLanguage.SV, "kielitutkinto_yleinenkielitutkinto_sv")
      }

      "palauttaa oikean template-nimen englannin kielelle" in {
        verifyTemplateName(TodistusLanguage.EN, "kielitutkinto_yleinenkielitutkinto_en")
      }
    }

    "Päivämääräformatoinnin lokalisointi" - {
      "formatoi päivämäärät suomeksi (DD.MM.YYYY)" in {
        val osasuoritukset = List(
          createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("tekstinymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob(TodistusLanguage.FI)

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        // Syntymäaika suomalaisessa muodossa
        todistusData.oppijaSyntymäaika should equal("1.1.2007")
        // Allekirjoituspäivä suomalaisessa muodossa
        todistusData.allekirjoitusPäivämäärä should equal("3.1.2011")
        // Vahvistuksen viimeinen päivämäärä suomalaisessa muodossa (DD.MM.YYYY)
        todistusData.vahvistusViimeinenPäivämäärä should fullyMatch regex """\d{1,2}\.\d{1,2}\.\d{4}"""
      }

      "formatoi päivämäärät ruotsiksi (DD.MM.YYYY)" in {
        val osasuoritukset = List(
          createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("tekstinymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob(TodistusLanguage.SV)

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        // Syntymäaika ruotsalaisessa muodossa (sama kuin suomessa)
        todistusData.oppijaSyntymäaika should equal("1.1.2007")
        // Allekirjoituspäivä ruotsalaisessa muodossa
        todistusData.allekirjoitusPäivämäärä should equal("3.1.2011")
        // Vahvistuksen viimeinen päivämäärä ruotsalaisessa muodossa (DD.MM.YYYY)
        todistusData.vahvistusViimeinenPäivämäärä should fullyMatch regex """\d{1,2}\.\d{1,2}\.\d{4}"""
      }

      "formatoi päivämäärät englanniksi" in {
        val osasuoritukset = List(
          createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("tekstinymmartaminen", "3", LocalDate.of(2011, 3, 4)),
          createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4))
        )

        val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
        val todistusJob = createTodistusJob(TodistusLanguage.EN)

        val result = generator.createTodistusData(
          mockOppija,
          opiskeluoikeus,
          todistusJob
        )

        result match {
          case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
          case Right(_) => // OK
        }
        val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]

        // Syntymäaika edelleen speksatussa muodossa suomi/ruotsi-tyyliin
        todistusData.oppijaSyntymäaika should equal("1.1.2007")
        // Allekirjoituspäivä englantilaisessa muodossa (Dth of Month, YYYY)
        todistusData.allekirjoitusPäivämäärä should equal("3rd of January, 2011")
        // Vahvistuksen viimeinen päivämäärä englantilaisessa muodossa (D Month YYYY)
        todistusData.vahvistusViimeinenPäivämäärä should fullyMatch regex """\d{1,2} [A-Z][a-z]+ \d{4}"""
      }

      "formatoi allekirjoitukseen eri päivämääriä englanniksi oikeilla järjestyslukusuffikseilla" in {
        // Testaa eri päiviä jotta varmistetaan oikeat suffiksit (st, nd, rd, th)
        val testCases = List(
          (1, "1st"),
          (2, "2nd"),
          (3, "3rd"),
          (4, "4th"),
          (21, "21st"),
          (22, "22nd"),
          (23, "23rd"),
          (31, "31st")
        )

        testCases.foreach { case (day, expectedPrefix) =>
          val testDate = LocalDate.of(2011, 1, day)
          val osasuoritukset = List(
            createOsakokeenSuoritus("puheenymmartaminen", "3", testDate)
          )

          // Luo opiskeluoikeus jossa ensimmäinen läsnä-tila alkaa testipäivänä
          val arviointipäivä = testDate
          val opiskeluoikeusRaw = KielitutkinnonOpiskeluoikeus(
            oid = Some("1.2.246.562.15.00000000001"),
            tila = KielitutkinnonOpiskeluoikeudenTila(
              opiskeluoikeusjaksot = List(
                KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
                  alku = testDate,
                  tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
                )
              )
            ),
            suoritukset = List(
              YleisenKielitutkinnonSuoritus(
                koulutusmoduuli = YleinenKielitutkinto(
                  tunniste = Koodistokoodiviite("kt", "ykitutkintotaso"),
                  kieli = Koodistokoodiviite("FI", "kieli")
                ),
                toimipiste = OidOrganisaatio(YleinenKielitutkintoOrg.organisaatio),
                järjestäjä = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
                vahvistus = Some(Päivämäärävahvistus(
                  päivä = arviointipäivä,
                  myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki)
                )),
                osasuoritukset = Some(osasuoritukset),
                yleisarvosana = None
              )
            )
          )

          // Aja serialisoinnin ja deserialisoinnin läpi, jotta opiskeluoikeudessa esim. koodiarvot lokalisoidaan
          val jsonString = JsonMethods.pretty(JsonSerializer.serializeWithRoot(opiskeluoikeusRaw))
          val opiskeluoikeus = app.validatingAndResolvingExtractor.extract[KielitutkinnonOpiskeluoikeus](strictDeserialization)(JsonMethods.parse(jsonString)).getOrElse(throw new InternalError("Bad test data"))

          val todistusJob = createTodistusJob(TodistusLanguage.EN)

          val result = generator.createTodistusData(
            mockOppija,
            opiskeluoikeus,
            todistusJob
          )

          result match {
            case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
            case Right(_) => // OK
          }
          val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]
          todistusData.allekirjoitusPäivämäärä should startWith(expectedPrefix)
        }
      }
    }
  }

  private def createTodistusJob(language: TodistusLanguage.TodistusLanguage = TodistusLanguage.FI): TodistusJob = {
    TodistusJob(
      id = "1",
      userOid = Some(mockOppija.oid),
      oppijaOid = mockOppija.oid,
      opiskeluoikeusOid = "1.2.246.562.15.00000000001",
      language = language,
      opiskeluoikeusVersionumero = Some(1),
      oppijaHenkilötiedotHash = Some("xxx"),
      state = TodistusState.GENERATING_RAW_PDF,
      createdAt = LocalDate.now().atStartOfDay()
    )
  }

  private def createOpiskeluoikeus(osasuoritukset: List[YleisenKielitutkinnonOsakokeenSuoritus]): KielitutkinnonOpiskeluoikeus = {
    val arviointipäivä = LocalDate.of(2011, 3, 4)

    val opiskeluoikeus = KielitutkinnonOpiskeluoikeus(
      oid = Some(oidGenerator.generateKoskiOid(mockOppija.oid)),
      tila = KielitutkinnonOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
            alku = LocalDate.of(2011, 1, 3),
            tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
          ),
          KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
            alku = arviointipäivä,
            tila = Koodistokoodiviite("hyvaksytystisuoritettu", "koskiopiskeluoikeudentila")
          )
        )
      ),
      suoritukset = List(
        YleisenKielitutkinnonSuoritus(
          koulutusmoduuli = YleinenKielitutkinto(
            tunniste = Koodistokoodiviite("kt", "ykitutkintotaso"),
            kieli = Koodistokoodiviite("FI", "kieli")
          ),
          toimipiste = OidOrganisaatio(YleinenKielitutkintoOrg.organisaatio),
          järjestäjä = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
          vahvistus = Some(Päivämäärävahvistus(
            päivä = arviointipäivä,
            myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki)
          )),
          osasuoritukset = Some(osasuoritukset),
          yleisarvosana = None
        )
      )
    )

    // Aja serialisoinnin ja deserialisoinnin läpi, jotta opiskeluoikeudessa esim. koodiarvot lokalisoidaan
    val jsonString = JsonMethods.pretty(JsonSerializer.serializeWithRoot(opiskeluoikeus))

    app.validatingAndResolvingExtractor.extract[KielitutkinnonOpiskeluoikeus](strictDeserialization)(JsonMethods.parse(jsonString)).getOrElse(throw new InternalError("Bad test data"))
  }

  private def createOsakokeenSuoritus(
    tyyppi: String,
    arvosana: String,
    arviointipäivä: LocalDate
  ): YleisenKielitutkinnonOsakokeenSuoritus = {
    YleisenKielitutkinnonOsakokeenSuoritus(
      koulutusmoduuli = YleisenKielitutkinnonOsakoe(
        tunniste = Koodistokoodiviite(tyyppi, "ykisuorituksenosa")
      ),
      arviointi = Some(List(
        YleisenKielitutkinnonOsakokeenArviointi(
          arvosana = Koodistokoodiviite(arvosana, "ykiarvosana"),
          päivä = arviointipäivä
        )
      ))
    )
  }

  private def createOsakokeenSuoritusMultipleArvioinnit(
    tyyppi: String,
    arvioinnit: List[(String, LocalDate)]
  ): YleisenKielitutkinnonOsakokeenSuoritus = {
    YleisenKielitutkinnonOsakokeenSuoritus(
      koulutusmoduuli = YleisenKielitutkinnonOsakoe(
        tunniste = Koodistokoodiviite(tyyppi, "ykisuorituksenosa")
      ),
      arviointi = Some(arvioinnit.map { case (arvosana, päivä) =>
        YleisenKielitutkinnonOsakokeenArviointi(
          arvosana = Koodistokoodiviite(arvosana, "ykiarvosana"),
          päivä = päivä
        )
      })
    )
  }

  private def verifyTemplateName(language: TodistusLanguage.TodistusLanguage, expectedTemplateName: String): Unit = {
    val osasuoritukset = List(
      createOsakokeenSuoritus("puheenymmartaminen", "3", LocalDate.of(2011, 3, 4)),
      createOsakokeenSuoritus("puhuminen", "3", LocalDate.of(2011, 3, 4)),
      createOsakokeenSuoritus("tekstinymmartaminen", "3", LocalDate.of(2011, 3, 4)),
      createOsakokeenSuoritus("kirjoittaminen", "3", LocalDate.of(2011, 3, 4))
    )

    val opiskeluoikeus = createOpiskeluoikeus(osasuoritukset)
    val todistusJob = TodistusJob(
      id = "1",
      userOid = Some(mockOppija.oid),
      oppijaOid = mockOppija.oid,
      opiskeluoikeusOid = "1.2.246.562.15.00000000001",
      language = language,
      opiskeluoikeusVersionumero = Some(1),
      oppijaHenkilötiedotHash = Some("xxx"),
      state = TodistusState.GENERATING_RAW_PDF,
      createdAt = LocalDate.now().atStartOfDay()
    )

    val result = generator.createTodistusData(
      mockOppija,
      opiskeluoikeus,
      todistusJob
    )

    result match {
      case Left(error) => fail(s"Virhe todistuksen luonnissa: ${error.errorString.mkString(", ")}")
      case Right(_) => // OK
    }
    val todistusData = result.toOption.get.asInstanceOf[YleinenKielitutkintoTodistusData]
    todistusData.templateName should equal(expectedTemplateName)
  }
}
