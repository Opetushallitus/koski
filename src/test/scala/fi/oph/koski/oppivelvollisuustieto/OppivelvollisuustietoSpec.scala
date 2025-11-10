package fi.oph.koski.oppivelvollisuustieto

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{opiskeluoikeusAktiivinen, opiskeluoikeusPäättynyt}
import fi.oph.koski.documentation.PerusopetusExampleData.yhdeksännenLuokanSuoritus
import fi.oph.koski.documentation.EuropeanSchoolOfHelsinkiExampleData.suoritusVahvistus
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.raportointikanta.{RaportointiDatabase, RaportointikantaTestMethods}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.schema._
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppivelvollisuustietoSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with OpiskeluoikeusTestMethodsAmmatillinen
    with RaportointikantaTestMethods
    with DirtiesFixtures {

  val validationContext = defaultKoskiApplication.validationContext

  val master = oppivelvollisuustietoMaster
  val slave1 = oppivelvollisuustietoSlave1.henkilö
  val slave2 = oppivelvollisuustietoSlave2.henkilö

  val testiOidit = List(master.oid, slave1.oid, slave2.oid)

  "Oppivelvollisuustieto" - {
    "Jos oppija ei ole oppivelvollisuuden piirissä, ei hänelle synny riviä oppivelvollisuustiedot-tauluun" - {
      "Slave oidilla perusopetuksen oppimäärä suoritettu ennen uuden oppivelvollisuuslain voimaantuloa -> ei rivejä" in {
        clearAndInsert(master, lukionOppimäärä(vahvistus = None))
        clearAndInsert(slave1, perusopetuksenOppimäärä(Some(date(2020, 12, 31))))
        clearAndInsert(slave2, ammatillinenTutkinto(vahvistus = None, lisääMaksuttomuus = false))
        reloadRaportointikanta
        queryTestioidit should equal(Nil)
      }
      "Master oidilla perusopetuksen oppimäärä suoritettu ennen uuden oppivelvollisuuslain voimaantuloa -> ei rivejä" in {
        clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2020, 12, 31))))
        clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = None, lisääMaksuttomuus = false))
        clearAndInsert(slave2, lukionOppimäärä(vahvistus = None, lisääMaksuttomuus = false))
        reloadRaportointikanta
        queryTestioidit should equal(Nil)
      }
      "Henkilö on liian vanha" in {
        clearAndInsert(oppivelvollisuustietoLiianVanha, lukionOppimäärä(vahvistus = None, lisääMaksuttomuus = false))
        reloadRaportointikanta
        queryOids(oppivelvollisuustietoLiianVanha.oid) shouldBe(Nil)
      }
      "Henkilö on suorittanut aikuisten perusopetuksen oppimäärän ennen vuotta 2021" in {
        clearAndInsert(oikeusOpiskelunMaksuttomuuteen, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineenValmistunutVanhanOppivelvollisuuslainAikana)
        reloadRaportointikanta
        queryOids(oikeusOpiskelunMaksuttomuuteen.oid) shouldBe(Nil)
      }
      "Henkilö on suorittanut international schoolin ysiluokan ennen vuotta 2021" in {
        clearAndInsert(oikeusOpiskelunMaksuttomuuteen, ExamplesInternationalSchool.opiskeluoikeus)
        reloadRaportointikanta
        queryOids(oikeusOpiskelunMaksuttomuuteen.oid) shouldBe(Nil)
      }
    }


    "Jos oppija on oppivelvollisuuden piirissä, löytyy samat tiedot hänen kaikilla oideilla" - {
      "Jos suorittaa ammatillista tutkintoa ja lukion oppimäärää erillään, käytetään aiempaa valmistumisaikaa päättymispäivien päättelyssä" - {
        "Ammatillisella tutkinnolla vahvistus -> vahvistus sen mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2019, 7, 1)), keskiarvo = Some(4.0))) // alku 2000-1-1
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = None)) // alku 2019-8-1
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2019, 7, 1), maksuttomuus = date(2019, 7, 1))
        }
        "Lukion oppimaaralla vahvistus (mutta ei YO-todistusta) -> iän mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = None))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = Some(date(2021, 10, 1)), alkamispäivä = date(2021, 8, 1)))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
        "Molemmilla vahvistus (mutta ei YO-todistusta) -> ammatillisen mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2019, 7, 1)), keskiarvo = Some(4.0)))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = Some(date(2021, 10, 1)), alkamispäivä = date(2021, 8, 1)))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2019, 7, 1), maksuttomuus = date(2019, 7, 1))
        }
      }
      "Jos suorittaa ammatillista tutkintoa ja lukion aineopintoja erillään, käytetään aiempaa valmistumisaikaa päättymispäivien päättelyssä" - {
        "Ammatillisella tutkinnolla vahvistus-> vahvistus sen mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2019, 7, 1)), keskiarvo = Some(4.0)))
          clearAndInsert(slave2, lukionAineopinnot(vahvistus = None)) // alku 2019-8-1
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2019, 7, 1), maksuttomuus = date(2019, 7, 1))
        }
        "Lukion oppimaaralla vahvistus (mutta ei YO-todistusta) -> iän mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = None))
          clearAndInsert(slave2, lukionAineopinnot(vahvistus = Some(date(2019, 10, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
        "Molemmilla vahvistus (mutta ei YO-todistusta) -> ammatillisen mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2019, 7, 1)), keskiarvo = Some(4.0)))
          clearAndInsert(slave2, lukionAineopinnot(vahvistus = Some(date(2019, 10, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2019, 7, 1), maksuttomuus = date(2019, 7, 1))
        }
      }

      "Jos suorittaa vain lukion oppimäärää, käytetään aina syntymäaikaa päättymispäivien päättelyssä" - {
        "Lukion oppimaaralla vahvistus" in {
          clearAndInsert(master, lukionOppimäärä(vahvistus = Some(date(2022, 1, 1)), alkamispäivä = date(2021, 8, 1)))
          clearAndInsert(slave1, lukionOppimäärä(vahvistus = Some(date(2023, 1, 1)), alkamispäivä = date(2022, 8, 1)))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = Some(date(2024, 1, 1)), alkamispäivä = date(2023, 8, 1)))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos on suorittanut YO-tutkinnon" - {
        "eikä mitään oppivelvollisuuden suorittamiseen hyväksyttyjä 2. asteen opintoja" - {
          "käytetään päättymispäivänä YO-tutkinnon vahvistuspäivää" in {
            resetFixtures
            queryOids(pelkkäYoKannassaUudenOvLainPiirissä.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYoKannassaUudenOvLainPiirissä.oid,
                oppivelvollisuusVoimassaAsti = date(2012, 11, 30),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2012, 11, 30)
              )
            ))
          }
        }
        "ja muista 2. asteen opinnoista vain lukio-opintoja" - {
          "käytetään päättymispäivänä YO-tutkinnon vahvistuspäivää, vaikka lukio-opinnot olisi vahvistettu myöhemmin" in {
            clearAndInsert(pelkkäYoKannassaUudenOvLainPiirissä, lukionOppimäärä(vahvistus = Some(date(2022, 1, 2))))
            reloadRaportointikanta
            queryOids(pelkkäYoKannassaUudenOvLainPiirissä.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYoKannassaUudenOvLainPiirissä.oid,
                oppivelvollisuusVoimassaAsti = date(2012, 11, 30),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2012, 11, 30)
              )
            ))
          }
        }
        "ja lukiosta poikkeavia muita keskeneräisiä 2. asteen opintoja" - {
          "käytetään päättymispäivänä YO-tutkinnon vahvistuspäivää" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(
              oppija = pelkkäYoKannassaUudenOvLainPiirissä,
              vahvistusEB = None,
              päättymispäivä = None
            )
            reloadRaportointikanta
            queryOids(pelkkäYoKannassaUudenOvLainPiirissä.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYoKannassaUudenOvLainPiirissä.oid,
                oppivelvollisuusVoimassaAsti = date(2012, 11, 30),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2012, 11, 30)
              )
            ))
          }
        }
        "ja lukiosta poikkeavia muita vahvistettuja 2. asteen opintoja" - {
          "käytetään päättymispäivänä aikaisinta päivämäärää" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(
              oppija = pelkkäYoKannassaUudenOvLainPiirissä,
              vahvistusEB = Some(date(2021, 1, 1)),
              päättymispäivä = Some(date(2023, 1, 1))
            )
            reloadRaportointikanta
            queryOids(pelkkäYoKannassaUudenOvLainPiirissä.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYoKannassaUudenOvLainPiirissä.oid,
                oppivelvollisuusVoimassaAsti = date(2012, 11, 30),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2012, 11, 30)
              )
            ))
          }
        }
        "ja vahvistettuja ammattiopintoja, ei lukio-opintoja" - {
          "käytetään päättymispäivänä YO-tutkinnon vahvistuspäivää" in {
            clearAndInsert(pelkkäYoKannassaUudenOvLainPiirissä, ammatillinenTutkinto(vahvistus = Some(date(2016, 8, 1)), keskiarvo = Some(4.0), alkamispäivä = date(2015, 8, 1)))
            reloadRaportointikanta
            queryOids(pelkkäYoKannassaUudenOvLainPiirissä.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYoKannassaUudenOvLainPiirissä.oid,
                oppivelvollisuusVoimassaAsti = date(2012, 11, 30),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2012, 11, 30)
              )
            ))
          }
        }

        "ja päättyneitä mutta vahvistamattomia ammattiopintoja (eronnut)" - {
          "käytetään päättimyspäivänä YO-todistuksen vahvistuspäivää" in {
            clearAndInsert(pelkkäYo2021, lisääTila(ammatillinenTutkinto(vahvistus = None, keskiarvo = None, alkamispäivä = date(2015, 8, 1)), date(2015, 8, 2), opiskeluoikeusEronnut))
            reloadRaportointikanta
            queryOids(pelkkäYo2021.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYo2021.oid,
                oppivelvollisuusVoimassaAsti = date(2021, 9, 5),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2021, 9, 5)
              )
            ))
          }
        }

        "ja vahvistettuja ammattiopintoja samaan aikaan kuin lukio-opintoja, YO todistus saatu myöhemmin" - {
          "käytetään päättimyspäivänä YO-todistuksen vahvistuspäivää" in {
            clearAndInsert(pelkkäYo2021, ammatillinenTutkinto(vahvistus = Some(date(2021, 1, 1)), keskiarvo = Some(4.0), alkamispäivä = date(2019, 8, 2)),
              lukionOppimäärä(vahvistus = Some(date(2021, 9, 5)), alkamispäivä = date(2019, 8, 1))) // yo: 2021-09-05
            reloadRaportointikanta
            queryOids(pelkkäYo2021.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYo2021.oid,
                oppivelvollisuusVoimassaAsti = date(2021, 1, 1),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2021, 9, 5)
              )
            ))
          }
        }

        "ja vahvisteettuja ammattiopintoja samaan aikaan kuin lukio-opintoja, ammattiopinnot vahvistettu myöhemmin" - {
          "käytetään päättimyspäivänä ammattiopintiojen vahvistuspäivää" in {
            clearAndInsert(pelkkäYo2021, ammatillinenTutkinto(vahvistus = Some(date(2021, 10, 1)), keskiarvo = Some(4.0), alkamispäivä = date(2019, 8, 2)),
              lukionOppimäärä(vahvistus = Some(date(2021, 9, 5))))//alku: 2019-8-1
            reloadRaportointikanta
            queryOids(pelkkäYo2021.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYo2021.oid,
                oppivelvollisuusVoimassaAsti = date(2021, 9, 5),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2021, 10, 1)
              )
            ))
          }
        }

        "ja vahvistamattomia ammattiopintoja samaan aikaan kuin lukio-opintoja" - {
          "on päättymispäivä iän mukaan" in {
            clearAndInsert(pelkkäYo2021, ammatillinenTutkinto(vahvistus = None, keskiarvo = None, alkamispäivä = date(2019, 8, 2)),
              lukionOppimäärä(vahvistus = Some(date(2021, 9, 5))))//alku: 2019-8-1
            reloadRaportointikanta
            queryOids(pelkkäYo2021.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYo2021.oid,
                oppivelvollisuusVoimassaAsti = date(2021, 9, 5),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2025, 12, 31)
              )
            ))
          }
        }

        "ja vahvistamattomia ammattiopintoja ilman lukio opintoja" - {
          "käytetään päättymispäivänä YO-tutkinnon vahvistuspäivää" in {
            clearAndInsert(pelkkäYoKannassaUudenOvLainPiirissä, ammatillinenTutkinto(vahvistus = None))
            reloadRaportointikanta
            queryOids(pelkkäYoKannassaUudenOvLainPiirissä.oid) should be(List(
              Oppivelvollisuustieto(
                pelkkäYoKannassaUudenOvLainPiirissä.oid,
                oppivelvollisuusVoimassaAsti = date(2012, 11, 30),
                oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2012, 11, 30)
              )
            ))
          }
        }


      }

      "Jos suorittaa vain ammatillista tutkintoa, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
        "Syntymäaika päättää oppivelvollisuuden aikaisemmin, ensimmäisenä vahvistetun tutkinnon vahvistus päättää oikeuden maksuttomuuteen aikaisemmin" in {
          validationContext.runWithoutValidations {
            clearAndInsert(master, ammatillinenTutkinto(vahvistus = Some(date(2030, 1, 1)), keskiarvo = Some(4.0)))
            clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2023, 1, 1)), keskiarvo = Some(4.0)))
            clearAndInsert(slave2, ammatillinenTutkinto(vahvistus = Some(date(2024, 1, 1)), keskiarvo = Some(4.0)))
          }
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2023, 1, 1))
        }
        "Syntymäaika päättää molemmat aikaisemmin" in {
          validationContext.runWithoutValidations {
            clearAndInsert(master, ammatillinenTutkinto(vahvistus = None))
            clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2025, 1, 1)), keskiarvo = Some(4.0)))
            clearAndInsert(slave2, ammatillinenTutkinto(vahvistus = Some(date(2024, 12, 31)), keskiarvo = Some(4.0)))
          }
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          validationContext.runWithoutValidations {
            clearAndInsert(master, ammatillinenTutkinto(vahvistus = Some(date(2021, 1, 1)), keskiarvo = Some(4.0)))
            clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2025, 1, 1)), keskiarvo = Some(4.0)))
            clearAndInsert(slave2, ammatillinenTutkinto(vahvistus = None))
          }
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa vain international schoolia, käytetään päättymispäivänä ikään perustuvia päiviä, koska international schoolia ei katsota ov päättäväksi tutkinnoksi" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, internationalSchoolToinenAste(päättymispäivä = Some(date(2021, 1, 1))))
          clearAndInsert(slave1, internationalSchoolToinenAste(alkamispäivä = date(2021, 1, 2), päättymispäivä = Some(date(2025, 1, 1))))
          clearAndInsert(slave2, internationalSchoolToinenAste(alkamispäivä = date(2025, 1, 2), päättymispäivä = None, lisääMaksuttomuus = false))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa international schoolia ja lukiota, käytetään päättymispäivänä ikään perustuvia päiviä, koska kumpaakaan ei katsota ov päättäväksi tutkinnoksi" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, internationalSchoolToinenAste(päättymispäivä = Some(date(2021, 1, 1))))
          clearAndInsert(slave1, internationalSchoolToinenAste(alkamispäivä = date(2021, 1, 2), päättymispäivä = Some(date(2025, 1, 1))))
          clearAndInsert(slave2, lukionOppimäärä(alkamispäivä = date(2025, 1, 2), vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa international schoolia, lukiota ja ammattikoulua, käytetään päättymispäivänä ikään perustuvia vahvistuspäiviä, koska mitään ov-päättävää tutkintoa ei katsota olevan" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, internationalSchoolToinenAste(päättymispäivä = Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = None))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos on suorittanut IB-tutkinnon ja international schoolin, käytetään päättymispäivänä ikään perustuvaa vahvistuspäivää, koska kumpikaan ei ole ov-päättävä tutkinto" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 3, 3))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ibTutkinto(alkamispäivä = date(2021, 3, 4), ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, internationalSchoolToinenAste(päättymispäivä = Some(date(2021, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "European School of Helsinki" - {

        "Jos suorittaa vain European school of Helsinkiä, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(master, vahvistusEB = Some(date(2021, 1, 1)), päättymispäivä = Some(date(2023, 1, 1)))
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave1, vahvistusEB = Some(date(2025, 1, 1)), alkamispäivä = date(2023, 1, 2), päättymispäivä = Some(date(2025, 1, 1)))
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave2, vahvistusEB = None, alkamispäivä = date(2025, 1, 2), päättymispäivä = None, lisääMaksuttomuus = false)
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
          }
        }

        "Jos suorittaa European school of Helsinkiä ja lukiota, käytetään päättymispäivänä EB-tutkinnon vahvistuspäivää" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(master, vahvistusEB = Some(date(2021, 1, 1)), päättymispäivä = Some(date(2023, 1, 1)))
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave1, vahvistusEB = Some(date(2025, 1, 1)), alkamispäivä = date(2023, 1, 2), päättymispäivä = Some(date(2025, 1, 1)))
            insert(slave2, lukionOppimäärä(vahvistus = None))
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
          }
        }

        "Jos suorittaa European school of Helsinkiä, lukiota ja ammattikoulua, käytetään päättymispäivänä EB-tutkinnon vahvistuspäivää" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(master, vahvistusEB = Some(date(2021, 1, 1)), päättymispäivä = Some(date(2023, 1, 1)))
            insert(slave1, ammatillinenTutkinto(vahvistus = None))
            insert(slave2, lukionOppimäärä(vahvistus = None))
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
          }
        }

        "Jos on suorittanut IB-tutkinnon ja European school of Helsingin, käytetään päättymispäivänä EB-tutkinnon vahvistuspäivää, koska IB-merkintä Koskessa ei ole ov:n päättävä tutkinto" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
            insert(slave1, ibTutkinto(alkamispäivä = date(2021, 1, 2), ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave2, vahvistusEB = Some(date(2021, 3, 3)), päättymispäivä = Some(date(2023, 1, 1)))
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 3, 3), maksuttomuus = date(2021, 3, 3))
          }
        }
      }

      "Jos suorittaa vain IB-tutkintoa, käytetään päättymispäivänä ikään perustuvaa päivää, koska Koskeen vahvistettu ib-suoritus ei päätä oppivelvollisuutta" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ibTutkinto(alkamispäivä = date(2021, 1, 2), ibTutkinnonVahvistus = Some(date(2024, 7, 30))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, ibTutkinto(alkamispäivä = date(2024, 7, 31), ibTutkinnonVahvistus = None).withLisääPuuttuvaMaksuttomuustieto)
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa IB-tutkintoa ja lukiota, käytetään päättymispäivänä ikään perustuvaa päivää, koska kumpikaan merkintä ei ole ov:n päättävä tutkinto" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ibTutkinto(alkamispäivä = date(2021, 1, 2), ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa IB-tutkintoa, lukiota ja ammattikoulua, käytetään päättymispäivänä ikään perustuvia päiviä, koska ov-päättävää tutkintoa ei ole" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = None))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos on suorittanut DIA-tutkinnon, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, diaTutkinto(diaTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, diaTutkinto(alkamispäivä = date(2021, 1, 2), diaTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, diaTutkinto(alkamispäivä = date(2025, 1, 2), diaTutkinnonVahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Ei ole vielä suorittamassa toisen asteen tutkintoa" - {
        "Käytetään syntymäaikaa" in {
          clearAndInsert(oikeusOpiskelunMaksuttomuuteen, perusopetuksenOppimäärä(vahvistus = Some(date(2021, 6, 1))))
          reloadRaportointikanta
          queryOids(oikeusOpiskelunMaksuttomuuteen.oid) should equal(List(
            Oppivelvollisuustieto(
              oikeusOpiskelunMaksuttomuuteen.oid,
              oppivelvollisuusVoimassaAsti = date(2022, 12, 30),
              oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2024, 12, 31)
            )
          ))
        }
      }

      "Jos opiskeluoikeuksissa on pidennetty oikeutta maksuttomuuteen" in {
        val alkamispaiva = date(2025, 1, 1)

        val maksuttomuusJaksot = Some(List(
          Maksuttomuus(alkamispaiva, None, maksuton = true),
        ))

        val pidennyksetMaster = Some(List(
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva, alkamispaiva.plusDays(20)),
        ))
        val pidennyksetSlave = Some(List(
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva.plusDays(25), alkamispaiva.plusDays(30)),
        ))

        clearAndInsert(master, ammatillinenTutkintoMaksuttomuusJaksoilla(vahvistus = None,
          maksuttomuusJaksot = maksuttomuusJaksot,
          maksuttomuudenPidennyksenJaksot = pidennyksetMaster))

        clearAndInsert(slave1, lukionOppimääräJaMaksuttomuus(vahvistus = None,
          maksuttomuusJaksot = maksuttomuusJaksot,
          maksuttomuudenPidennyksenJaksot = pidennyksetSlave))

        reloadRaportointikanta

        val queryResult = queryOids(List(master.oid, slave1.oid))
        queryResult should contain(Oppivelvollisuustieto(
          master.oid,
          oppivelvollisuusVoimassaAsti = date(2021, 12, 31),
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2025, 1, 21)
        ))
        queryResult should contain(Oppivelvollisuustieto(
          slave1.oid,
          oppivelvollisuusVoimassaAsti = date(2021, 12, 31),
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2025, 1, 21)
        ))
      }
    }

    "Oppivelvollisuuden ja maksuttomuuden päättely kuntahistorian perusteella" - {
      lazy val resetKoskiFixtures = resetFixtures()

      def isOppivelvollinen(oid: String): Boolean =
        Oppivelvollisuustiedot.queryByOids(Seq(oid), KoskiApplicationForTests.raportointiDatabase).nonEmpty

      "Suomeen täysi-ikäisenä muuttanut ei ole oppivelvollisuden alainen" in {
        resetKoskiFixtures
        isOppivelvollinen(KoskiSpecificMockOppijat.suomeenTäysiikäisenäMuuttanut.oid) should be(false)
      }

      "Suomeen alaikäisenä muuttanut on oppivelvollisuden alainen" in {
        resetKoskiFixtures
        isOppivelvollinen(KoskiSpecificMockOppijat.suomeenAlaikäisenäMuuttanut.oid) should be(true)
        queryOids(KoskiSpecificMockOppijat.suomeenAlaikäisenäMuuttanut.oid).head.oppivelvollisuusVoimassaAsti shouldBe(date(2023,12,31))
        queryOids(KoskiSpecificMockOppijat.suomeenAlaikäisenäMuuttanut.oid).head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe(date(2026,12,31))
      }

      "Suomesta alaikäisenä pois muuttanut ja täysi-ikäisenä takaisin muuttanut on oppivelvollisuden alainen" in {
        resetKoskiFixtures
        isOppivelvollinen(KoskiSpecificMockOppijat.ulkomaillaHetkenAsunut.oid) should be(true)
        // Nykyinen kotikunta Suomessa -> oppivelvollisuuden päättyminen iän mukaan
        queryOids(KoskiSpecificMockOppijat.ulkomaillaHetkenAsunut.oid).head.oppivelvollisuusVoimassaAsti shouldBe(date(2023,12,31))
        // Suomesta alaikäisenä muuttanut ja täysi-ikäisenä palannut -> oikeus maksuttomuuteen päättynyt Suomesta muuton päivänä
        queryOids(KoskiSpecificMockOppijat.ulkomaillaHetkenAsunut.oid).head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe(date(2022, 3, 1))
      }

      "Ahvenanmaalta täysi-ikäisenä Manner-Suomeen muuttanut ei ole oppivelvollisuden alainen" in {
        resetKoskiFixtures
        isOppivelvollinen(KoskiSpecificMockOppijat.suomeenAhvenanmaaltaTäysiikäisenäMuuttanut.oid) should be(false)
      }

      "Turvakiellolliset kotikuntahistoriarivit eivät päädy raportointikannan tauluun" in {
        def getKotikuntahistoriaCount(db: RaportointiDatabase, oid: String): Int =
          QueryMethods.runDbSync(
            db.db,
            sql"SELECT COUNT(*) FROM #${db.schema.name}.r_kotikuntahistoria WHERE master_oid = $oid".as[Int],
          ).head
        resetKoskiFixtures
        val oid = "1.2.246.562.24.00000000071"
        getKotikuntahistoriaCount(KoskiApplicationForTests.raportointiDatabase, oid) should equal(1)
        getKotikuntahistoriaCount(KoskiApplicationForTests.raportointiDatabase.confidential.get, oid) should equal(2)
      }
    }

    "Oppivelvollisuuden ja maksuttomuuden päättely kuntahistorian perusteella (Valpas fixturella)" - {
      lazy val resetValpasMockData = FixtureUtil.resetMockData(defaultKoskiApplication)

      "Ulkomaille muutto päättää oppivelvollisuuden ja oikeuden maksuttomuuteen" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.muuttanutUlkomaille.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 1, 1)
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2023, 1, 1)
      }
      "Muuttanut ulkomaille ennen 7v, mutta opiskeluoikeus on (teoreettisesti) olemassa -> oppivelvollisuus päättyy samana päivänä kuin alkaa, lisäksi oikeus maksuttomuuteen päättyy ulkomaille muuttoon" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.muuttanutUlkomailleEnnen7vIkää.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2012, 9, 1)
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2010, 10, 1)
      }
      "Muuttanut ulkomaille alle 18-vuotiaana ja palannut alle 18-vuotiaana takaisin Suomeen" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.ulkomailleAlle18vuotiaanaMuuttanutJaAlle18vuotiaanaPalannut.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 10, 20)
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2025, 12, 31)
      }
      "Muuttanut ulkomaille alle 18-vuotiaana ja palannut yli 18-vuotiaana takaisin Suomeen" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.ulkomailleAlle18vuotiaanaMuuttanutJaYli18vuotiaanaPalannut.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 1, 3)
        // Ulkomaille muuttamisen päivämäärä, kun palannut yli 18-vuotiaana
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2015, 10, 2)
      }
      "Muuttanut ulkomaille yli 18-vuotiaana ja palannut yli 18-vuotiaana takaisin Suomeen" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.ulkomailleYli18vuotiaanaMuuttanutJaYli18vuotiaanaPalannut.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 4, 4)
        // Oikeus maksuttomuuteen jatkuu normaalisti
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2025, 12, 31)
      }
      "Muuttanut ulkomaille alle 18-vuotiaana ja palannut yli 20-vuotiaana takaisin Suomeen (sinä vuonna kun täyttää 21)" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.ulkomailleAlle18vuotiaanaMuuttanutJaYli20vuotiaanaPalannut.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 5, 7)
        // Ulkomaille muuttamisen päivämäärä, kun palannut yli 18-vuotiaana
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2015, 4, 21)
      }
      "Muuttanut ulkomaille yli 18-vuotiaana ja palannut yli 20-vuotiaana takaisin Suomeen (sinä vuonna kun täyttää 21)" in {
        resetValpasMockData
        val result = queryOids(ValpasMockOppijat.ulkomailleYli18vuotiaanaMuuttanutJaYli20vuotiaanaPalannut.oid)
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 4, 20)
        // Ulkomaille muuttamisen päivämäärä
        result.head.oikeusMaksuttomaanKoulutukseenVoimassaAsti shouldBe date(2023, 4, 21)
      }
    }
  }

  private def ammatillinenTutkinto(vahvistus: Option[LocalDate], alkamispäivä: LocalDate = longTimeAgo, lisääMaksuttomuus: Boolean = true, keskiarvo: Option[Double] = None): AmmatillinenOpiskeluoikeus = {
    var oo = makeOpiskeluoikeus(alkamispäivä = alkamispäivä)
    oo = oo.copy(
      suoritukset = List(
        autoalanPerustutkinnonSuoritus().copy(
          alkamispäivä = Some(alkamispäivä.plusDays(1)),
          vahvistus = vahvistus.flatMap(date => ExampleData.vahvistus(date, stadinAmmattiopisto, Some(helsinki))),
          keskiarvo = keskiarvo,
          osasuoritukset = Some(AmmatillinenOldExamples.tutkinnonOsat.map(s => s.copy(
            vahvistus = vahvistusValinnaisellaTittelillä(vahvistus.getOrElse(date(2013, 1, 31)), stadinAmmattiopisto),
            arviointi = s.arviointi.map(_.map(_.copy(päivä = vahvistus.getOrElse(date(2013, 1, 31))))))))
        )),
      lisätiedot = if (lisääMaksuttomuus) {
        Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          hojks = None,
          maksuttomuus = maksuttomuustietoAlkamispäivästä(oo.alkamispäivä)
        ))
      } else {
        None
      },
    )
    vahvistus match {
      case Some(v) => lisääTila(oo, v, opiskeluoikeusValmistunut)
      case None => oo
    }
  }

  private def insertEuropeanSchoolOfHelsinkiToinenAsteEB(
    oppija: Henkilö,
    vahvistusEB: Option[LocalDate],
    alkamispäivä: LocalDate = date(2004, 8, 15),
    päättymispäivä: Option[LocalDate],
    lisääMaksuttomuus: Boolean = true
  ) = {
    europeanSchoolOfHelsinkiToinenAsteEB(vahvistusEB, alkamispäivä, päättymispäivä, lisääMaksuttomuus).map(
      oo => insert(oppija, oo)
    )
  }

  private def europeanSchoolOfHelsinkiToinenAsteEB(
    vahvistusEB: Option[LocalDate],
    alkamispäivä: LocalDate = date(2004, 8, 15),
    päättymispäivä: Option[LocalDate],
    lisääMaksuttomuus: Boolean = true
  ): Seq[Opiskeluoikeus] = {
    val eshOpiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
      lisätiedot = if (lisääMaksuttomuus) {
        Some(EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(
          maksuttomuus = maksuttomuustietoAlkamispäivästä(Some(alkamispäivä))
        ))
      } else {
        None
      },
      tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
        List(
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
        ) ++ päättymispäivä.map(pv => EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(pv, LukioExampleData.opiskeluoikeusPäättynyt))
      ),
      suoritukset = List(
        ExamplesEuropeanSchoolOfHelsinki.n1.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.n2.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p1.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p2.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p3.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p4.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p5.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s1.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s2.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s3.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s4.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s5.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s6.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s7.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus), alkamispäivä = Some(alkamispäivä))
      )
    )
    val ebOpiskeluoikeus = ExamplesEB.opiskeluoikeus.copy(
      tila = EBOpiskeluoikeudenTila(
        List(
          EBOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen)
        ) ++ päättymispäivä.map(pv => EBOpiskeluoikeusjakso(pv, LukioExampleData.opiskeluoikeusPäättynyt))
      ),
      suoritukset = List(
        ExamplesEB.eb.copy(vahvistus = vahvistusEB.flatMap(suoritusVahvistus))
      )
    )

    Seq(eshOpiskeluoikeus, ebOpiskeluoikeus)
  }


  private def internationalSchoolToinenAste(
    alkamispäivä: LocalDate = date(2004, 8, 15),
    päättymispäivä: Option[LocalDate],
    lisääMaksuttomuus: Boolean = true
  ): Opiskeluoikeus = {
    ExamplesInternationalSchool.opiskeluoikeus.copy(
      lisätiedot = if (lisääMaksuttomuus) {
        Some(InternationalSchoolOpiskeluoikeudenLisätiedot(
          maksuttomuus = maksuttomuustietoAlkamispäivästä(Some(alkamispäivä))
        ))
      } else {
        None
      },
      tila = InternationalSchoolOpiskeluoikeudenTila(
        List(
          InternationalSchoolOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
        ) ++ päättymispäivä.map(päättymisPäivä => InternationalSchoolOpiskeluoikeusjakso(päättymisPäivä, LukioExampleData.opiskeluoikeusPäättynyt))
      ),
      suoritukset = List(
        ExamplesInternationalSchool.gradeExplorer.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade1.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade2.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade3.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade4.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade5.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade6.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade7.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade8.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade9.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade10.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade11.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus)),
        ExamplesInternationalSchool.grade12.copy(alkamispäivä = Some(alkamispäivä), vahvistus = päättymispäivä.flatMap(InternationalSchoolExampleData.vahvistus))
      )
    )
  }

  private def ibTutkinto(
    alkamispäivä: LocalDate = date(2019, 8, 1),
    ibTutkinnonVahvistus: Option[LocalDate]
  ): IBOpiskeluoikeus = {
    ExamplesIB.aktiivinenOpiskeluoikeus.copy(
      lisätiedot = None,
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = alkamispäivä, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        ) ++ ibTutkinnonVahvistus.map(päättymispäivä => LukionOpiskeluoikeusjakso(alku = päättymispäivä, tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)))
      ),
      suoritukset = List(
        ExamplesIB.preIBSuoritus,
        ExamplesIB.ibTutkinnonSuoritus(predicted = false).copy(
          vahvistus = ibTutkinnonVahvistus.flatMap(date => ExampleData.vahvistusPaikkakunnalla(päivä = date, org = YleissivistavakoulutusExampleData.ressunLukio, kunta = helsinki))
        )
      )
    )
  }

  private def diaTutkinto(alkamispäivä: LocalDate = date(2012, 9, 1), diaTutkinnonVahvistus: Option[LocalDate]): DIAOpiskeluoikeus = {
    ExamplesDIA.opiskeluoikeus.copy(
      tila = DIAOpiskeluoikeudenTila(
        List(
          DIAOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen))
        ) ++ diaTutkinnonVahvistus.map(päättymispäivä => DIAOpiskeluoikeusjakso(päättymispäivä, LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen)))
      ),
      lisätiedot = None,
      suoritukset = List(
        ExamplesDIA.diaValmistavanVaiheenSuoritus,
        ExamplesDIA.diaTutkintovaiheenSuoritus(Some(870), Some(590), Some(280), Some(1.2F)).copy(
          vahvistus = diaTutkinnonVahvistus.flatMap(date =>
            ExampleData.vahvistusPaikkakunnalla(
              päivä = date,
              org = DIAExampleData.saksalainenKoulu,
              kunta = helsinki
            )
          )
        )
      )
    )
  }

  private def ammatillinenTutkintoMaksuttomuusJaksoilla(vahvistus: Option[LocalDate],
                                   maksuttomuusJaksot: Option[List[Maksuttomuus]] = None,
                                   maksuttomuudenPidennyksenJaksot: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None): Opiskeluoikeus = {
    val alkamispaiva = date(2021, 8, 1)

    val opiskeluoikeus = alkamispäivällä(defaultOpiskeluoikeus, alkamispaiva)

    opiskeluoikeus.copy(suoritukset = List(
      autoalanPerustutkinnonSuoritus().copy(
        alkamispäivä = Some(date(2021, 8, 1)),
        vahvistus = vahvistus.flatMap(date => ExampleData.vahvistus(date, stadinAmmattiopisto, Some(helsinki))),
        osasuoritukset = Some(AmmatillinenOldExamples.tutkinnonOsat)
      )
    ),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(hojks = None,
        maksuttomuus = maksuttomuusJaksot,
        oikeuttaMaksuttomuuteenPidennetty = maksuttomuudenPidennyksenJaksot)))
  }

  private def lukionOppimäärä(vahvistus: Option[LocalDate], lisääMaksuttomuus: Boolean = true, alkamispäivä: LocalDate = date(2021, 8, 1)): Opiskeluoikeus = {
    ExamplesLukio2019.aktiivinenOpiskeluoikeus
      .copy(
        oppilaitos = None,
        tila = vahvistus.map(vahvistusDate => LukionOpiskeluoikeudenTila(
          List(
            LukionOpiskeluoikeusjakso(alku = alkamispäivä, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
            LukionOpiskeluoikeusjakso(alku = vahvistusDate, tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
          )
        )).getOrElse(ExamplesLukio2019.aktiivinenOpiskeluoikeus.tila),
        suoritukset = List(ExamplesLukio2019.oppimääränSuoritus.copy(vahvistus = vahvistus.flatMap(vahvistusPaikkakunnalla(_)))),
        lisätiedot = if (lisääMaksuttomuus) {
            Some(LukionOpiskeluoikeudenLisätiedot(
              maksuttomuus = maksuttomuustietoAlkamispäivästä(Some(alkamispäivä)),
            ))
          } else {
            None
          }
      )
  }

  private def lukionOppimääräJaMaksuttomuus(vahvistus: Option[LocalDate],
                                            maksuttomuusJaksot: Option[List[Maksuttomuus]] = None,
                                            maksuttomuudenPidennyksenJaksot: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None): Opiskeluoikeus = {
    ExamplesLukio2019.aktiivinenOpiskeluoikeus
      .copy(
        oppilaitos = None,
        suoritukset = List(ExamplesLukio2019.oppimääränSuoritus.copy(vahvistus = vahvistus.flatMap(vahvistusPaikkakunnalla(_)))),
        lisätiedot = maksuttomuusJaksot.flatMap(_ => Some(LukionOpiskeluoikeudenLisätiedot(
          maksuttomuus = maksuttomuusJaksot,
          oikeuttaMaksuttomuuteenPidennetty = maksuttomuudenPidennyksenJaksot
        )))
      )
  }

  private def lukionAineopinnot(vahvistus: Option[LocalDate], lisääMaksuttomuus: Boolean = false): Opiskeluoikeus = {
    ExamplesLukio2019.oppiaineenOppimääräOpiskeluoikeus
      .copy(
        oppilaitos = None,
        suoritukset = List(ExamplesLukio2019.oppiaineidenOppimäärienSuoritus.copy(vahvistus = vahvistus.flatMap(vahvistusPaikkakunnalla(_)))),
        lisätiedot = if (lisääMaksuttomuus) {
          Some(LukionOpiskeluoikeudenLisätiedot(
            maksuttomuus = maksuttomuustietoAlkamispäivästä(ExamplesLukio2019.aktiivinenOpiskeluoikeus.alkamispäivä),
          ))
        } else {
          None
        }
      )
  }

  def maksuttomuustietoAlkamispäivästä(alkamispäivä: Option[LocalDate]): Option[List[Maksuttomuus]] =
    alkamispäivä.map(a => List(Maksuttomuus(alku = a, loppu = None, maksuton = true)))

  private def perusopetuksenOppimäärä(vahvistus: Option[LocalDate]): Opiskeluoikeus = {
    PerusopetusExampleData.opiskeluoikeus(
      päättymispäivä = None,
      suoritukset = List(
        yhdeksännenLuokanSuoritus,
        PerusopetusExampleData.perusopetuksenOppimääränSuoritus.copy(vahvistus = vahvistus.flatMap(ExampleData.vahvistusPaikkakunnalla(_)))
      )
    )
  }

  private def insert(oppija: Henkilö, opiskeluoikeudet: Opiskeluoikeus*) = {
    putOpiskeluoikeudet(opiskeluoikeudet.toList, oppija) {
      verifyResponseStatusOk()
    }
  }

  private def clearAndInsert(oppija: OppijaHenkilö, opiskeluoikeudet: Opiskeluoikeus*) = {
    clearOppijanOpiskeluoikeudet(oppija.oid)
    insert(oppija, opiskeluoikeudet:_*)
  }

  private def queryTestioidit = queryOids(testiOidit)

  private def queryOids(oids: String*): List[Oppivelvollisuustieto] = queryOids(oids.toList)

  private def queryOids(oids: List[String]): List[Oppivelvollisuustieto] = Oppivelvollisuustiedot.queryByOids(oids, mainRaportointiDb).toList

  private def verifyTestiOidit(oppivelvollisuus: LocalDate, maksuttomuus: LocalDate) = {
    queryTestioidit should contain theSameElementsAs(
      testiOidit.map(oid =>
        Oppivelvollisuustieto(
          oid,
          oppivelvollisuusVoimassaAsti = oppivelvollisuus,
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = maksuttomuus
        )
      )
    )
  }
}
