package fi.oph.koski.oppivelvollisuustieto

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus}
import org.scalatest.{FreeSpec, Matchers}

class OppivelvollisuustietoSpec
  extends FreeSpec
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
        insert(slave1, ammatillinenTutkinto(vahvistus = None), perusopetuksenOppimäärä(Some(date(2020, 12, 31))))
        insert(slave2, ammatillinenTutkinto(vahvistus = None), lukionOppimäärä(vahvistus = None))
        reloadRaportointikanta
        queryTestioidit should equal(Nil)
      }
      "Master oidilla perusopetuksen oppimäärä suoritettu ennen uuden oppivelvollisuuslain voimaantuloa -> ei rivejä" in {
        resetFixtures
        insert(master, perusopetuksenOppimäärä(Some(date(2020, 12, 31))))
        insert(slave1, ammatillinenTutkinto(vahvistus = None))
        insert(slave2, ammatillinenTutkinto(vahvistus = None), lukionOppimäärä(vahvistus = None))
        reloadRaportointikanta
        queryTestioidit should equal(Nil)
      }
      "Henkilö on liian vanha" in {
        resetFixtures
        insert(oppivelvollisuustietoLiianVanha, lukionOppimäärä(vahvistus = None))
        reloadRaportointikanta
        queryOids(oppivelvollisuustietoLiianVanha.oid) shouldBe(Nil)
      }
      "Henkilö on suorittanut aikuisten perusopetuksen oppimäärän ennen vuotta 2021" in {
        resetFixtures
        insert(oikeusOpiskelunMaksuttomuuteen, ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen)
        reloadRaportointikanta
        queryOids(oikeusOpiskelunMaksuttomuuteen.oid) shouldBe(Nil)
      }
      "Henkilö on suorittanut international schoolin ysiluokan" in {
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

      "Ei ole vielä suorittamassa toisen asteen tutkintoa" - {
        "Käytetään syntymäaikaa" in {
          resetFixtures
          insert(oikeusOpiskelunMaksuttomuuteen, perusopetuksenOppimäärä(vahvistus = Some(date(2021, 6, 1))))
          reloadRaportointikanta
          queryOids(oikeusOpiskelunMaksuttomuuteen.oid) should equal(List(
            Oppivelvollisuustieto(
              oikeusOpiskelunMaksuttomuuteen.oid,
              oppivelvollisuusVoimassaAsti = date(2022, 1, 1),
              oikeusMaksuttomaanKoulutukseenVoimassaAsti = date(2024, 12, 31)
            )
          ))
        }
      }
    }
  }

  private def ammatillinenTutkinto(vahvistus: Option[LocalDate]): Opiskeluoikeus = {
    defaultOpiskeluoikeus.copy(suoritukset = List(
      autoalanPerustutkinnonSuoritus().copy(
        vahvistus = vahvistus.flatMap(date => ExampleData.vahvistus(date, stadinAmmattiopisto, Some(helsinki))),
        osasuoritukset = Some(AmmatillinenOldExamples.tutkinnonOsat)
      )
    ))
  }

  private def lukionOppimäärä(vahvistus: Option[LocalDate]): Opiskeluoikeus = {
    ExamplesLukio2019.aktiivinenOpiskeluoikeus
      .copy(
        oppilaitos = None,
        suoritukset = List(ExamplesLukio2019.oppimääränSuoritus.copy(vahvistus = vahvistus.flatMap(vahvistusPaikkakunnalla(_))))
      )
  }

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
