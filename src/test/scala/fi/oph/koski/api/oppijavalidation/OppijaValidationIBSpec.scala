package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesIB._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.ressunLukio
import fi.oph.koski.documentation.{ExampleData, LukioExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationIBSpec extends AnyFreeSpec with KoskiHttpSpec with PutOpiskeluoikeusTestMethods[IBOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[IBOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = opiskeluoikeus

  "IB validation" - {

    "IB tutkinnon suoritus" - {

      "CAS-aine, arvosanan antaminen" - {
        def historiaOppiaine(level: String, arvosana: String) = ibAineSuoritus(ibOppiaine("HIS", level, 3), ibArviointi(arvosana), ibPredictedArviointi(arvosana))

        "Arvosana S" - {
          "Palautetaan HTTP/200" in {
            val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithCASArvosana("S")
            setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
              verifyResponseStatusOk()
          }}
        }

        "Arvosana numeerinen" - {
          "Palautetaan HTTP/400" in {
            val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithCASArvosana("4")
            setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
              verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
          }}
        }
      }

      "Kaksi samaa oppiainetta"  - {
        def historiaOppiaine(level: String, arvosana: String) = ibAineSuoritus(ibOppiaine("HIS", level, 3), ibArviointi(arvosana), ibPredictedArviointi(arvosana))
        "Joilla sama taso" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "S"),
            historiaOppiaine(higherLevel, "1")
          ))
          "Palautetaan HTTP/400" in { setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus oppiaineetib/HIS esiintyy useammin kuin kerran ryhmässä HL"))
          }}
        }

        "Eri taso, vain toisella tasolla numeroarviointi" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "2"),
            historiaOppiaine(standardLevel, "O")
          ))
          "Palautetaan HTTP/200" in { setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }}
        }

        "Eri taso, molemmilla numeroarviointi" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "3"),
            historiaOppiaine(standardLevel, "4")
          ))
          "Palautetaa HTTP/400" in { setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.kaksiSamaaOppiainettaNumeroarvioinnilla("Kahdella saman oppiaineen suorituksella oppiaineetib/HIS ei molemmilla voi olla numeerista arviointia"))
          }}
        }

        "Eri taso, ei numeroarviointeja" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "S"),
            historiaOppiaine(standardLevel, "S")
          ))
          "Palautetaan HTTP/200" in { setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }}
        }
      }

      "Uusi oppiaine DIS sallittu IBOppiaineMuu" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ibTutkinnonSuoritus(predicted = false).copy(
          osasuoritukset = Some(List(
            IBOppiaineenSuoritus(
              koulutusmoduuli = ibOppiaine("DIS", higherLevel, 3),
              osasuoritukset = None,
              arviointi = ibArviointi("S"),
              predictedArviointi = None,
            )
          ))
        )))

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatusOk()
        }
      }
    }

    "Opintojen rahoitus" - {
      "lasna -tilalta vaaditaan opintojen rahoitus" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
        }
      }
      "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
        val tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusValmistunut)
        ))
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta valmistunut puuttuu rahoitusmuoto"))
        }
      }
    }

    "Maksuttomuustieto" - {
      "Kun opiskeluoikeus alkanut 1.1.2021 jälkeen" - {
        "Palautetaan HTTP/200" in {
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 1, 1) , loppu = None, maksuton = true))))),
              tila = LukionOpiskeluoikeudenTila(
              List(
                LukionOpiskeluoikeusjakso(date(2021, 1, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
                LukionOpiskeluoikeusjakso(date(2022, 6, 4), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
              )
            )
          )
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }
        }
      }
      "Kun opiskeluoikeus alkanut ennen 1.1.2021" - {
        "Palautetaan HTTP/400" in {
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(maksuttomuus = Some(List(Maksuttomuus(alku = date(2020, 12, 31) , loppu = None, maksuton = true))))),
            tila = LukionOpiskeluoikeudenTila(
              List(
                LukionOpiskeluoikeusjakso(date(2020, 12, 31), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
                LukionOpiskeluoikeusjakso(date(2022, 6, 4), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
              )
            )
          )
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on aloittanut Pre-IB opinnot aiemmin kuin 1.1.2021."))
          }
        }
      }
    }

    "Suorituksen vahvistaminen" - {

      "jos vahvistuspäivä 1.1.2024 tai myöhemmin" - {

        "Ei onnistu, jos ei yhtään osasuoritusta" in {
          val suoritus = ibTutkinnonSuoritus(
            predicted = false,
            vahvistus = ExampleData.vahvistusPaikkakunnalla(
              päivä = LocalDate.of(2024, 1, 1),
              org = ressunLukio,
              kunta = helsinki
            )
          )
          val osasuoritukset = None
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(
              List(
                LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
                LukionOpiskeluoikeusjakso(date(2024, 1, 1), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
              )
            ),
            suoritukset = List(suoritus.copy(osasuoritukset = osasuoritukset))
          )
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400,
              KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus koulutus/301102 on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai opiskeluoikeudelta puuttuu linkitys"),
              KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu("Vahvistettu suoritus koulutus/301102 ei sisällä vähintään yhtä osasuoritusta, jolla on predicted grade"),
            )
          }
        }

        "Ei onnistu, jos millään osasuorituksella ei ole predicted-arvosanaa" in {
          val suoritus = ibTutkinnonSuoritus(
            predicted = false,
            vahvistus = ExampleData.vahvistusPaikkakunnalla(
              päivä = LocalDate.of(2024, 1, 1),
              org = ressunLukio,
              kunta = helsinki
            )
          )

          val osasuoritukset = suoritus.osasuoritukset.map(_.map(_.copy(
            arviointi = ibArviointi("5"),
            predictedArviointi = None,
          )))
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(
              List(
                LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
                LukionOpiskeluoikeusjakso(date(2024, 1, 1), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
              )
            ),
            suoritukset = List(preIBSuoritus, suoritus.copy(osasuoritukset = osasuoritukset))
          )
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400,
              KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu("Vahvistettu suoritus koulutus/301102 ei sisällä vähintään yhtä osasuoritusta, jolla on predicted grade"),
            )
          }
        }

        "Ei onnistu, jos joltain osasuoritukselta puuttuu päättöarvosana" in {
          val suoritus = ibTutkinnonSuoritus(
            predicted = false,
            vahvistus = ExampleData.vahvistusPaikkakunnalla(
              päivä = LocalDate.of(2024, 1, 1),
              org = ressunLukio,
              kunta = helsinki
            )
          )
          val osasuoritukset = suoritus.osasuoritukset.map(_.map(os => os.copy(
            arviointi = (if (os.koulutusmoduuli.tunniste.koodiarvo == "A2") { None } else { ibArviointi("5") }),
            predictedArviointi = ibPredictedArviointi("4"),
          )))
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(
              List(
                LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
                LukionOpiskeluoikeusjakso(date(2024, 1, 1), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
              )
            ),
            suoritukset = List(suoritus.copy(osasuoritukset = osasuoritukset))
          )
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400,
              KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/301102 on keskeneräinen osasuoritus oppiaineetib/A2")
            )
          }
        }

        "Onnistuu, kun kaikilla osasuorituksilla on päättöarvosana ja osalla niistä predicted-arvosana" in {
          val suoritus = ibTutkinnonSuoritus(predicted = false)
          val osasuoritukset = suoritus.osasuoritukset.map(_.map(os => os.copy(
            arviointi = ibArviointi("5"),
            predictedArviointi = (if (os.koulutusmoduuli.tunniste.koodiarvo != "A2") { None } else { ibPredictedArviointi("4") }),
          )))
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus.copy(osasuoritukset = osasuoritukset)))
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }
        }

        "Onnistuu, kun molemmat arvosanat on annettu" in {
          val suoritus = ibTutkinnonSuoritus(predicted = false)
          val osasuoritukset = suoritus.osasuoritukset.map(_.map(_.copy(
            arviointi = ibArviointi("5"),
            predictedArviointi = ibPredictedArviointi("4"),
          )))
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus.copy(osasuoritukset = osasuoritukset)))
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }
        }
      }

      "jos vahvistuspäivä 31.12.2023 tai aiemmin" - {

        "Onnistuu, vaikka millään osasuorituksella ei ole predicted-arvosanaa" in {
          val suoritus = ibTutkinnonSuoritus(
            predicted = false,
            vahvistus = ExampleData.vahvistusPaikkakunnalla(
              päivä = LocalDate.of(2023, 12, 31),
              org = ressunLukio,
              kunta = helsinki
            )
          )

          val osasuoritukset = suoritus.osasuoritukset.map(_.map(_.copy(
            arviointi = ibArviointi("5"),
            predictedArviointi = None,
          )))
          val opiskeluoikeus = defaultOpiskeluoikeus.copy(
            tila = LukionOpiskeluoikeudenTila(
              List(
                LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
                LukionOpiskeluoikeusjakso(date(2024, 12, 31), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
              )
            ),
            suoritukset = List(suoritus.copy(osasuoritukset = osasuoritukset))
          )
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }
        }
      }
    }

    "Muutokset IB-tutkinnon rakenteeseen 1.8.2025 alkaen" - {
      val rajapäivä = LocalDate.of(2025, 8, 1)

      def createOpiskeluoikeusYhdelläKurssilla(
        alkamispäivä: LocalDate,
        laajuus: LaajuusOpintopisteissäTaiKursseissa,
        modifyPts: IBTutkinnonSuoritus => IBPäätasonSuoritus = filterIbTutkinto(),
      ): IBOpiskeluoikeus = opiskeluoikeus.copy(
        tila = LukionOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            LukionOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
          )
        ),
        suoritukset = List(
          modifyPts(ibTutkinnonSuoritus(predicted = false, vahvistus = None).copy(
            osasuoritukset = Some(List(
              ibAineSuoritus(
                ibKieli("A", "FI", standardLevel, 1),
                None,
                None,
                List(
                  (ibKurssi("FIN_S1", "A Finnish standard level 1").copy(laajuus = Some(laajuus)), "4", Some("B"))),
              ))
            )
          ))
        )
      )

      def filterIbTutkinto(extendedEssay: Boolean = false, theoryOfKnowledge: Boolean = false, cas: Boolean = false, lisäpisteet: Boolean = false)(pts: IBTutkinnonSuoritus): IBTutkinnonSuoritus =
        pts.copy(
          extendedEssay = if (extendedEssay) pts.extendedEssay else None,
          theoryOfKnowledge = if (theoryOfKnowledge) pts.theoryOfKnowledge else None,
          creativityActionService = if (cas) pts.creativityActionService else None,
          lisäpisteet = if (lisäpisteet) pts.lisäpisteet else None,
        )


      "Ennen rajapäivää" - {
        "Laajuuden ilmoitus kursseina ok" in {
          val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä.minusDays(1), LaajuusKursseissa(1))
          setupOppijaWithOpiskeluoikeus(oo) {
            verifyResponseStatusOk()
          }
        }

        "Laajuuden ilmoitus opintopisteinä ei ole ok" in {
          val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä.minusDays(1), LaajuusOpintopisteissä(1))
          setupOppijaWithOpiskeluoikeus(oo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(
              "Osasuorituksen laajuuden voi ilmoitettaa opintopisteissä vain 1.8.2025 tai myöhemmin alkaneille IB-tutkinnon opiskeluoikeuksille"
            ))
          }
        }

        "Core Requirements -kentät voidaan siirtää" in {
          val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä.minusDays(1), LaajuusKursseissa(1), filterIbTutkinto(
            theoryOfKnowledge = true,
            extendedEssay = true,
            cas = true,
            lisäpisteet = true,
          ))
          setupOppijaWithOpiskeluoikeus(oo) {
            verifyResponseStatusOk()
          }
        }
      }

      "Rajapäivän jälkeen" - {
        "Laajuuden ilmoitus kursseina ei ole ok" in {
          val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusKursseissa(1))
          setupOppijaWithOpiskeluoikeus(oo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(
              "Osasuorituksen laajuus on ilmoitettava opintopisteissä 1.8.2025 tai myöhemmin alkaneille IB-tutkinnon opiskeluoikeuksille"
            ))
          }
        }

        "Laajuuden ilmoitus opintopisteinä on ok" in {
          val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1))
          setupOppijaWithOpiskeluoikeus(oo) {
            verifyResponseStatusOk()
          }
        }

        "Core Requirements" - {
          "Extended Essay ei voi siirtää päätason suorituksen kenttään" in {
            val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1), filterIbTutkinto(extendedEssay = true))
            setupOppijaWithOpiskeluoikeus(oo) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.dpCoreDeprecated(
                "Extended Essay -suoritus on siirrettävä osasuorituksena 1.8.2025 tai myöhemmin alkaneelle IB-opiskeluoikeudelle"
              ))
            }
          }

          "Theory of Knowledge ei voi siirtää päätason suorituksen kenttään" in {
            val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1), filterIbTutkinto(theoryOfKnowledge = true))
            setupOppijaWithOpiskeluoikeus(oo) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.dpCoreDeprecated(
                "Theory of Knowledge -suoritus on siirrettävä osasuorituksena 1.8.2025 tai myöhemmin alkaneelle IB-opiskeluoikeudelle"
              ))
            }
          }

          "CAS ei voi siirtää päätason suorituksen kenttään" in {
            val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1), filterIbTutkinto(cas = true))
            setupOppijaWithOpiskeluoikeus(oo) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.dpCoreDeprecated(
                "Creativity Action Service -suoritus on siirrettävä osasuorituksena 1.8.2025 tai myöhemmin alkaneelle IB-opiskeluoikeudelle"
              ))
            }
          }

          "Lisäpisteitä ei voi siirtää" in {
            val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1), filterIbTutkinto(lisäpisteet = true))
            setupOppijaWithOpiskeluoikeus(oo) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.dpCoreDeprecated(
                "Lisäpisteitä ei voi siirtää 1.8.2025 tai myöhemmin alkaneelle IB-opiskeluoikeudelle"
              ))
            }
          }
        }

        "Pre-IB:n opetussunnitelma" - {
          "Vuoden 2015 opetussuunnitelman mukaista pre-ib-suoritusta ei voi siirtää" in {
            val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1), { _ => preIBSuoritus })
            setupOppijaWithOpiskeluoikeus(oo) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne(
                "1.8.2025 tai myöhemmin alkaneelle IB-opiskeluoikeudelle voi siirtää vain vuoden 2019 opetussuunnitelman mukaisen pre-IB-suorituksen"
              ))
            }
          }

          "Vuoden 2019 opetussuunnitelman mukaisen pre-ib-suorituksen voi siirtää" in {
            val oo = createOpiskeluoikeusYhdelläKurssilla(rajapäivä, LaajuusOpintopisteissä(1), { _ => preIBSuoritus2019 })
            setupOppijaWithOpiskeluoikeus(oo) {
              verifyResponseStatusOk()
            }
          }
        }
      }
    }
  }

  private def opiskeluoikeusIBTutkinnollaWithOppiaineet(oppiaineet: List[IBOppiaineenSuoritus]) = {
    defaultOpiskeluoikeus.copy(
      suoritukset = List(ibTutkinnonSuoritus(predicted = false).copy(
        osasuoritukset = Some(oppiaineet)
      ))
    )
  }

  private def opiskeluoikeusIBTutkinnollaWithCASArvosana(arvosana: String) = {
    defaultOpiskeluoikeus.copy(
      suoritukset = List(ibTutkinnonSuoritus(predicted = false).copy(
        creativityActionService = Some(IBCASSuoritus(
          IBOppiaineCAS(laajuus = Some(LaajuusTunneissa(267))), ibCASArviointi(arvosana)
        ))
      ))
    )
  }
}
