package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class AmmatillinenOsittainenRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsAmmatillinen with BeforeAndAfterAll {

  override def beforeAll(): Unit = loadRaportointikantaFixtures

  lazy val repository = AmmatillisenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  lazy val defaultTestiHenkilö = MockOppijat.ammatillisenOsittainenRapsa

  "Ammatillisen tutkinnon osa/osia -raporti" - {
    "Raportin voi ladata" in {
      verifyRaportinLataaminen(
        apiUrl = "api/raportit/ammatillinenosittainensuoritustietojentarkistus",
        expectedRaporttiNimi = s"ammatillinenosittainensuoritustietojentarkistus",
        expectedFileNamePrefix = "Ammatillinen_tutkinnon_osa_ja_osia"
      )
    }
    "Raportin tiedot" in {
      val rows = testiHenkilöRaporttiRows(alku = date(2016, 1, 1), loppu = date(2016, 5, 5), osasuoritustenAikarajaus = false)
      rows should equal(List(defaultExpectedRow))
    }
    "Tutkinnon osia voidaan rajata arviointipäivän perusteella" - {
      "Tutkinnon osat jotka arvioitu ennen aikaväliä, ei oteta mukaan raportille" in {
        val rows = testiHenkilöRaporttiRows(alku = date(2015, 1, 1), loppu = date(2015, 2, 2), osasuoritustenAikarajaus = true)
        rows.map(_.suoritettujenOpintojenYhteislaajuus) should equal(List(20))
      }
      "Tutkinnon osiat jotka arvioitu jälkeen aikavälin, ei oteta mukaan raportille" in {
        val rows = testiHenkilöRaporttiRows(alku = date(2014, 1, 1), loppu = date(2014, 12, 31), osasuoritustenAikarajaus = true)
        rows.map(_.suoritettujenOpintojenYhteislaajuus) should equal(List(91))
      }
    }

    "Ei näytetä riviä näyttötutkintoon valmistavalle koulutukselle, jos sen parina oleva pääsuoritus ei ole osittainen" in {
      val rivit = testiHenkilöRaporttiRows(alku = date(2016, 1, 1), loppu = date(2016, 5, 30), osasuoritustenAikarajaus = false, hetu = MockOppijat.erikoisammattitutkinto.hetu.get)

      rivit.length should equal(0)
    }
  }

  val defaultExpectedRow = AmmatillinenOsittainRaporttiRow(
    opiskeluoikeusOid = lastOpiskeluoikeus(defaultTestiHenkilö.oid).oid.get,
    lähdejärjestelmä = None,
    lähdejärjestelmänId = None,
    sisältyyOpiskeluoikeuteenOid = "",
    ostettu = false,
    sisältyvätOpiskeluoikeudetOidit = "",
    sisältyvätOpiskeluoikeudetOppilaitokset = "",
    linkitetynOpiskeluoikeudenOppilaitos = "",
    aikaleima = LocalDate.now,
    toimipisteOid = MockOrganisaatiot.lehtikuusentienToimipiste,
    yksiloity = true,
    oppijaOid = defaultTestiHenkilö.oid,
    hetu = defaultTestiHenkilö.hetu,
    sukunimi = defaultTestiHenkilö.sukunimi,
    etunimet = defaultTestiHenkilö.etunimet,
    tutkinto = "361902",
    osaamisalat = Some("1525"),
    tutkintonimike = "Autokorinkorjaaja",
    päätasonSuorituksenNimi = "Luonto- ja ympäristöalan perustutkinto",
    päätasonSuorituksenSuoritustapa = "Reformin mukainen näyttö",
    päätasonSuorituksenTila = Some("Valmis"),
    opiskeluoikeudenAlkamispäivä = Some(date(2012, 9, 1)),
    viimeisinOpiskeluoikeudenTila = Some("valmistunut"),
    viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = "lasna",
    opintojenRahoitukset = "1",
    suoritettujenOpintojenYhteislaajuus = 111.0,
    valmiitAmmatillisetTutkinnonOsatLkm = 3,
    näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = 1,
    tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = 2,
    rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = 1,
    suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = 89.0,
    valmiitYhteistenTutkinnonOsatLkm = 2,
    pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = 8,
    valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = 1,
    tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = 1,
    rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = 0,
    tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm = 1,
    rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm = 1,
    suoritettujenYhteistenTutkinnonOsienYhteislaajuus = 23.0,
    suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = 22.0,
    pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = 20.0,
    valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = 3.0,
    valmiitVapaaValintaisetTutkinnonOsatLkm = 0,
    valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm = 0
  )

  private def testiHenkilöRaporttiRows(alku: LocalDate, loppu: LocalDate, osasuoritustenAikarajaus: Boolean, hetu:String = defaultTestiHenkilö.hetu.get) = {
    val request = AikajaksoRaporttiAikarajauksellaRequest(MockOrganisaatiot.stadinAmmattiopisto, None, "", alku, loppu, osasuoritustenAikarajaus)
    AmmatillinenOsittainenRaportti.buildRaportti(request, repository).filter(_.hetu.contains(hetu)).toList
  }
}
