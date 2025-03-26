package fi.oph.koski.oppivelvollisuustieto

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData.yhdeksännenLuokanSuoritus
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.raportointikanta.{RaportointiDatabase, RaportointikantaTestMethods}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.schema._
import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil
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
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = Some(date(2019, 10, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
        "Molemmilla vahvistus (mutta ei YO-todistusta) -> ammatillisen mukaan" in {
          clearAndInsert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2019, 7, 1)), keskiarvo = Some(4.0)))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = Some(date(2019, 10, 1)))) // alku 2019-8-1 ??
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
          clearAndInsert(master, lukionOppimäärä(vahvistus = Some(date(2018, 1, 1))))
          clearAndInsert(slave1, lukionOppimäärä(vahvistus = Some(date(2019, 1, 1))))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = Some(date(2020, 1, 1))))
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
            clearAndInsert(pelkkäYoKannassaUudenOvLainPiirissä, lukionOppimäärä(vahvistus = Some(date(2018, 1, 1))))
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
              vahvistusEB = None
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
              vahvistusEB = Some(date(2021, 1, 1))
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
              lukionOppimäärä(vahvistus = Some(date(2021, 9, 5))))// alku: 2019-8-1 yo: 2021-09-05
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
          clearAndInsert(master, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          clearAndInsert(slave1, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2025, 1, 1))))
          clearAndInsert(slave2, internationalSchoolToinenAste(vahvistusGrade12 = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa international schoolia ja lukiota, käytetään päättymispäivänä ikään perustuvia päiviä, koska kumpaakaan ei katsota ov päättäväksi tutkinnoksi" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          clearAndInsert(slave1, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2025, 1, 1))))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa international schoolia, lukiota ja ammattikoulua, käytetään päättymispäivänä ikään perustuvia vahvistuspäiviä, koska mitään ov-päättävää tutkintoa ei katsota olevan" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          clearAndInsert(slave1, ammatillinenTutkinto(vahvistus = None))
          clearAndInsert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos on suorittanut IB-tutkinnon ja international schoolin, käytetään päättymispäivänä ikään perustuvaa vahvistuspäivää, koska kumpikaan ei ole ov-päättävä tutkinto" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 3, 3))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "European School of Helsinki" - {

        "Jos suorittaa vain European school of Helsinkiä, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(master, vahvistusEB = Some(date(2021, 1, 1)))
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave1, vahvistusEB = Some(date(2025, 1, 1)))
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave2, vahvistusEB = None)
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
          }
        }

        "Jos suorittaa European school of Helsinkiä ja lukiota, käytetään päättymispäivänä EB-tutkinnon vahvistuspäivää" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(master, vahvistusEB = Some(date(2021, 1, 1)))
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave1, vahvistusEB = Some(date(2025, 1, 1)))
            insert(slave2, lukionOppimäärä(vahvistus = None))
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
          }
        }

        "Jos suorittaa European school of Helsinkiä, lukiota ja ammattikoulua, käytetään päättymispäivänä EB-tutkinnon vahvistuspäivää" - {
          "Vahvistuspäivä päättää molemmat aikaisemmin" in {
            resetFixtures
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(master, vahvistusEB = Some(date(2021, 1, 1)))
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
            insert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
            insertEuropeanSchoolOfHelsinkiToinenAsteEB(slave2, vahvistusEB = Some(date(2021, 3, 3)))
            reloadRaportointikanta
            verifyTestiOidit(oppivelvollisuus = date(2021, 3, 3), maksuttomuus = date(2021, 3, 3))
          }
        }
      }

      "Jos suorittaa vain IB-tutkintoa, käytetään päättymispäivänä ikään perustuvaa päivää, koska Koskeen vahvistettu ib-suoritus ei päätä oppivelvollisuutta" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, ibTutkinto(ibTutkinnonVahvistus = None).withLisääPuuttuvaMaksuttomuustieto)
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 12, 31), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa IB-tutkintoa ja lukiota, käytetään päättymispäivänä ikään perustuvaa päivää, koska kumpikaan merkintä ei ole ov:n päättävä tutkinto" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          clearAndInsert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
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
          clearAndInsert(slave1, diaTutkinto(diaTutkinnonVahvistus = Some(date(2025, 1, 1))).withLisääPuuttuvaMaksuttomuustieto)
          clearAndInsert(slave2, diaTutkinto(diaTutkinnonVahvistus = None).withLisääPuuttuvaMaksuttomuustieto)
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
      }

      "Suomesta alaikäisenä pois muuttanut ja täysi-ikäisenä takaisin muuttanut on oppivelvollisuden alainen" in {
        resetKoskiFixtures
        isOppivelvollinen("1.2.246.562.24.00000000168") should be(true)
        // Nykyinen kotikunta Suomessa -> oppivelvollisuuden päättyminen iän mukaan
        queryOids("1.2.246.562.24.00000000168").head.oppivelvollisuusVoimassaAsti shouldBe(date(2023,12,31))
      }

      "Ahvenanmaalta täysi-ikäisenä Manner-Suomeen muuttanut ei ole oppivelvollisuden alainen" in {
        resetKoskiFixtures
        isOppivelvollinen("1.2.246.562.24.00000000169") should be(false)
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

      "Ulkoimaille muutto päättää oppivelvollisuuden" in {
        resetValpasMockData
        val result = queryOids("1.2.246.562.24.00000000088")
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2023, 1, 1)
      }
      "Muuttanut ulkomaille ennen 7v, mutta opiskeluoikeus on (teoreettisesti) olemassa -> oppivelvollisuus päättyy samana päivänä kuin alkaa" in {
        resetValpasMockData
        val result = queryOids("1.2.246.562.24.00000000184")
        result.head.oppivelvollisuusVoimassaAsti shouldBe date(2012, 9, 1)
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

  private def insertEuropeanSchoolOfHelsinkiToinenAsteEB(oppija: Henkilö, vahvistusEB: Option[LocalDate], lisääMaksuttomuus: Boolean = true) = {
    europeanSchoolOfHelsinkiToinenAsteEB(vahvistusEB, lisääMaksuttomuus).map(
      oo => insert(oppija, oo)
    )
  }

  private def europeanSchoolOfHelsinkiToinenAsteEB(vahvistusEB: Option[LocalDate], lisääMaksuttomuus: Boolean = true): Seq[Opiskeluoikeus] = {
    val alkamispäivä = date(2004, 8, 15)
    val eshOpiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
      lisätiedot = if (lisääMaksuttomuus) {
        Some(EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(
          maksuttomuus = maksuttomuustietoAlkamispäivästä(ExamplesInternationalSchool.opiskeluoikeus.alkamispäivä)
        ))
      } else {
        None
      },
      tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
        List(
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
        )
      ),
      suoritukset = List(
        ExamplesEuropeanSchoolOfHelsinki.n1.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.n2.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p1.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p2.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p3.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p4.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.p5.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s1.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s2.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s3.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s4.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s5.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s6.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä)),
        ExamplesEuropeanSchoolOfHelsinki.s7.copy(vahvistus = None, alkamispäivä = Some(alkamispäivä))
      )
    )
    val ebOpiskeluoikeus = ExamplesEB.opiskeluoikeus.copy(
      tila = EBOpiskeluoikeudenTila(
        List(EBOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen))
      ),
      suoritukset = List(
        ExamplesEB.eb.copy(vahvistus = vahvistusEB.flatMap(InternationalSchoolExampleData.vahvistus))
      )
    )

    Seq(eshOpiskeluoikeus, ebOpiskeluoikeus)
  }


  private def internationalSchoolToinenAste(vahvistusGrade12: Option[LocalDate], lisääMaksuttomuus: Boolean = true): Opiskeluoikeus = {
    ExamplesInternationalSchool.opiskeluoikeus.copy(
      lisätiedot = if (lisääMaksuttomuus) {
        Some(InternationalSchoolOpiskeluoikeudenLisätiedot(
          maksuttomuus = maksuttomuustietoAlkamispäivästä(ExamplesInternationalSchool.opiskeluoikeus.alkamispäivä)
        ))
      } else {
        None
      },
      tila = InternationalSchoolOpiskeluoikeudenTila(
        List(
          InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen),
        )
      ),
      suoritukset = List(
        ExamplesInternationalSchool.gradeExplorer.copy(vahvistus = None),
        ExamplesInternationalSchool.grade1.copy(vahvistus = None),
        ExamplesInternationalSchool.grade2.copy(vahvistus = None),
        ExamplesInternationalSchool.grade3.copy(vahvistus = None),
        ExamplesInternationalSchool.grade4.copy(vahvistus = None),
        ExamplesInternationalSchool.grade5.copy(vahvistus = None),
        ExamplesInternationalSchool.grade6.copy(vahvistus = None),
        ExamplesInternationalSchool.grade7.copy(vahvistus = None),
        ExamplesInternationalSchool.grade8.copy(vahvistus = None),
        ExamplesInternationalSchool.grade9.copy(vahvistus = None),
        ExamplesInternationalSchool.grade10.copy(vahvistus = None),
        ExamplesInternationalSchool.grade11.copy(vahvistus = None),
        ExamplesInternationalSchool.grade12.copy(
          vahvistus = vahvistusGrade12.flatMap(InternationalSchoolExampleData.vahvistus)
        )
      )
    )
  }

  private def ibTutkinto(ibTutkinnonVahvistus: Option[LocalDate]): IBOpiskeluoikeus = {
    ExamplesIB.aktiivinenOpiskeluoikeus.copy(
      lisätiedot = None,
      suoritukset = List(
        ExamplesIB.preIBSuoritus,
        ExamplesIB.ibTutkinnonSuoritus(predicted = false).copy(
          vahvistus = ibTutkinnonVahvistus.flatMap(date => ExampleData.vahvistusPaikkakunnalla(päivä=date, org = YleissivistavakoulutusExampleData.ressunLukio, kunta = helsinki))
        )
      )
    )
  }

  private def diaTutkinto(diaTutkinnonVahvistus: Option[LocalDate]): DIAOpiskeluoikeus = {
    ExamplesDIA.opiskeluoikeus.copy(
      tila = DIAOpiskeluoikeudenTila(
        List(
          DIAOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen))
        )
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

  private def lukionOppimäärä(vahvistus: Option[LocalDate], lisääMaksuttomuus: Boolean = true): Opiskeluoikeus = {
    ExamplesLukio2019.aktiivinenOpiskeluoikeus
      .copy(
        oppilaitos = None,
        suoritukset = List(ExamplesLukio2019.oppimääränSuoritus.copy(vahvistus = vahvistus.flatMap(vahvistusPaikkakunnalla(_)))),
        lisätiedot = if (lisääMaksuttomuus) {
            Some(LukionOpiskeluoikeudenLisätiedot(
              maksuttomuus = maksuttomuustietoAlkamispäivästä(ExamplesLukio2019.aktiivinenOpiskeluoikeus.alkamispäivä),
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
