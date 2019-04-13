package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class PerusopetuksenVuosiluokkaSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsPerusopetus {

  val repository = PerusopetuksenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  resetFixtures

  "Perusopetuksenvuosiluokka raportti" - {

    "Raportin lataaminen toimiii" in {
      verifyPerusopetukseVuosiluokkaRaportinLataaminen(
        queryString = defaultQuery,
        apiUrl = "api/raportit/perusopetuksenvuosiluokka",
        expectedRaporttiNimi = "perusopetuksenvuosiluokka",
        expectedFileNamePrefix = "Perusopetuksen_vuosiluokka")
    }

    "Tuottaa oikeat tiedot" in {
      withLisätiedotFixture(MockOppijat.ysiluokkalainen, perusopetuksenOpiskeluoikeudenLisätiedot) {
        val result = PerusopetuksenVuosiluokka.buildRaportti(repository, MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2014, 1, 1), vuosiluokka = "8")
        val ynjevinOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ysiluokkalainen.oid).oid.get
        val rivi = result.find(_.opiskeluoikeusOid == ynjevinOpiskeluoikeusOid)

        rivi should equal(
          Some(ynjevinExpectedKasiLuokkaRowWithLisätiedot.copy(opiskeluoikeusOid = ynjevinOpiskeluoikeusOid))
        )
      }
    }

    "Monta saman vuosiluokan suoritusta eritellään omiksi riveiksi" in {
      resetFixtures
      withAdditionalVuosiluokkaSuoritus(MockOppijat.ysiluokkalainen, kahdeksannenLuokanLuokallejääntiSuoritus) {
        val result = PerusopetuksenVuosiluokka.buildRaportti(repository, MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2014, 1, 1), vuosiluokka = "8")
        val ynjevinOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ysiluokkalainen.oid).oid.get
        val rivit = result.filter(_.opiskeluoikeusOid == ynjevinOpiskeluoikeusOid)

        rivit.length should equal(2)
        rivit should contain(defaultYnjeviExpectedKasiLuokkaRow.copy(opiskeluoikeusOid = ynjevinOpiskeluoikeusOid))
        rivit should contain(kahdeksannenLuokanLuokalleJääntiRow.copy(opiskeluoikeusOid = ynjevinOpiskeluoikeusOid))
      }
    }

    "Peruskoulun päättävät" - {

      "Hakee tiedot peruskoulun oppimäärän suorituksesta" in {
        val result = PerusopetuksenVuosiluokka.buildRaportti(repository, MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2016, 1, 1), "10")
        val kaisanOpiskeluoikeusOid = getOpiskeluoikeudet(MockOppijat.koululainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
        val kaisanRivi = result.find(_.opiskeluoikeusOid == kaisanOpiskeluoikeusOid)
        kaisanRivi shouldBe defined

        kaisanRivi.get should equal(kaisanPäättötodistusRow.copy(opiskeluoikeusOid = kaisanOpiskeluoikeusOid))
      }

      "Jos oppilas on jäämässä luokalle käytetään yhdeksännen luokan vuosiluokka suoritusta" in {
        withAdditionalVuosiluokkaSuoritus(MockOppijat.koululainen, yhdeksännenLuokanLuokallejääntiSuoritus) {
          val result = PerusopetuksenVuosiluokka.buildRaportti(repository, MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2016, 1, 1), "10")
          val kaisanOpiskeluoikeusOid = getOpiskeluoikeudet(MockOppijat.koululainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
          val kaisanRivi = result.find(_.opiskeluoikeusOid == kaisanOpiskeluoikeusOid)
          kaisanRivi shouldBe defined

          kaisanRivi.get should equal(kaisanYhdeksännenLuokanLuokalleJääntiRow.copy(opiskeluoikeusOid = kaisanOpiskeluoikeusOid))
        }
      }
    }
  }

  val defaultYnjeviExpectedKasiLuokkaRow = PerusopetusRow(
    opiskeluoikeusOid = "",
    lähdejärjestelmä = None,
    lähdejärjestelmänId = None,
    oppijaOid = MockOppijat.ysiluokkalainen.oid,
    hetu = MockOppijat.ysiluokkalainen.hetu,
    sukunimi = Some(MockOppijat.ysiluokkalainen.sukunimi),
    etunimet = Some(MockOppijat.ysiluokkalainen.etunimet),
    sukupuoli = None,
    luokka = "8C",
    viimeisinTila = "lasna",
    suorituksenTila = "valmis",
    suorituksenVahvistuspaiva = "2015-05-30",
    voimassaolevatVuosiluokat = "9",
    jaaLuokalle = false,
    aidinkieli = "9",
    pakollisenAidinkielenOppimaara = "Suomen kieli ja kirjallisuus",
    kieliA = "8",
    kieliAOppimaara = "englanti",
    kieliB = "8",
    kieliBOppimaara = "ruotsi",
    uskonto = "10",
    historia = "8",
    yhteiskuntaoppi = "10",
    matematiikka = "9",
    kemia = "7",
    fysiikka = "9",
    biologia = "9",
    maantieto = "9",
    musiikki = "7",
    kuvataide = "8",
    kotitalous = "8",
    terveystieto = "8",
    kasityo = "9",
    liikunta = "9",
    ymparistooppi = "Arvosana puuttuu",
    kayttaymisenArvio = "S",
    paikallistenOppiaineidenKoodit = "TH",
    pakollisetPaikalliset = "",
    valinnaisetPaikalliset = "Tietokoneen hyötykäyttö (TH)",
    valinnaisetValtakunnalliset = "ruotsi (B1),Kotitalous (KO),Liikunta (LI),saksa (B2)",
    valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "saksa (B2) 4.0",
    valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "ruotsi (B1) 1.0,Kotitalous (KO) 1.0,Liikunta (LI) 0.5",
    numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetEiLaajuutta = "Tietokoneen hyötykäyttö (TH)",
    majoitusetu = false,
    kuljetusetu = false,
    kotiopetus = false,
    ulkomailla = false,
    perusopetuksenAloittamistaLykatty = false,
    aloittanutEnnenOppivelvollisuutta = false,
    pidennettyOppivelvollisuus = false,
    tehostetunTuenPaatos = false,
    joustavaPerusopetus = false,
    vuosiluokkiinSitoutumatonOpetus = false,
    vammainen = false,
    vaikeastiVammainen = false,
    oikeusMaksuttomaanAsuntolapaikkaan = false,
    sisaoppilaitosmainenMaijoitus = false,
    koulukoti = false,
    erityisenTuenPaatos = "",
    tukimuodot = ""
  )

  val ynjevinExpectedKasiLuokkaRowWithLisätiedot = defaultYnjeviExpectedKasiLuokkaRow.copy(
    majoitusetu = true,
    kuljetusetu = false,
    kotiopetus = false,
    ulkomailla = false,
    perusopetuksenAloittamistaLykatty = true,
    aloittanutEnnenOppivelvollisuutta = false,
    pidennettyOppivelvollisuus = true,
    tehostetunTuenPaatos = true,
    joustavaPerusopetus = true,
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = true,
    vaikeastiVammainen = true,
    oikeusMaksuttomaanAsuntolapaikkaan = true,
    sisaoppilaitosmainenMaijoitus = true,
    koulukoti = true,
    erityisenTuenPaatos = "Opetus on kokonaan erityisryhmissä tai -luokassa,Opetuksesta 20-49 % on yleisopetuksen ryhmissä",
    tukimuodot = "Osa-aikainen erityisopetus"
  )

  val kahdeksannenLuokanLuokalleJääntiRow = defaultYnjeviExpectedKasiLuokkaRow.copy(
    jaaLuokalle = true,
    luokka = "8B",
    viimeisinTila = "lasna",
    suorituksenTila = "valmis",
    suorituksenVahvistuspaiva = "2014-05-30",
    voimassaolevatVuosiluokat = "9",
    aidinkieli = "4",
    pakollisenAidinkielenOppimaara = "Suomen kieli ja kirjallisuus",
    kieliA = "4",
    kieliAOppimaara = "englanti",
    kieliB = "4",
    kieliBOppimaara = "ruotsi",
    uskonto = "4",
    historia = "4",
    yhteiskuntaoppi = "4",
    matematiikka = "4",
    kemia = "4",
    fysiikka = "4",
    biologia = "4",
    maantieto = "4",
    musiikki = "4",
    kuvataide = "4",
    kotitalous = "4",
    terveystieto = "4",
    kasityo = "4",
    liikunta = "4",
    ymparistooppi = "Arvosana puuttuu",
    kayttaymisenArvio = "",
    paikallistenOppiaineidenKoodit = "",
    pakollisetPaikalliset = "",
    valinnaisetPaikalliset = "",
    valinnaisetValtakunnalliset = "",
    valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetEiLaajuutta = "",
  )

  val kaisanYhdeksännenLuokanLuokalleJääntiRow = kahdeksannenLuokanLuokalleJääntiRow.copy(
    oppijaOid = MockOppijat.koululainen.oid,
    hetu = MockOppijat.koululainen.hetu,
    sukunimi = Some(MockOppijat.koululainen.sukunimi),
    etunimet = Some(MockOppijat.koululainen.etunimet),
    luokka = "9A",
    viimeisinTila = "lasna",
    suorituksenTila = "valmis",
    voimassaolevatVuosiluokat = ""
  )

  val kaisanPäättötodistusRow = defaultYnjeviExpectedKasiLuokkaRow.copy(
    oppijaOid = MockOppijat.koululainen.oid,
    hetu = MockOppijat.koululainen.hetu,
    sukunimi = Some(MockOppijat.koululainen.sukunimi),
    etunimet = Some(MockOppijat.koululainen.etunimet),
    sukupuoli = None,
    luokka = "",
    viimeisinTila = "lasna",
    suorituksenTila = "valmis",
    suorituksenVahvistuspaiva = "2016-06-04",
    voimassaolevatVuosiluokat = "",
    kayttaymisenArvio = "",
  )

  private def withAdditionalVuosiluokkaSuoritus(oppija: OppijaHenkilö, vuosiluokanSuoritus: PerusopetuksenVuosiluokanSuoritus)(f: => Any) = {
    val oo = getOpiskeluoikeudet(oppija.oid).collect { case oo: PerusopetuksenOpiskeluoikeus => oo }.head
    val lisatyllaVuosiluokanSuorituksella = oo.copy(suoritukset = (vuosiluokanSuoritus :: oo.suoritukset))
    putOppija(Oppija(oppija, List(lisatyllaVuosiluokanSuorituksella))) {
      loadRaportointikantaFixtures
      f
    }
  }

  private def withLisätiedotFixture[T <: PerusopetuksenOpiskeluoikeus](oppija: OppijaHenkilö, lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot)(f: => Any) = {
    val oo = lastOpiskeluoikeus(oppija.oid).asInstanceOf[T].copy(lisätiedot = Some(lisätiedot))
    putOppija(Oppija(oppija, List(oo))) {
      loadRaportointikantaFixtures
      f
    }
  }

  private val defaultQuery = makeQueryString(MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2016, 1, 1), "9")

  private def makeQueryString(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String) = {
    s"oppilaitosOid=$oppilaitosOid&paiva=${paiva.toString}&vuosiluokka=$vuosiluokka"
  }

  private val kahdeksannenLuokanLuokallejääntiSuoritus = PerusopetusExampleData.seitsemännenLuokanLuokallejääntiSuoritus.copy(
    koulutusmoduuli = PerusopetuksenLuokkaAste(8, PerusopetusExampleData.perusopetuksenDiaarinumero),
    luokka = "8B"
  )

  private val yhdeksännenLuokanLuokallejääntiSuoritus = PerusopetusExampleData.seitsemännenLuokanLuokallejääntiSuoritus.copy(
    koulutusmoduuli = PerusopetuksenLuokkaAste(9, PerusopetusExampleData.perusopetuksenDiaarinumero),
    luokka = "9A"
  )

  private val mennytAikajakso = Aikajakso(date(2000, 1, 1), Some(date(2001, 1, 1)))
  private val voimassaolevaAikajakso = Aikajakso(date(2008, 1, 1), None)
  private val aikajakso = voimassaolevaAikajakso.copy(loppu = Some(date(2018, 1, 1)))
  private val aikajaksot = Some(List(aikajakso))
  private val tukimuodot = Some(List(Koodistokoodiviite("1", "perusopetuksentukimuoto")))
  private val erityisenTuenPäätös = ErityisenTuenPäätös(
    alku = Some(date(2014, 1, 1)),
    loppu = Some(date(2018, 1, 1)),
    opiskeleeToimintaAlueittain = true,
    erityisryhmässä = Some(true),
    toteutuspaikka = Some(Koodistokoodiviite("1", "erityisopetuksentoteutuspaikka"))
  )

  private val perusopetuksenOpiskeluoikeudenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    perusopetuksenAloittamistaLykätty = true,
    pidennettyOppivelvollisuus = Some(voimassaolevaAikajakso),
    tukimuodot = tukimuodot,
    erityisenTuenPäätös = Some(erityisenTuenPäätös),
    erityisenTuenPäätökset = Some(List(
      erityisenTuenPäätös.copy(alku = Some(date(2016, 1, 1)), toteutuspaikka = Some(Koodistokoodiviite("2", "erityisopetuksentoteutuspaikka"))),
      erityisenTuenPäätös.copy(toteutuspaikka = Some(Koodistokoodiviite("3", "erityisopetuksentoteutuspaikka")))
    )),
    tehostetunTuenPäätös = Some(voimassaolevaAikajakso),
    tehostetunTuenPäätökset = aikajaksot,
    joustavaPerusopetus = Some(voimassaolevaAikajakso),
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = aikajaksot,
    vaikeastiVammainen = aikajaksot,
    majoitusetu = Some(voimassaolevaAikajakso),
    kuljetusetu = Some(mennytAikajakso),
    oikeusMaksuttomaanAsuntolapaikkaan = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = aikajaksot,
    koulukoti = aikajaksot
  )
}
