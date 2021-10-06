package fi.oph.koski.oppivelvollisuustieto

import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
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

  val master = oppivelvollisuustietoMaster
  val slave1 = oppivelvollisuustietoSlave1.henkilö
  val slave2 = oppivelvollisuustietoSlave2.henkilö

  val testiOidit = List(master.oid, slave1.oid, slave2.oid)

  "Oppivelvollisuustieto" - {

    "Jos oppija ei ole oppivelvollisuuden piirissä, ei hänelle synny riviä oppivelvollisuustiedot-tauluun" - {
      "Slave oidilla perusopetuksen oppimäärä suoritettu ennen uuden oppivelvollisuuslain voimaantuloa -> ei rivejä" in {
        resetFixtures
        insert(master, lukionOppimäärä(vahvistus = None))
        insert(slave1, ammatillinenTutkinto(vahvistus = None, lisääMaksuttomuus = false), perusopetuksenOppimäärä(Some(date(2020, 12, 31))))
        insert(slave2, ammatillinenTutkinto(vahvistus = None, lisääMaksuttomuus = false), lukionOppimäärä(vahvistus = None, lisääMaksuttomuus = false))
        reloadRaportointikanta
        queryTestioidit should equal(Nil)
      }
      "Master oidilla perusopetuksen oppimäärä suoritettu ennen uuden oppivelvollisuuslain voimaantuloa -> ei rivejä" in {
        resetFixtures
        insert(master, perusopetuksenOppimäärä(Some(date(2020, 12, 31))))
        insert(slave1, ammatillinenTutkinto(vahvistus = None, lisääMaksuttomuus = false))
        insert(slave2, ammatillinenTutkinto(vahvistus = None, lisääMaksuttomuus = false), lukionOppimäärä(vahvistus = None, lisääMaksuttomuus = false))
        reloadRaportointikanta
        queryTestioidit should equal(Nil)
      }
      "Henkilö on liian vanha" in {
        resetFixtures
        insert(oppivelvollisuustietoLiianVanha, lukionOppimäärä(vahvistus = None, lisääMaksuttomuus = false))
        reloadRaportointikanta
        queryOids(oppivelvollisuustietoLiianVanha.oid) shouldBe(Nil)
      }
      "Henkilö on suorittanut aikuisten perusopetuksen oppimäärän ennen vuotta 2021" in {
        resetFixtures
        insert(oikeusOpiskelunMaksuttomuuteen, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen)
        reloadRaportointikanta
        queryOids(oikeusOpiskelunMaksuttomuuteen.oid) shouldBe(Nil)
      }
      "Henkilö on suorittanut international schoolin ysiluokan ennen vuotta 2021" in {
        resetFixtures
        insert(oikeusOpiskelunMaksuttomuuteen, ExamplesInternationalSchool.opiskeluoikeus)
        reloadRaportointikanta
        queryOids(oikeusOpiskelunMaksuttomuuteen.oid) shouldBe(Nil)
      }
    }

    "Jos oppija on oppivelvollisuuden piirissä, löytyy samat tiedot hänen kaikilla oideilla" - {
      "Jos suorittaa ammatillista tutkintoa ja lukion oppimäärää, käytetään aina syntymäaikaa päättymispäivien päättelyssä" - {
        "Ammatillisella tutkinnolla vahvistus" in {
          resetFixtures
          insert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2020, 1, 1))))
          insert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2022, 1, 1), maksuttomuus = date(2024, 12, 31))
        }
        "Lukion oppimaaralla vahvistus" in {
          resetFixtures
          insert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = None))
          insert(slave2, lukionOppimäärä(vahvistus = Some(date(2019, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2022, 1, 1), maksuttomuus = date(2024, 12, 31))
        }
        "Molemmilla vahvistus" in {
          resetFixtures
          insert(master, perusopetuksenOppimäärä(Some(date(2021, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2020, 1, 1))))
          insert(slave2, lukionOppimäärä(vahvistus = Some(date(2019, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2022, 1, 1), maksuttomuus = date(2024, 12, 31))
        }
      }
      "Jos suorittaa vain lukion oppimäärää, käytetään aina syntymäaikaa päättymispäivien päättelyssä" - {
        "Lukion oppimaaralla vahvistus" in {
          resetFixtures
          insert(master, lukionOppimäärä(vahvistus = Some(date(2018, 1, 1))))
          insert(slave1, lukionOppimäärä(vahvistus = Some(date(2019, 1, 1))))
          insert(slave2, lukionOppimäärä(vahvistus = Some(date(2020, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2022, 1, 1), maksuttomuus = date(2024, 12, 31))
        }
      }

      "Jos suorittaa vain ammatillista tutkintoa, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
        "Syntymäaika päättää oppivelvollisuuden aikaisemmin, ensimmäisenä vahvistetun tutkinnon vahvistus päättää oikeuden maksuttomuuteen aikaisemmin" in {
          resetFixtures
          insert(master, ammatillinenTutkinto(vahvistus = Some(date(2030, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2023, 1, 1))))
          insert(slave2, ammatillinenTutkinto(vahvistus = Some(date(2024, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2022, 1, 1), maksuttomuus = date(2023, 1, 1))
        }
        "Syntymäaika päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, ammatillinenTutkinto(vahvistus = None))
          insert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2025, 1, 1))))
          insert(slave2, ammatillinenTutkinto(vahvistus = Some(date(2024, 12, 31))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2022, 1, 1), maksuttomuus = date(2024, 12, 31))
        }
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, ammatillinenTutkinto(vahvistus = Some(date(2021, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = Some(date(2025, 1, 1))))
          insert(slave2, ammatillinenTutkinto(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa vain international schoolia, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          insert(slave1, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2025, 1, 1))))
          insert(slave2, internationalSchoolToinenAste(vahvistusGrade12 = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa international schoolia ja lukiota, käytetään päättymispäivän international schoolin vahvistuspäivää" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          insert(slave1, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2025, 1, 1))))
          insert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa international schoolia, lukiota ja ammattikoulua, käytetään päättymispäivän international schoolin vahvistuspäivää" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = None))
          insert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos on suorittanut IB-tutkinnon ja international schoolin, käytetään päättymispäivänä aiempaa vahvistuspäivää" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 3, 3))))
          insert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))))
          insert(slave2, internationalSchoolToinenAste(vahvistusGrade12 = Some(date(2021, 1, 1))))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa vain IB-tutkintoa, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))))
          insert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))))
          insert(slave2, ibTutkinto(ibTutkinnonVahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa IB-tutkintoa ja lukiota, käytetään päättymispäivän IB-tutkinnon vahvistuspäivää" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))))
          insert(slave1, ibTutkinto(ibTutkinnonVahvistus = Some(date(2025, 1, 1))))
          insert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos suorittaa IB-tutkintoa, lukiota ja ammattikoulua, käytetään päättymispäivän IB-tutkinnon vahvistuspäivää" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, ibTutkinto(ibTutkinnonVahvistus = Some(date(2021, 1, 1))))
          insert(slave1, ammatillinenTutkinto(vahvistus = None))
          insert(slave2, lukionOppimäärä(vahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Jos on suorittanut DIA-tutkinnon, käytetään päättymispäivänä päivää, joka lopettaa aikaisemmin oikeuden maksuttomuuteen tai oppivelvollisuuteen" - {
        "Vahvistuspäivä päättää molemmat aikaisemmin" in {
          resetFixtures
          insert(master, diaTutkinto(diaTutkinnonVahvistus = Some(date(2021, 1, 1))))
          insert(slave1, diaTutkinto(diaTutkinnonVahvistus = Some(date(2025, 1, 1))))
          insert(slave2, diaTutkinto(diaTutkinnonVahvistus = None))
          reloadRaportointikanta
          verifyTestiOidit(oppivelvollisuus = date(2021, 1, 1), maksuttomuus = date(2021, 1, 1))
        }
      }

      "Ei ole vielä suorittamassa toisen asteen tutkintoa" - {
        "Käytetään syntymäaikaa" in {
          resetFixtures
          insert(oikeusOpiskelunMaksuttomuuteen, perusopetuksenOppimäärä(vahvistus = Some(date(2021, 6, 1))))
          reloadRaportointikanta
          queryOids(oikeusOpiskelunMaksuttomuuteen.oid) should equal(List(
            Oppivelvollisuustieto(
              oikeusOpiskelunMaksuttomuuteen.oid,
              oppivelvollisuusVoimassaAsti = date(2022, 12, 31),
              oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2024, 12, 31)
            )
          ))
        }
      }

      "Jos opiskeluoikeuksissa on pidennetty oikeutta maksuttomuuteen" in {
        resetFixtures

        val alkamispaiva = date(2021, 8, 1)

        val maksuttomuusJaksot = Some(List(
          Maksuttomuus(alkamispaiva, None, maksuton = true),
        ))

        val pidennyksetMaster = Some(List(
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva, alkamispaiva.plusDays(20)),
        ))
        val pidennyksetSlave = Some(List(
          OikeuttaMaksuttomuuteenPidennetty(alkamispaiva.plusDays(25), alkamispaiva.plusDays(30)),
        ))

        insert(master, ammatillinenTutkintoMaksuttomuusJaksoilla(vahvistus = None,
          maksuttomuusJaksot = maksuttomuusJaksot,
          maksuttomuudenPidennyksenJaksot = pidennyksetMaster))
        insert(slave1, ammatillinenTutkintoMaksuttomuusJaksoilla(vahvistus = None,
          maksuttomuusJaksot = maksuttomuusJaksot,
          maksuttomuudenPidennyksenJaksot = pidennyksetSlave))
        reloadRaportointikanta

        val queryResult = queryOids(List(master.oid, slave1.oid))
        queryResult should contain(Oppivelvollisuustieto(
          master.oid,
          oppivelvollisuusVoimassaAsti = date(2022, 1, 1),
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2025, 1, 27)
        ))
        queryResult should contain(Oppivelvollisuustieto(
          slave1.oid,
          oppivelvollisuusVoimassaAsti = date(2022, 1, 1),
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2025, 1, 27)
        ))
      }
    }
  }

  private def ammatillinenTutkinto(vahvistus: Option[LocalDate], lisääMaksuttomuus: Boolean = true): Opiskeluoikeus = {
    defaultOpiskeluoikeus.copy(
      suoritukset = List(
        autoalanPerustutkinnonSuoritus().copy(
          vahvistus = vahvistus.flatMap(date => ExampleData.vahvistus(date, stadinAmmattiopisto, Some(helsinki))),
          osasuoritukset = Some(AmmatillinenOldExamples.tutkinnonOsat),
        )
      ),
      lisätiedot = if (lisääMaksuttomuus) {
        Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          hojks = None,
          maksuttomuus = maksuttomuustietoAlkamispäivästä(defaultOpiskeluoikeus.alkamispäivä)
        ))
      } else {
        None
      },
    )
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

  private def ibTutkinto(ibTutkinnonVahvistus: Option[LocalDate]): Opiskeluoikeus = {
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

  private def diaTutkinto(diaTutkinnonVahvistus: Option[LocalDate]): Opiskeluoikeus = {
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

  def maksuttomuustietoAlkamispäivästä(alkamispäivä: Option[LocalDate]): Option[List[Maksuttomuus]] =
    alkamispäivä.map(a => List(Maksuttomuus(alku = a, loppu = None, maksuton = true)))

  private def perusopetuksenOppimäärä(vahvistus: Option[LocalDate]): Opiskeluoikeus = {
    PerusopetusExampleData.opiskeluoikeus(
      päättymispäivä = None,
      suoritukset = List(PerusopetusExampleData.perusopetuksenOppimääränSuoritus.copy(vahvistus = vahvistus.flatMap(ExampleData.vahvistusPaikkakunnalla(_))))
    )
  }

  private def insert(oppija: Henkilö, opiskeluoikeudet: Opiskeluoikeus*) = {
    putOpiskeluoikeudet(opiskeluoikeudet.toList, oppija) {
      verifyResponseStatusOk()
    }
  }

  private def maksuttomuudenPidennyksenJakso = {
    OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2020, 10, 10),
      LocalDate.of(2020, 10, 15))
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
