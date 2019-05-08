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

  val database = KoskiApplicationForTests.raportointiDatabase
  val defaultTestiHenkilö = MockOppijat.ammatillisenOsittainenRapsa

  "Ammatillisen tutkinnon osa/osia -raporti" - {
    "Raportin voi ladata" in {
      verifyRaportinLataaminen(
        apiUrl = "api/raportit/ammatillinenosittainensuoritustietojentarkistus",
        expectedRaporttiNimi = s"ammatillinenosittainensuoritustietojentarkistus",
        expectedFileNamePrefix = "Ammatillinen_tutkinnon_osa_ja_osia"
      )
    }
    "Raportin tiedot" in {
      val result = makeRaporttiFilterRowsByHetu(defaultTestiHenkilö.hetu)
      result.length should equal(1)
      val row = result.head
      row should equal(defaultExpectedRow)
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
    toimipisteOidit = MockOrganisaatiot.lehtikuusentienToimipiste,
    oppijaOid = defaultTestiHenkilö.oid,
    hetu = defaultTestiHenkilö.hetu,
    sukunimi = Some(defaultTestiHenkilö.sukunimi),
    etunimet = Some(defaultTestiHenkilö.etunimet),
    koulutusmoduulit = "351301",
    osaamisalat = Some("1525"),
    tutkintonimikkeet = "Autoalan perustutkinto",
    päätasonSuoritustenTilat = Some("Valmis"),
    viimeisinOpiskeluoikeudenTila = "valmistunut",
    opintojenRahoitukset = "",
    suoritettujenOpintojenYhteislaajuus = 100.0,
    valmiitAmmatillisetTutkinnonOsatLkm = 3,
    näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm = 1,
    tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm = 2,
    rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm = 1,
    suoritetutAmmatillisetTutkinnonOsatYhteislaajuus = 78.0,
    valmiitYhteistenTutkinnonOsatLkm = 2,
    pakollisetYhteistenTutkinnonOsienOsaalueidenLkm = 7,
    valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm = 1,
    tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm = 1,
    rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm = 0,
    tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm = 1,
    rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm = 1,
    suoritettujenYhteistenTutkinnonOsienYhteislaajuus = 9.0,
    suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = 22.0,
    pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus = 19.0,
    valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus = 3.0,
    valmiitVapaaValintaisetTutkinnonOsatLkm = 1,
    valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm = 1
  )

  def makeRaporttiFilterRowsByHetu(hetu: Option[String], oppilaitosOid: String = MockOrganisaatiot.stadinAmmattiopisto, alku: LocalDate = date(2016, 1, 1), loppu: LocalDate = date(2016, 12, 12)) = {
    val rows = AmmatillinenOsittainenRaportti.buildRaportti(database, oppilaitosOid, alku, loppu)
    rows.filter(_.hetu == hetu)
  }
}
