package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.vahvistusPaikkakunnalla
import fi.oph.koski.documentation.PerusopetusExampleData.perusopetuksenOppimääränSuoritus
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExamplesAikuistenPerusopetus, ExamplesInternationalSchool, LukioExampleData, PerusopetusExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.koskiSpecificOppijat
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class MaksuttomuusSpec extends FreeSpec with OpiskeluoikeusTestMethodsAmmatillinen with KoskiHttpSpec {

  "Tiedon siirtäminen" - {
    lazy val opiskeluoikeus = alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
    "Testattavan opiskeluoikeuden suoritus on merkitty vaativan maksuttomuustiedon lisätiedoilta" in {
      opiskeluoikeus.suoritukset.collectFirst { case s: SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta => s }.isDefined shouldBe(true)
    }
    "Vaaditaan vuonna 2004 tai sen jälkeen syntyneiltä, joiden opiskeluoikeus on alkanut 1.8.2021 ja sisältää suorituksen joka vaatii maksuttomuus tiedon" in {
      putOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta puuttuu."))
      }
    }
    "Ei saa siirtää jos opiskeluoikeus on alkanut ennen 1.8.2021" in {
      putMaksuttomuus(
        List(
          Maksuttomuus(date(2021, 8, 1), None, true)
        ),
        KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
        alkamispäivällä(defaultOpiskeluoikeus, date(2021, 7, 31))
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.8.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä."))
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
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.8.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä."))
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
        KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen, oo
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.8.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä."))
      }
    }
    "Siirto kun opiskelijalla perusopetuksen päättötodistus tai siihen verrattavissa oleva suoritus" - {
      "Ei saa siirtää jos suoritus vahvistettu ennen Valpas-lain voimaantuloaikaa" - {
        "Aikuisten perusopetuksen oppimäärä" in {
          val opiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

          putOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskelija on suorittanut perusopetuksen, aikuisten perusopetuksen oppimäärän tai International Schoolin 9. vuosiluokan ennen 1.1.2021."))
          }

          resetFixtures()
        }

        "Perusopetuksen oppimäärä" in {
          val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
            suoritukset = List(perusopetuksenOppimääränSuoritus.copy(
              vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 1, 1))
            )),
            alkamispäivä = date(2010, 8, 1),
            päättymispäivä = Some(date(2020, 8, 1)),
          )

          putOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskelija on suorittanut perusopetuksen, aikuisten perusopetuksen oppimäärän tai International Schoolin 9. vuosiluokan ennen 1.1.2021."))
          }

          resetFixtures()
        }

        "International Schoolin ysiluokka" in {
          val opiskeluoikeus = ExamplesInternationalSchool.opiskeluoikeus.copy(
            suoritukset = Nil,
          )
          val ysiLuokka: MYPVuosiluokanSuoritus = ExamplesInternationalSchool.grade9.copy(alkamispäivä = Some(date(2015, 6, 30)))

          putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(ysiLuokka)), KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskelija on suorittanut perusopetuksen, aikuisten perusopetuksen oppimäärän tai International Schoolin 9. vuosiluokan ennen 1.1.2021."))
          }

          resetFixtures()
        }
        "Linkitetty oppija - slavella siirron estävä oppimäärä" in {
          val opiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

          putOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1.henkilö) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oppivelvollisuustietoMaster,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskelija on suorittanut perusopetuksen, aikuisten perusopetuksen oppimäärän tai International Schoolin 9. vuosiluokan ennen 1.1.2021."))
          }

          resetFixtures()
        }
        "Linkitetty oppija - masterilla siirron estävä oppimäärä" in {
          val opiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

          putOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oppivelvollisuustietoMaster) {
            verifyResponseStatusOk()
          }

          putMaksuttomuus(
            List(Maksuttomuus(date(2021, 8, 1), None, true)),
            KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1.henkilö,
            alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
          ) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskelija on suorittanut perusopetuksen, aikuisten perusopetuksen oppimäärän tai International Schoolin 9. vuosiluokan ennen 1.1.2021."))
          }

          resetFixtures()
        }
      }
      "Saa siirtää jos suoritus vahvistettu Valpas-lain voimaantulopäivän jälkeen" in {
        val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(
          suoritukset = List(perusopetuksenOppimääränSuoritus.copy(
            vahvistus = vahvistusPaikkakunnalla(päivä = date(2021, 1, 1))
          )),
          alkamispäivä = date(2010, 8, 1),
          päättymispäivä = Some(date(2021, 8, 1)),
        )

        putOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
          verifyResponseStatusOk()
        }

        putMaksuttomuus(
          List(Maksuttomuus(date(2021, 8, 1), None, true)),
          KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
          alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
        ) {
          verifyResponseStatusOk()
        }

        resetFixtures()
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
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(kymppiLuokka)), KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
        verifyResponseStatusOk()
      }
    }
    "Maksuttomuus-tietoa ei voi siirtää jos on pelkästään muun vuosiluokan MYP-suorituksia" in {
      putOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(ysiLuokka)), KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.8.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä."))
      }
    }
  }


  "Maksuttomuus-jaksot" - {
    val opiskeluoikeusAlkamispäivällä = alkamispäivällä(defaultOpiskeluoikeus, date(2021, 8, 1))
    "Jakson päättymispäiväksi päätellään aina seuraavan jakson alkamispäivä, useita" in {
      val oppija = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
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
      val oppija = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
      putMaksuttomuus(List(Maksuttomuus(date(2021, 8, 1), Some(date(2021, 8, 1)), false)), oppija, opiskeluoikeusAlkamispäivällä) {
        verifyResponseStatusOk()
      }
      getTallennetutMaksuttomuusJaksot(oppija) shouldBe List(
        Maksuttomuus(date(2021, 8, 1), None, false)
      )
    }
    "Jaksoilla ei saa olla samoja alkamispäiviä" in {
      val oppija = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
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
      val oppija = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
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
      lazy val oppija = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
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
    lazy val oppija = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
    val alkamispaiva = date(2021, 8, 2)
    val paattymispaiva = date(2021, 12, 12)
    val maksuttomuusJakso = Some(List(Maksuttomuus(alkamispaiva, None, maksuton = true)))
    val opiskeluoikeus = päättymispäivällä(alkamispäivällä(defaultOpiskeluoikeus, alkamispaiva), paattymispaiva)

    "Ei sallita jakson alkamispäivää opiskeluoikeuden päättymisen jälkeen" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 12, 13), date(2021, 12, 14))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (2021-08-02 - 2021-12-12) sisällä OikeuttaMaksuttomuuteenPidennetty(2021-12-13,2021-12-14)"))
      }
    }
    "Ei sallita jakson päättymispäivää jälkeen opiskeluoikeuden päättymisen" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 2), date(2021, 12, 13))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (2021-08-02 - 2021-12-12) sisällä OikeuttaMaksuttomuuteenPidennetty(2021-08-02,2021-12-13)"))
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
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 2), date(2021, 8, 2)),
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 4), date(2021, 8, 4)),
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 3), date(2021, 8, 3))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatusOk()
      }
      getTallennetutOikeuttaMaksuttomuuteenPidennettyJaksot(oppija) shouldBe(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 2), date(2021, 8, 2)),
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 3),date(2021, 8, 3)),
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 4),date(2021, 8, 4))
      ))
    }
    "Jaksojen voimassaolot eivät saa olla päällekkäisiä" in {
      putMaksuttomuuttaPidennetty(List(
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 8, 2), date(2021, 10, 10)),
        OikeuttaMaksuttomuuteenPidennetty(date(2021, 10, 10), date(2021, 12, 12))
      ), oppija, opiskeluoikeus, maksuttomuusJakso) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyviä jaksoja, jotka ovat keskenään päällekkäisiä (OikeuttaMaksuttomuuteenPidennetty(2021-08-02,2021-10-10),OikeuttaMaksuttomuuteenPidennetty(2021-10-10,2021-12-12))"))
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

  private def putMaksuttomuus(jaksot: List[Maksuttomuus], oppija: OppijaHenkilö, oo: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus)(verifyStatus: => Any) = {
    val lisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot(hojks = None, maksuttomuus = Some(jaksot))
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
}
