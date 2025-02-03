package fi.oph.koski.api.misc

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData.{perusopetuksenOppimääränSuoritus, yhdeksännenLuokanSuoritus}
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.ValpasOppivelvollisuudestaVapautusService
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class MaksuttomuusSpec extends AnyFreeSpec with OpiskeluoikeusTestMethodsAmmatillinen with KoskiHttpSpec {
  val oppivelvollisuudestaVapautusService: ValpasOppivelvollisuudestaVapautusService = KoskiApplicationForTests.valpasOppivelvollisuudestaVapautusService

  oppivelvollisuudestaVapautusService.db.deleteAll()

  "Tiedon siirtäminen" - {
    lazy val opiskeluoikeus = alkamispäivällä(defaultOpiskeluoikeus, date(2021, 1, 1))
    "Testattavan opiskeluoikeuden suoritus on merkitty vaativan maksuttomuustiedon lisätiedoilta" in {
      opiskeluoikeus.suoritukset.collectFirst { case s: SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta => s }.isDefined shouldBe(true)
    }
    "Saa siirtää jos opiskeluoikeus on alkanut ennen 1.1.2021" in {
      setupOppijaWithMaksuttomuus(
        List(
          Maksuttomuus(date(2020, 12, 31), None, true)
        ),
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        alkamispäivällä(defaultOpiskeluoikeus, date(2020, 12, 31))
      ) {
        verifyResponseStatusOk()
      }
    }
    "Saa siirtää jos lukion aiemman kuin 2019 opsin mukainen opiskeluoikeus on alkanut 1.1.2021-31.7.2021" in {
      val alkamispäivä = date(2021, 1, 1)
      putMaksuttomuus(
        List(
          Maksuttomuus(alkamispäivä, None, true)
        ),
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
      ) {
        verifyResponseStatusOk()
      }
    }
    "Saa siirtää jos lukion aiemman kuin 2019 opsin mukainen opiskeluoikeus on alkanut 1.8.2021 tai myöhemmin" in {
      val alkamispäivä = date(2021, 8, 1)
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021)
      putMaksuttomuus(
        List(
          Maksuttomuus(alkamispäivä, None, true)
        ),
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
      ) {
        verifyResponseStatusOk()
      }
    }
    "Ei saa siirtää jos lukion aiemman kuin 2019 opsin mukainen opiskeluoikeus on alkanut aiemmin kuin 1.1.2021" in {
      val alkamispäivä = date(2020, 12, 31)
      putMaksuttomuus(
        List(
          Maksuttomuus(alkamispäivä, None, true)
        ),
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
      ) {
        verifyResponseStatus(
          400,
          KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on aloittanut vanhojen lukion opetussuunnitelman perusteiden mukaisen koulutuksen aiemmin kuin 2021-01-01.")
        )
      }
    }
    "Ei saa siirtää jos henkilö on syntynyt ennen vuotta 2004" in {
      putMaksuttomuus(
        List(
          Maksuttomuus(date(2021, 8, 1), None, true)
        ),
        KoskiSpecificMockOppijat.eiOikeuttaMaksuttomuuteen,
        alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on syntynyt ennen vuotta 2004 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
      }
    }
    "Ei saa siirtää jos opiskeluoikeus on alkamassa vuonna, jona henkilö täyttää 21 tai enemmän" in {
      putMaksuttomuus(
        List(
          Maksuttomuus(date(2025, 1, 1), None, true)
        ),
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        alkamispäivällä(defaultOpiskeluoikeus, date(2025, 1, 1))
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on merkitty alkavaksi vuonna, jona oppija täyttää enemmän kuin 20 vuotta."))
      }
    }
    "Ei saa siirtää jos opiskeluoikeus ei sisällä suoritusta joka vaatii maksuttomuus tiedon" in {
      val o = alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
      val oo = o.copy(suoritukset = List(AmmatillinenExampleData.kiinteistösihteerinMuuAmmatillinenKoulutus().copy(alkamispäivä = Some(date(2021, 8, 1)))))
      oo.suoritukset.collectFirst { case s: SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta => s}.isDefined shouldBe(false)

      putMaksuttomuus(
        List(
          Maksuttomuus(date(2021, 8, 1), None, true)
        ),
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021, oo
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä koulutus ei siirrettyjen tietojen perusteella kelpaa oppivelvollisuuden suorittamiseen (tarkista, että koulutuskoodi, käytetyn opetussuunnitelman perusteen diaarinumero, suorituksen tyyppi ja/tai suoritustapa ovat oikein)."))
      }
    }

    "Oppijalla ei ole hetua" - {
      "Ei saa siirtää maksuttomuustietoa" in {
        putMaksuttomuus(
          List(
            Maksuttomuus(date(2021, 8, 1), None, true)
          ),
          KoskiSpecificMockOppijat.nuoriHetuton,
          alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppijan tiedoista ei löydy henkilötunnusta."))
        }
      }
    }

    "Oppivelvollisuudesta vapautus" - {
      val oppija = KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa

      oppivelvollisuudestaVapautusService.findVapautukset(List(oppija.oid)) shouldBe empty
      oppivelvollisuudestaVapautusService.db.lisääOppivelvollisuudestaVapautus(oppija.oid, "", LocalDate.of(2000, 8, 1), "091")
      oppivelvollisuudestaVapautusService.findVapautukset(List(oppija.oid)) should not be empty

      "Jos vapautus, maksuttomuustietoa ei saa siirtää kun maksuton = true" in {
        setupOppijaWithMaksuttomuus(
          List(Maksuttomuus(date(2021, 8, 1), None, maksuton = true)),
          oppija,
          alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on vapautettu oppivelvollisuudesta ja koulutusta yritettiin merkitä maksuttomaksi."))
        }
      }

      "Jos vapautus, maksuttomuustiedon saa siirtää kun maksuton = false" in {
        setupOppijaWithMaksuttomuus(
          List(Maksuttomuus(date(2021, 8, 1), None, maksuton = false)),
          oppija,
          alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
        ) {
          verifyResponseStatusOk()
        }
        oppivelvollisuudestaVapautusService.db.deleteAll()
      }
    }

    "Siirto kun opiskelijalla perusopetuksen päättötodistus tai siihen verrattavissa oleva suoritus" - {

      "Ei saa siirtää jos suoritus vahvistettu ennen Valpas-lain voimaantuloaikaa" - {
        "Aikuisten perusopetuksen oppimäärä" in {
          val opiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }

        "Perusopetuksen vahvistettu oppimäärä opiskeluoikeuden valmistumisella" in {
          val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
            suoritukset = List(perusopetuksenOppimääränSuoritus.copy(
              vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 1, 1))),
              yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2016, 1, 1)))
            ),
            alkamispäivä = date(2010, 8, 1),
            päättymispäivä = Some(date(2020, 8, 1)),
          )

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }

        "Perusopetuksen vahvistettu oppimäärä ilman opiskeluoikeuden valmistumista" in {
          val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
            suoritukset = List(
              yhdeksännenLuokanSuoritus,
              perusopetuksenOppimääränSuoritus.copy(
                vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 1, 1))
            )),
            alkamispäivä = date(2010, 8, 1),
            päättymispäivä = None,
          )

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }

        "International Schoolin ysiluokka" in {
          val opiskeluoikeus = ExamplesInternationalSchool.opiskeluoikeus.copy(
            suoritukset = Nil,
          )
          val ysiLuokka: MYPVuosiluokanSuoritus = ExamplesInternationalSchool.grade9.copy(alkamispäivä = Some(date(2015, 6, 30)))

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(ysiLuokka)), KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }
        "European School of Helsinki -koulun luokka S4" in {
          val opiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
            suoritukset = Nil,
          )
          val s4Luokka: SecondaryLowerVuosiluokanSuoritus = ExamplesEuropeanSchoolOfHelsinki.s4.copy(alkamispäivä = Some(ExamplesEuropeanSchoolOfHelsinki.alkamispäivä))

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(s4Luokka)), KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }
        "International Schoolin vahvistettu ysiluokka ilman opiskeluoikeuden päättymistä" in {
          val opiskeluoikeus = ExamplesInternationalSchool.opiskeluoikeus.copy(
            tila = InternationalSchoolOpiskeluoikeudenTila(
              List(
                InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen),
              )
            ),
            suoritukset = Nil,
          )
          val ysiLuokka: MYPVuosiluokanSuoritus = ExamplesInternationalSchool.grade9.copy(alkamispäivä = Some(date(2015, 6, 30)))

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(ysiLuokka)), KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }
        "European School of Helsinki -koulun S4 ilman opiskeluoikeuden päättymistä" in {
          val opiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
            tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
              List(
                EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(date(2000, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen),
              )
            ),
            suoritukset = Nil,
          )
          val s4Luokka: SecondaryLowerVuosiluokanSuoritus = ExamplesEuropeanSchoolOfHelsinki.s4.copy(alkamispäivä = Some(date(2015, 6, 30)))

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(s4Luokka)), KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }

        "Linkitetty oppija - slavella siirron estävä oppimäärä" in {
          val opiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1.henkilö) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oppivelvollisuustietoMaster,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }
        "Linkitetty oppija - masterilla siirron estävä oppimäärä" in {
          val opiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oppivelvollisuustietoMaster) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1.henkilö,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä oppija on suorittanut oppivelvollisuutensa ennen 1.1.2021 eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin."))
          }
        }
      }
      "Saa siirtää, jos ennen rajapäivää tehty vahvistus on mitätöidyssä opiskeluoikeudessa" in {
        val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
          suoritukset = List(perusopetuksenOppimääränSuoritus.copy(
            vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 1, 1))),
            yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2016, 1, 1)))
          ),
          alkamispäivä = date(2010, 8, 1),
          päättymispäivä = Some(date(2020, 8, 1))
        )

        mitätöiOpiskeluoikeus(setupOppijaWithAndGetOpiskeluoikeus(henkilö = KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa, oo = opiskeluoikeus))

        putMaksuttomuus(
          List(Maksuttomuus(date(2021, 8, 1), None, true)),
          KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
          alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
        ) {
          verifyResponseStatusOk()
        }
      }
      "Saa siirtää, jos ennen rajapäivää loppunut peruskoulun suoritus on päättynyt eroamiseen" in {
        val peruskoulunAlkamispäivä = date(2010, 8, 1)
        val peruskoulunPäättymispäivä = date(2020, 8, 1)
        val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
          suoritukset = List(perusopetuksenOppimääränSuoritus.copy(
            vahvistus = None
          )),
          alkamispäivä = peruskoulunAlkamispäivä,
          päättymispäivä = Some(peruskoulunPäättymispäivä)
        ).copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
            List(
              NuortenPerusopetuksenOpiskeluoikeusjakso(peruskoulunAlkamispäivä, opiskeluoikeusLäsnä),
              NuortenPerusopetuksenOpiskeluoikeusjakso(peruskoulunPäättymispäivä, opiskeluoikeusEronnut)
            )
          )
        )

        setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa) {
          verifyResponseStatusOk()
        }

        val peruskoulunJälkeisenAlkamispäivä = date(2021, 8, 1)
        putMaksuttomuus(
          List(Maksuttomuus(peruskoulunJälkeisenAlkamispäivä, None, true)),
          KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
          alkamispäivällä(defaultOpiskeluoikeus, peruskoulunJälkeisenAlkamispäivä)
        ) {
          verifyResponseStatusOk()
        }
      }
      "Saa siirtää jos suoritus vahvistettu Valpas-lain voimaantulopäivän jälkeen" in {
        val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
          suoritukset = List(perusopetuksenOppimääränSuoritus.copy(
            vahvistus = vahvistusPaikkakunnalla(päivä = date(2021, 1, 1))),
            yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2016, 1, 1)))
          ),
          alkamispäivä = date(2010, 8, 1),
          päättymispäivä = Some(date(2021, 8, 1))
        )

        val oppija = KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa
        setupOppijaWithOpiskeluoikeus(opiskeluoikeus, oppija) {
          verifyResponseStatusOk()
        }

        putMaksuttomuus(
          List(Maksuttomuus(date(2021, 8, 1), None, true)),
          oppija,
          alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
        ) {
          verifyResponseStatusOk()
        }
      }
      "Ei tarvitse siirtää, jos peruskoulu loppunut ennen vuotta 2021" in {
        val alkamispäivä = date(2021, 1, 1)
        val opiskeluoikeus = LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
        val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaPeruskouluValmisEnnen2021

        putOpiskeluoikeus(opiskeluoikeus, oppija) {
          verifyResponseStatusOk()
        }
      }
      "Ei tarvitse siirtää, jos peruskoulu loppunut eroamiseen ennen vuotta 2021" in {
        val alkamispäivä = date(2021, 1, 1)
        val opiskeluoikeus = LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
        val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaEronnutPeruskoulustaEnnen2021

        putOpiskeluoikeus(opiskeluoikeus, oppija) {
          verifyResponseStatusOk()
        }
      }
      "Ei tarvitse siirtää, jos kotikunta ei ole Suomessa" in {
        val alkamispäivä = date(2021, 1, 1)
        val opiskeluoikeus = LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
        val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021EiKotikuntaaSuomessa

        putOpiskeluoikeus(opiskeluoikeus, oppija) {
          verifyResponseStatusOk()
        }
      }
      "Ei tarvitse siirtää, jos kotikunta Ahvenanmaalla" in {
        val alkamispäivä = date(2021, 1, 1)
        val opiskeluoikeus = LukioExampleData.alkamispäivällä(LukioExampleData.lukionOpiskeluoikeus(), alkamispäivä)
        val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021KotikuntaAhvenanmaalla

        putOpiskeluoikeus(opiskeluoikeus, oppija) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  "European School of Helsinki secondary upper vuosiluokan suoritus" - {
    val lisätiedot = EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(maksuttomuus = Some(List(Maksuttomuus(date(2021, 8, 1), None, true))))
    val opiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
      suoritukset = Nil,
      tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(date(2021, 8, 1), LukioExampleData.opiskeluoikeusAktiivinen))),
      lisätiedot = Some(lisätiedot)
    )
    val s4Luokka = ExamplesEuropeanSchoolOfHelsinki.s4.copy(alkamispäivä = Some(date(2021, 8, 1)), vahvistus = None)
    val s5Luokka = ExamplesEuropeanSchoolOfHelsinki.s5.copy(alkamispäivä = Some(date(2021, 8, 1)), vahvistus = None)
    val s6luokka = ExamplesEuropeanSchoolOfHelsinki.s6.copy(alkamispäivä = Some(date(2021, 8, 1)), vahvistus = None)
    val s7luokka = ExamplesEuropeanSchoolOfHelsinki.s7.copy(alkamispäivä = Some(date(2021, 8, 1)), vahvistus = None)

    "Maksuttomuus-tiedon voi siirtää jos opiskeluoikeudella on s5-vuosiluokan suoritus" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(s5Luokka)), KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021) {
        verifyResponseStatusOk()
      }
    }
    "Maksuttomuus-tiedon voi siirtää jos opiskeluoikeudella on s6-vuosiluokan suoritus, koska se tulkitaan 'lukiotason suoritukseksi'" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(s6luokka)), KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021) {
        verifyResponseStatusOk()
      }
    }
    "Maksuttomuus-tiedon voi siirtää jos opiskeluoikeudella on s7-vuosiluokan suoritus, koska se tulkitaan 'lukiotason suoritukseksi'" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(s7luokka)), KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021) {
        verifyResponseStatusOk()
      }
    }
    "Maksuttomuus-tietoa ei voi siirtää jos on pelkästään muun vuosiluokan suorituksia" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(s4Luokka)), KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä koulutus ei siirrettyjen tietojen perusteella kelpaa oppivelvollisuuden suorittamiseen (tarkista, että koulutuskoodi, käytetyn opetussuunnitelman perusteen diaarinumero, suorituksen tyyppi ja/tai suoritustapa ovat oikein)."))
      }
    }
  }

  "International school MYPVuosiluokanSuoritus" - {
    val lisätiedot = InternationalSchoolOpiskeluoikeudenLisätiedot(maksuttomuus = Some(List(Maksuttomuus(date(2021, 8, 1), None, true))))
    val opiskeluoikeus = ExamplesInternationalSchool.opiskeluoikeus.copy(
      suoritukset = Nil,
      tila = InternationalSchoolOpiskeluoikeudenTila(List(InternationalSchoolOpiskeluoikeusjakso(date(2021, 8, 1), LukioExampleData.opiskeluoikeusAktiivinen))),
      lisätiedot = Some(lisätiedot)
    )
    val ysiLuokka: MYPVuosiluokanSuoritus = ExamplesInternationalSchool.grade9.copy(alkamispäivä = Some(date(2021, 8, 1)), vahvistus = None)
    val kymppiLuokka: MYPVuosiluokanSuoritus = ExamplesInternationalSchool.grade10.copy(alkamispäivä = Some(date(2021, 8, 1)), vahvistus = None)

    "Maksuttomuus-tiedon voi siirtää jos opiskeluoikeudella on 10. vuosiluokan MYP-suoritus, koska se tulkitaan 'lukiotason suoritukseksi'" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(kymppiLuokka)), KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021) {
        verifyResponseStatusOk()
      }
    }
    "Maksuttomuus-tietoa ei voi siirtää jos on pelkästään muun vuosiluokan MYP-suorituksia" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(ysiLuokka)), KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä koulutus ei siirrettyjen tietojen perusteella kelpaa oppivelvollisuuden suorittamiseen (tarkista, että koulutuskoodi, käytetyn opetussuunnitelman perusteen diaarinumero, suorituksen tyyppi ja/tai suoritustapa ovat oikein)."))
      }
    }
  }

  "Maksuttomuus-jaksot" - {
    val opiskeluoikeusAlkamispäivällä = alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
    "Jakson päättymispäiväksi päätellään aina seuraavan jakson alkamispäivä, useita" in {
      val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
      putMaksuttomuus(List(
        Maksuttomuus(date(2021, 8, 1), None, false),
        Maksuttomuus(date(2021, 9, 2), Some(date(2030, 3, 3)), true),
        Maksuttomuus(date(2021, 10, 2),Some(date(2030, 3, 3)), true)
      ), oppija, opiskeluoikeusAlkamispäivällä) {
        verifyResponseStatusOk()
      }

      getTallennetutMaksuttomuusJaksot(oppija) shouldBe List(
        Maksuttomuus(date(2021, 8, 1), Some(date(2021, 9, 1)), false),
        Maksuttomuus(date(2021, 9, 2), Some(date(2021, 10, 1)), true),
        Maksuttomuus(date(2021, 10, 2), None, true)
      )
    }
    "Jakson päättymispäiväksi päätellään aina seuraavan jakson alkamispäivä, vain yksi" in {
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021)
      val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
      putMaksuttomuus(List(Maksuttomuus(date(2021, 8, 1), Some(date(2021, 8, 1)), false)), oppija, opiskeluoikeusAlkamispäivällä) {
        verifyResponseStatusOk()
      }
      getTallennetutMaksuttomuusJaksot(oppija) shouldBe List(
        Maksuttomuus(date(2021, 8, 1), None, false)
      )
    }
    "Jaksoilla ei saa olla samoja alkamispäiviä" in {
      val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
      putMaksuttomuus(List(
        Maksuttomuus(date(2021, 8, 1), None, false),
        Maksuttomuus(date(2021, 9, 2), None, true),
        Maksuttomuus(date(2021, 9, 2), None, true),
        Maksuttomuus(date(2021, 10, 2), None, true)
      ), oppija, opiskeluoikeusAlkamispäivällä) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, joilla on sama alkupäivä 2021-09-02, 2021-09-02"))
      }
    }
    "Siirretyt jaksot järjestetään päivämäärä järjestykseen jakson alkamispäivän mukaan" in {
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021)
      val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
      putMaksuttomuus(List(
        Maksuttomuus(date(2021, 8, 1), None,false),
        Maksuttomuus(date(2021, 10, 1), None, true),
        Maksuttomuus(date(2021, 9, 1), None, true)
      ), oppija, opiskeluoikeusAlkamispäivällä) {
        verifyResponseStatusOk()
      }

      getTallennetutMaksuttomuusJaksot(oppija) shouldBe List(
        Maksuttomuus(date(2021, 8, 1), Some(date(2021, 8, 31)), false),
        Maksuttomuus(date(2021, 9, 1), Some(date(2021, 9, 30)), true),
        Maksuttomuus(date(2021, 10, 1), None, true)
      )
    }
    "Jaksojen tulee olla opiskeluoikeuden voimassaolon sisällä" - {
      lazy val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
      val alkamispaiva = date(2021, 8, 2)
      val paattymispaiva = date(2021, 12, 12)
      val opiskeluoikeus = päättymispäivällä(alkamispäivällä(defaultOpiskeluoikeus, alkamispaiva), paattymispaiva)

      "Ei sallita jakson alkamispäivää ennen opiskeluoikeuden alkua" in {
        putMaksuttomuus(List(
          Maksuttomuus(date(2021, 8, 1), None, true),
          Maksuttomuus(date(2021, 9, 2), None, true)
        ), oppija, opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, jonka alkupäivä 2021-08-01 ei ole opiskeluoikeuden voimassaolon (2021-08-02 - 2021-12-12) sisällä"))
        }
      }
      "Ei sallita jakson alkamispäivää opiskeluoikeuden päättymisen jälkeen" in {
        putMaksuttomuus(List(
          Maksuttomuus(date(2021, 8, 1), None, true),
          Maksuttomuus(date(2021, 12, 13), None, true)
        ), oppija, opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, jonka alkupäivä 2021-08-01, 2021-12-13 ei ole opiskeluoikeuden voimassaolon (2021-08-02 - 2021-12-12) sisällä"))
        }
      }
    }
  }

  "Maksuttomuutta pidennetty" - {
    lazy val oppija = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
    val alkamispaiva = date(2021, 8, 2)
    val paattymispaiva = date(2021, 12, 12)
    val maksuttomuusJakso = Some(List(Maksuttomuus(alkamispaiva, None, maksuton = true)))
    val opiskeluoikeus = päättymispäivällä(alkamispäivällä(defaultOpiskeluoikeus, alkamispaiva), paattymispaiva)

    "Ei sallita jakson alkamispäivää opiskeluoikeuden päättymisen jälkeen" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 12, 13), date(2021, 12, 14))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (2021-08-02 - 2021-12-12) sisällä 2021-12-13 – 2021-12-14"))
      }
    }
    "Ei sallita jakson päättymispäivää jälkeen opiskeluoikeuden päättymisen" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 2), date(2021, 12, 13))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (2021-08-02 - 2021-12-12) sisällä 2021-08-02 – 2021-12-13"))
      }
    }
    "Jakson päättymispäivä ei voi olla ennen jakson alkamispäivää" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 3), date(2021, 8, 2))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka loppupäivä on aikaisemmin kuin alkupäivä. 2021-08-03 (alku) - 2021-08-02 (loppu)"))
      }
    }
    "Jaksot järjestetään päivämääräjärjestykseen" in {
      val opiskeluoikeus = päättymispäivällä(alkamispäivällä(defaultOpiskeluoikeus, alkamispaiva), date(2025, 8, 4))

      mitätöiOppijanKaikkiOpiskeluoikeudet(oppija)

      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2025, 8, 2), date(2025, 8, 2)),
        OikeuttaMaksuttomuuteenPidennetty(date(2025, 8, 4), date(2025, 8, 4)),
        OikeuttaMaksuttomuuteenPidennetty(date(2025, 8, 3), date(2025, 8, 3))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatusOk()
      }
      getTallennetutOikeuttaMaksuttomuuteenPidennettyJaksot(oppija) shouldBe(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2025, 8, 2), date(2025, 8, 2)),
        OikeuttaMaksuttomuuteenPidennetty(date(2025, 8, 3),date(2025, 8, 3)),
        OikeuttaMaksuttomuuteenPidennetty(date(2025, 8, 4),date(2025, 8, 4))
      ))
    }
    "Jaksojen voimassaolot eivät saa olla päällekkäisiä" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 2), date(2021, 10, 10)),
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 10, 10), date(2021, 12, 12))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyviä jaksoja, jotka ovat keskenään päällekkäisiä (2021-08-02 – 2021-10-10,2021-10-10 – 2021-12-12)"))
      }
    }
    "Pidennys jakson tulee olla maksuttuman jakson sisällä" - {
      "Pidennys alkanut maksullisella jaksolla" in {
        val maksuttomuusJaksot = Some(List(
          Maksuttomuus(alkamispaiva, None, maksuton = true),
          Maksuttomuus(alkamispaiva.plusDays(10), None, maksuton = false),
          Maksuttomuus(alkamispaiva.plusDays(20), None, maksuton = true)
        ))

        val pidennykset = List(
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva, alkamispaiva.plusDays(9)),
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva.plusDays(19), alkamispaiva.plusDays(30))
        )

        putMaksuttomuuttaPidennetty(pidennykset, oppija, opiskeluoikeus, maksuttomuusJaksot) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Maksuttomuutta voidaan pidetäntää vain aikavälillä jolloin koulutus on maksutontonta"))
        }
      }
      "Pidennys päättyy maksullisella jaksolla" in {
        val maksuttomuusJaksot = Some(List(
          Maksuttomuus(alkamispaiva, None, maksuton = true),
          Maksuttomuus(alkamispaiva.plusDays(20), None, maksuton = false)
        ))

        val pidennykset = List(
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva, alkamispaiva.plusDays(20))
        )

        putMaksuttomuuttaPidennetty(pidennykset, oppija, opiskeluoikeus, maksuttomuusJaksot) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Maksuttomuutta voidaan pidetäntää vain aikavälillä jolloin koulutus on maksutontonta"))
        }
      }
    }

    val jaksoAikainen = OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2020, 10, 10),
      LocalDate.of(2020, 10, 15))
    val jaksoAikainenPäällekäinen = OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2020, 10, 12),
      LocalDate.of(2020, 10, 25))

    val jaksoKeskimmäinen = OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2020, 12, 10),
      LocalDate.of(2021, 1, 15))
    val jaksoKeskimmäinenSisäkkäinen = OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2020, 12, 20),
      LocalDate.of(2021, 1, 14))

    val jaksoMyöhäinen = OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2021, 10, 10),
      LocalDate.of(2021, 10, 15))
    val jaksoMyöhäinenHetiPerään = OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2021, 10, 15),
      LocalDate.of(2021, 10, 20))

    "Pidennyksen yhteenlasku" - {
      "Peräkkäiset aikajaksot tyhjällä välillä" in {
        OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(List(jaksoAikainen, jaksoMyöhäinen)) should equal (12)
      }
      "Peräkkäiset aikajaksot, suoraan toisiaan seuraavat" in {
        OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(List(jaksoMyöhäinen, jaksoMyöhäinenHetiPerään)) should equal (11)
      }
      "Päällekkäiset aikajaksot" in {
        OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(List(jaksoAikainen, jaksoAikainenPäällekäinen)) should equal (16)
      }
      "Sisäkkäiset aikajaksot" in {
        OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(List(jaksoKeskimmäinen, jaksoKeskimmäinenSisäkkäinen)) should equal (37)
      }
      "Peräkkäisiä, sisäkkäisiä ja erillisiä yhdessä" in {
        OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(List(
          jaksoMyöhäinenHetiPerään,
          jaksoAikainen,
          jaksoMyöhäinen,
          jaksoKeskimmäinen,
          jaksoAikainenPäällekäinen,
          jaksoKeskimmäinenSisäkkäinen)) should equal (64)
      }
      "Sama aikajakso kahdesti" in {
        OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(List(jaksoKeskimmäinen, jaksoKeskimmäinen)) should equal (37)
      }
    }
  }

  "Maksuttomuustietojen vaatiminen niiden puuttuessa" - {
    val oppija = vuonna2004SyntynytPeruskouluValmis2021
    val alkamispaiva = vuonna2004SyntynytPeruskouluValmis2021.syntymäaika.get.plusYears(20).atEndOfYear
    val paattymispaiva = alkamispaiva.plusYears(1)
    val opiskeluoikeus = päättymispäivällä(alkamispäivällä(defaultOpiskeluoikeus, alkamispaiva), paattymispaiva).copy(lisätiedot = None)

    "Maksuttomuustiedot vaaditaan, jos kaikki tietyt oppijan ja opiskeluoikeuden ehdot täyttyvät" in {
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus, oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta vaaditaan opiskeluoikeudelle."))
      }
    }

    "Maksuttomuustietoja ei vaadita jos on vapautus oppivelvollisuudesta" in {
      oppivelvollisuudestaVapautusService.db.lisääOppivelvollisuudestaVapautus(oppija.oid, "", LocalDate.of(2000, 8, 1), "091")

      setupOppijaWithOpiskeluoikeus(opiskeluoikeus, oppija) {
        verifyResponseStatusOk()
      }
    }

    "Maksuttomuustietoja ei vaadita, jos oppija on syntynyt ennen 2004" in {
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus, vuonna2003SyntynytPeruskouluValmis2021) {
        verifyResponseStatusOk()
      }
    }

    "Maksuttomuustietoja ei vaadita, jos oppija on valmistunut perusopetuksesta ennen 1.1.2021" in {
      putOpiskeluoikeus(opiskeluoikeus, vuonna2004SyntynytMuttaPeruskouluValmisEnnen2021) {
        verifyResponseStatusOk()
      }
    }

    "Maksuttomuustietoja ei vaadita, jos opinnot ovat alkaneet myöhemmin kuin sen vuoden lopussa, jolloin oppija täyttää 20 vuotta" in {
      val alkamispäivä = oppija.syntymäaika.get.plusYears(20).atEndOfYear.plusDays(1)
      val opiskeluoikeus = päättymispäivällä(alkamispäivällä(defaultOpiskeluoikeus, alkamispäivä), alkamispäivä.plusDays(10)).copy(lisätiedot = None)
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus, oppija) {
        verifyResponseStatusOk()
      }
    }

    "Maksuttomuustietoja ei vaadita, jos koulutus ei kelpaa oppivelvollisuuden suorittamiseen" in {
      val opiskeluoikeus = ExamplesTaiteenPerusopetus.Opiskeluoikeus.aloitettuYleinenOppimäärä
      putOpiskeluoikeus(opiskeluoikeus, oppija) {
        verifyResponseStatusOk()
      }
    }

    "Maksuttomuustietoja ei vaadita, jos oppijalle ei löydy kotikuntahistoriaa" in {
      val oppija = vuonna2004SyntynytPeruskouluValmis2021EiKotikuntahistoriaa
      putOpiskeluoikeus(opiskeluoikeus, oppija) {
        verifyResponseStatusOk()
      }
    }

    "Maksuttomuustietoja ei vaadita, jos oppija ei kotikuntahistorian perusteella ole oppivelvollisuuslain alainen" in {
      val oppija = vuonna2004SyntynytPeruskouluValmis2021MuuttanutSuomeenTäysiIkäisenä
      putOpiskeluoikeus(opiskeluoikeus, oppija) {
        verifyResponseStatusOk()
      }
    }
  }

  private def setupOppijaWithMaksuttomuus(jaksot: List[Maksuttomuus], oppija: OppijaHenkilö, oo: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus)(verifyStatus: => Any) = {
    val lisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot(hojks = None, maksuttomuus = Some(jaksot))
    val opiskeluoikeus = oo.copy(lisätiedot = Some(lisatiedot))

    setupOppijaWithOpiskeluoikeus(opiskeluoikeus, oppija) {
      verifyStatus
    }
  }

  private def putMaksuttomuus(jaksot: List[Maksuttomuus], oppija: OppijaHenkilö, oo: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus)(verifyStatus: => Any) = {
    val lisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot(hojks = None, maksuttomuus = Some(jaksot))
    val opiskeluoikeus = oo.copy(lisätiedot = Some(lisatiedot))

    putOpiskeluoikeus(opiskeluoikeus, oppija) {
      verifyStatus
    }
  }

  private def putMaksuttomuus(jaksot: List[Maksuttomuus], oppija: OppijaHenkilö, oo: LukionOpiskeluoikeus)(verifyStatus: => Any) = {
    val lisatiedot = LukionOpiskeluoikeudenLisätiedot(maksuttomuus = Some(jaksot))
    val opiskeluoikeus = oo.copy(lisätiedot = Some(lisatiedot))

    putOpiskeluoikeus(opiskeluoikeus, oppija) {
      verifyStatus
    }
  }

  private def putMaksuttomuuttaPidennetty(jaksot: List[OikeuttaMaksuttomuuteenPidennetty], oppija: OppijaHenkilö, oo: AmmatillinenOpiskeluoikeus, maksuttomuus: Option[List[Maksuttomuus]] = None)(verifyStatus: => Any) = {
    val lisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot(hojks = None, oikeuttaMaksuttomuuteenPidennetty = Some(jaksot), maksuttomuus = maksuttomuus)
    val opiskeluoikeus = oo.copy(lisätiedot = Some(lisatiedot))

    putOpiskeluoikeus(opiskeluoikeus, oppija) {
      verifyStatus
    }
  }

  private def getTallennetutMaksuttomuusJaksot(oppija: OppijaHenkilö) = {
    val maksuttomuusjaksot = lastOpiskeluoikeus(oppija.oid).lisätiedot.collect {
      case m: MaksuttomuusTieto => m.maksuttomuus.toList.flatten
    }
    maksuttomuusjaksot shouldBe defined
    maksuttomuusjaksot.get
  }

  private def getTallennetutOikeuttaMaksuttomuuteenPidennettyJaksot(oppija: OppijaHenkilö) = {
    val jaksot = lastOpiskeluoikeus(oppija.oid).lisätiedot.collect {
      case m: MaksuttomuusTieto => m.oikeuttaMaksuttomuuteenPidennetty.toList.flatten
    }
    jaksot shouldBe defined
    jaksot.get
  }

  private def mitätöiOpiskeluoikeus(oo: PerusopetuksenOpiskeluoikeus) = {
    delete(s"api/opiskeluoikeus/${oo.oid.get}", headers = authHeaders())(verifyResponseStatusOk())
    oo
  }
}
