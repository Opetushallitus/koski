package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesPerusopetus._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.perusopetus.{PerusopetuksenRaportitRepository, PerusopetuksenVuosiluokkaRaportti, PerusopetusRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PerusopetuksenVuosiluokkaRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with OpiskeluoikeusTestMethodsPerusopetus
    with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  private lazy val repository = PerusopetuksenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  "Perusopetuksenvuosiluokka raportti" - {

    "Raportin lataaminen toimii" in {
      verifyPerusopetukseVuosiluokkaRaportinLataaminen(
        queryString = defaultQuery(lang = "fi"),
        apiUrl = "api/raportit/perusopetuksenvuosiluokka",
        expectedRaporttiNimi = "perusopetuksenvuosiluokka",
        expectedFileNamePrefix = "Perusopetuksen_vuosiluokka")
    }

    "Raportin lataaminen toimii eri lokalisaatiolla" in {
      verifyPerusopetukseVuosiluokkaRaportinLataaminen(
        queryString = defaultQuery(lang = "sv"),
        apiUrl = "api/raportit/perusopetuksenvuosiluokka",
        expectedRaporttiNimi = "perusopetuksenvuosiluokka",
        expectedFileNamePrefix = "Grundläggande_utbildning_årskurs"
      )
    }

    "Tuottaa oikeat tiedot" in {
      withLisätiedotFixture(KoskiSpecificMockOppijat.ysiluokkalainen, perusopetuksenOpiskeluoikeudenLisätiedot.copy(
        vammainen = None,
        vaikeastiVammainen = None,
        pidennettyOppivelvollisuus = None,
        erityisenTuenPäätös = None,
        erityisenTuenPäätökset = None,
      )) {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), LocalDate.of(2014, 8, 15), None, vuosiluokka = "8", t)
        val ynjevinOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ysiluokkalainen.oid).oid.get
        val rivi = result.find(_.opiskeluoikeusOid == ynjevinOpiskeluoikeusOid)

        rivi should equal(
          Some(ynjevinExpectedKasiLuokkaRowWithLisätiedot.copy(opiskeluoikeusOid = ynjevinOpiskeluoikeusOid))
        )
      }
    }

    "Tuottaa oikeat tiedot tuen päätöksen jaksolle" in {
      val hakupäivä = LocalDate.of(2026, 8, 1)
      val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(suoritukset = List(
        createVuosiluokanSuoritus(Some(date(2026, 8, 1)), None)
          .copy(osasuoritukset = Some(List(
            suoritus(oppiaine("HI", vuosiviikkotuntia(1)))
              .copy(arviointi = arviointi(8), rajattuOppimäärä = true),
            suoritus(oppiaine("KE", vuosiviikkotuntia(1)))
              .copy(arviointi = arviointi(8, arviointipäivä = Some(hakupäivä)), luokkaAste = perusopetuksenLuokkaAste("7"))
          )))
      )).copy(
        tila = opiskeluoikeusKesken,
        lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
          tuenPäätöksenJaksot = Some(List(Tukijakso(Some(hakupäivä), None))),
          opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = Some(List(Aikajakso(hakupäivä, None))),
          tavoitekokonaisuuksittainOpiskelu = Some(List(Aikajakso(Some(hakupäivä), None)))
      )))
      putOpiskeluoikeus(
        opiskeluoikeus,
        KoskiSpecificMockOppijat.lukioKesken
      ) {
        verifyResponseStatusOk()
      }

      reloadRaportointikanta

      val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), hakupäivä, None, vuosiluokka = "8", t)
      val opiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.lukioKesken.oid).oid.get
      val rivi = result.find(_.opiskeluoikeusOid == opiskeluoikeusOid)

      rivi should equal(
        Some(leilanRow.copy(opiskeluoikeusOid = opiskeluoikeusOid))
      )
    }

    "Ei näytetä laajuuksia kun päätason suorituksen arviointipäivä on 31.7.2020 tai ennen" in {
      withAdditionalSuoritukset(KoskiSpecificMockOppijat.ysiluokkalainen, List(seitsemännenLuokanSuoritusLaajuudet_ennen_1_8_2020)) {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), LocalDate.of(2019, 8, 15), None, vuosiluokka = "7", t)
        val oppijaOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ysiluokkalainen.oid).oid.get
        val rivi = result.find(_.opiskeluoikeusOid == oppijaOpiskeluoikeusOid)

        rivi.get.aidinkieli should equal("9")
        rivi.get.kieliA2 should equal("Oppiaine puuttuu")
        rivi.get.biologia should equal("9*")
        rivi.get.uskonto should equal("10")
      }
    }

    "Näytetään laajuudet kun päätason suorituksen arviointipäivä on 1.8.2020 tai jälkeen" in {
      withAdditionalSuoritukset(KoskiSpecificMockOppijat.ysiluokkalainen, List(seitsemännenLuokanSuoritusLaajuudet_jälkeen_1_8_2020)) {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), LocalDate.of(2020, 8, 1), None, vuosiluokka = "7", t)
        val oppijaOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ysiluokkalainen.oid).oid.get
        val rivi = result.find(_.opiskeluoikeusOid == oppijaOpiskeluoikeusOid)

        rivi.get.aidinkieli should equal("9 (1.0)")
        rivi.get.kieliA2 should equal("Oppiaine puuttuu")
        rivi.get.biologia should equal("9* (1.0)")
        rivi.get.uskonto should equal("10 (1.0)")
      }
    }


    "Monta saman vuosiluokan suoritusta eritellään omiksi riveiksi" in {
      withAdditionalSuoritukset(KoskiSpecificMockOppijat.ysiluokkalainen, List(kahdeksannenLuokanLuokalleJääntiSuoritus.copy(luokka = "8C"))) {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), LocalDate.of(2014, 8, 15), None, vuosiluokka = "8", t)
        val ynjevinOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ysiluokkalainen.oid).oid.get
        val rivit = result.filter(_.opiskeluoikeusOid == ynjevinOpiskeluoikeusOid)

        rivit.length should equal(2)
        rivit should contain(defaultYnjeviExpectedKasiLuokkaRow.copy(opiskeluoikeusOid = ynjevinOpiskeluoikeusOid))
        rivit should contain(kahdeksannenLuokanLuokalleJääntiRow.copy(opiskeluoikeusOid = ynjevinOpiskeluoikeusOid))
      }
    }

    "Raportille ei päädy vuosiluokkien suorituksia joiden vahvistuspäivä on menneisyydessä" in {
      val hakuDate = date(2015, 5, 30)
      val rows = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), hakuDate, None, "7", t)
      rows.map(_.suorituksenVahvistuspaiva).foreach(paivaStr => {
        if (!paivaStr.isEmpty) {
          val paiva = LocalDate.parse(paivaStr)
          paiva.isBefore(hakuDate) shouldBe (false)
        }
      })
    }

    "Toiminta alueiden suoritukset joilla on arvosana näytetään omassa kolumnissaan" in {
      val suoritusToimintaAlueenOsasuorituksilla = yhdeksännenLuokanSuoritus.copy(osasuoritukset = toimintaAlueOsasuoritukset, vahvistus = None)
      withAdditionalSuoritukset(KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija, List(suoritusToimintaAlueenOsasuorituksilla)) {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2015, 9, 1), None, "9", t)
        val rows = result.filter(_.oppijaOid == KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija.oid)
        rows.length should equal(1)
        val row = rows.head

        row.valinnaisetPaikalliset should equal("")
        row.valinnaisetValtakunnalliset should equal("")
        row.vahvistetutToimintaAlueidenSuoritukset should equal(
          "motoriset taidot (1),kieli ja kommunikaatio (2),sosiaaliset taidot (3),päivittäisten toimintojen taidot (4),kognitiiviset taidot (5)"
        )
      }
    }

    "Elämänkatsomustiedon opiskelijoille ei yritetä hakea uskonnon oppimäärää" in {
      val suoritusElämänkatsomustiedolla = kahdeksannenLuokanSuoritus.copy(osasuoritukset = Some(List(suoritus(uskonto(oppiaineenKoodiarvo = "ET")).copy(arviointi = arviointi(5)))), vahvistus = None)
      withAdditionalSuoritukset(KoskiSpecificMockOppijat.vuosiluokkalainen, List(suoritusElämänkatsomustiedolla)) {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2016, 1, 1), None, "8", t)
        val rows = result.filter(_.oppijaOid == KoskiSpecificMockOppijat.vuosiluokkalainen.oid)
        rows.length should equal(1)
        val row = rows.head

        row.elamankatsomustieto should equal("5")
        row.uskonto should equal("Oppiaine puuttuu")
      }
    }

    "Peruskoulun päättävät" - {

      "Hakee tiedot peruskoulun oppimäärän suorituksesta" in {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), LocalDate.of(2016, 1, 1), None, "9", t)
        val kaisanOpiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.koululainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
        val rivit = result.filter(_.opiskeluoikeusOid == kaisanOpiskeluoikeusOid)
        rivit.length should equal(1)
        val kaisanRivi = rivit.head

        kaisanRivi should equal(kaisanPäättötodistusRow.copy(opiskeluoikeusOid = kaisanOpiskeluoikeusOid))
      }

      "Jos oppilas on jäämässä luokalle käytetään yhdeksännen luokan vuosiluokka suoritusta" in {
        withAdditionalSuoritukset(KoskiSpecificMockOppijat.vuosiluokkalainen, List(perusopetuksenOppimääränSuoritus, yhdeksännenLuokanLuokallejääntiSuoritus, kahdeksannenLuokanSuoritus)) {
          val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2016, 1, 1), None, "9", t)
          val opiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vuosiluokkalainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
          val rows = result.filter(_.opiskeluoikeusOid == opiskeluoikeusOid)
          rows.length should equal(1)
          rows.head should equal(yhdeksännenLuokanLuokalleJääntiRow.copy(opiskeluoikeusOid = opiskeluoikeusOid))
        }
      }

      "Oman äidinkielen tiedot näkyvät oikein" in {
        val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2016, 1, 1), None, "9", t)
        val opiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.ysiluokkalainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
        val rows = result.filter(_.opiskeluoikeusOid == opiskeluoikeusOid)
        rows.length should equal(1)
        rows.head.omanÄidinkielenLaajuusJaArvosana should equal("S laajuus: 1.0")
        rows.head.omanÄidinkielenKieli should equal("saame, lappi")
      }

      "Jos oppilas on jäämässä luokalle käytetään yhdeksännen luokan vuosiluokka suoritusta (ei vahvistusta luokalle jäänti suorituksella)" in {
        withAdditionalSuoritukset(KoskiSpecificMockOppijat.vuosiluokkalainen, List(perusopetuksenOppimääränSuoritus.copy(vahvistus = None), yhdeksännenLuokanLuokallejääntiSuoritus.copy(vahvistus = None), kahdeksannenLuokanSuoritus)) {
          val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2016, 1, 1), None, "9", t)
          val opiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vuosiluokkalainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
          val rows = result.filter(_.opiskeluoikeusOid == opiskeluoikeusOid)
          rows.length should equal(1)
          rows.head should equal(yhdeksännenLuokanLuokalleJääntiRow.copy(opiskeluoikeusOid = opiskeluoikeusOid, suorituksenTila = "kesken", suorituksenVahvistuspaiva = "", voimassaolevatVuosiluokat = "9"))
        }
      }

      "Jos oppilas on jäänyt aikaisemmin luokalle mutta nyt suorittanut perusopetuksen oppimäärän" in {
        withAdditionalSuoritukset(KoskiSpecificMockOppijat.vuosiluokkalainen, List(perusopetuksenOppimääränSuoritus.copy(vahvistus = None), yhdeksännenLuokanSuoritus, yhdeksännenLuokanLuokallejääntiSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(date(2015, 1, 1))), kahdeksannenLuokanSuoritus)) {
          val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2016, 1, 1), None, "9", t)
          val opiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vuosiluokkalainen.oid).find(_.tyyppi.koodiarvo == "perusopetus").get.oid.get
          val rows = result.filter(_.opiskeluoikeusOid == opiskeluoikeusOid)
          rows.length should equal(1)
          rows.head should equal(kaisanPäättötodistusRow.copy(opiskeluoikeusOid = opiskeluoikeusOid, oppijaOid = KoskiSpecificMockOppijat.vuosiluokkalainen.oid, hetu = KoskiSpecificMockOppijat.vuosiluokkalainen.hetu, sukunimi = KoskiSpecificMockOppijat.vuosiluokkalainen.sukunimi, etunimet = KoskiSpecificMockOppijat.vuosiluokkalainen.etunimet, viimeisinTila = "lasna", suorituksenTila = "kesken", suorituksenVahvistuspaiva = "", luokka = Some("9A,9C")))
        }
      }

      "Ei tulosta päättötodistusta oppijoilla joilla ei ole yhdeksännen luokan opintoja" in {
        withAdditionalSuoritukset(KoskiSpecificMockOppijat.vuosiluokkalainen, List(perusopetuksenOppimääränSuoritus), Some(perusopetuksenOpiskeluoikeudenLisätiedot.copy(
          vuosiluokkiinSitoutumatonOpetus = true,
          pidennettyOppivelvollisuus = None,
          vammainen = None,
          vaikeastiVammainen = None,
          erityisenTuenPäätös = None,
          erityisenTuenPäätökset = None,
        ))) {
          val result = PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2016, 6, 1), None, "9", t)
          result.map(_.oppijaOid) shouldNot contain(KoskiSpecificMockOppijat.vuosiluokkalainen.oid)
        }
      }

      "Suorituksen alkamispäivä huomioidaan" in {
        withAdditionalSuoritukset(
          KoskiSpecificMockOppijat.vuosiluokkalainen,
          List(
            yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(date(2015, 10, 1))),
            perusopetuksenOppimääränSuoritus
          )
        ) {
          PerusopetuksenVuosiluokkaRaportti
            .buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2015, 9, 1), None, "9", t)
            .map(_.oppijaOid) shouldNot contain(KoskiSpecificMockOppijat.vuosiluokkalainen.oid)

          PerusopetuksenVuosiluokkaRaportti
            .buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2015, 11, 1), None, "9", t)
            .map(_.oppijaOid) should contain(KoskiSpecificMockOppijat.vuosiluokkalainen.oid)
        }
      }
    }

    "Raportilla näytettävien vuosiluokan suoritusten valinta" - {
      lazy val raportti = {
        insertTestData
        PerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), date(2015, 2, 2), None, "8", t)
      }
      "Viimeisin suoritus on vahvistettu suoritus haetulta vuosiluokalta" - {
        "Näytetään, vaikka hakupäivä ei osu alku- ja vahvistuspäivän väliin" in {
          raportti.filter(_.oppijaOid == KoskiSpecificMockOppijat.eero.oid).length shouldBe(1)
        }
        "Ei näytetä duplikaattina jos hakupäivä osuu alku- ja vahvistuspäivän väliin" in {
          raportti.filter(_.oppijaOid == KoskiSpecificMockOppijat.eerola.oid).length shouldBe(1)
        }
      }
      "Kaksi saman vuosiluokan suoritusta. Näytetään molemmat, kun hakupäivä osuu suoritusten alku- ja vahvistuspäivien väliin" in {
        raportti.filter(_.oppijaOid == KoskiSpecificMockOppijat.tero.oid).length shouldBe(2)
      }
      "Jos uusii vuosiluokkaa, ei poimita vanhaa suoritusta mukaan, kun hakupäivä ei osu vanhan suorituksen alku- ja vahvistuspäivän väliin" in {
        raportti.filter(_.oppijaOid === KoskiSpecificMockOppijat.teija.oid).length shouldBe(1)
      }
      "Jos löytyy ylemmän vuosiluokan suoritus, ei poimita mukaan" in {
        raportti.filter(_.oppijaOid === KoskiSpecificMockOppijat.markkanen.oid).length shouldBe(0)
      }
    }

    "Koulutustoimijalla voidaan hakea sen alaisuudessa olevien oppilaitosten suoritukset" - {

      lazy val raportitService = new RaportitService(KoskiApplicationForTests)

      "Vuosiluokan suoritus" in {
        val request = PerusopetuksenVuosiluokkaRequest(MockOrganisaatiot.helsinginKaupunki, None, "", date(2015, 1, 1), "8", "fi", None)
        val rows = raportitService.perusopetuksenVuosiluokka(request, t).sheets.collect { case dSheet: DataSheet => dSheet }
        rows.flatMap(_.rows.map(_.asInstanceOf[PerusopetusRow])).map(_.oppilaitoksenNimi).toSet should equal(Set("Stadin ammatti- ja aikuisopisto"))
      }

      "Peruskoulun päättävät" in {
        val request = PerusopetuksenVuosiluokkaRequest(MockOrganisaatiot.helsinginKaupunki, None, "", date(2015, 9, 1), "9", "fi", None)
        val rows = raportitService.perusopetuksenVuosiluokka(request, t).sheets.collect { case dSheet: DataSheet => dSheet }
        rows.flatMap(_.rows.map(_.asInstanceOf[PerusopetusRow])).map(_.oppilaitoksenNimi).toSet should equal(Set("Stadin ammatti- ja aikuisopisto"))
      }
    }

    "Näyttää raportin tulostushetken oppilaitoksen jos eri kuin nykyinen" in {
      lazy val raportti = PerusopetuksenVuosiluokkaRaportti.buildRaportti(
        repository,
        Seq(MockOrganisaatiot.kulosaarenAlaAste),
        LocalDate.of(2012, 8, 15),
        None,
        vuosiluokka = "6",
        t
      )
      val oppijaOid = KoskiSpecificMockOppijat.organisaatioHistoriallinen.oid
      val rivi = raportti.find(_.oppijaOid == oppijaOid).get
      rivi.oppilaitosRaportointipäivänä should equal(Some("1.1.2006-31.12.2012: Jyväskylän normaalikoulu (Helsingin kaupunki)"))
    }

    "Kotikunnan hakeminen kotikuntahistoriasta" - {
      def getKotikuntahistoriaaKäyttäväRaportti(kotikuntaPvm: LocalDate) =
        PerusopetuksenVuosiluokkaRaportti.buildRaportti(
          repository,
          Seq(MockOrganisaatiot.kulosaarenAlaAste),
          LocalDate.of(2012, 8, 15),
          Some(kotikuntaPvm),
          vuosiluokka = "6",
          t
        )

      "Kotikunta löytyy historiasta" in {
        val raportti = getKotikuntahistoriaaKäyttäväRaportti(LocalDate.of(2024, 1, 1))
        val oppijaOid = KoskiSpecificMockOppijat.monessaKoulussaOllut.oid
        val rivi = raportti.find(_.oppijaOid == oppijaOid).get
        rivi.kotikunta should equal(Some("Juva"))
      }

      "Kotikunta ei löydy historiasta" in {
        val raportti = getKotikuntahistoriaaKäyttäväRaportti(LocalDate.of(2024, 6, 2))
        val oppijaOid = KoskiSpecificMockOppijat.monessaKoulussaOllut.oid
        val rivi = raportti.find(_.oppijaOid == oppijaOid).get
        rivi.kotikunta should equal(Some("Ei tiedossa 2.6.2024 (nykyinen kotikunta on Helsinki)"))
      }

    }
  }

  val defaultYnjeviExpectedKasiLuokkaRow = PerusopetusRow(
    opiskeluoikeusOid = "",
    oppilaitoksenNimi = "Jyväskylän normaalikoulu",
    oppilaitosRaportointipäivänä = None,
    lähdejärjestelmä = None,
    lähdejärjestelmänId = None,
    yksiloity = true,
    oppijaOid = KoskiSpecificMockOppijat.ysiluokkalainen.oid,
    hetu = KoskiSpecificMockOppijat.ysiluokkalainen.hetu,
    sukunimi = KoskiSpecificMockOppijat.ysiluokkalainen.sukunimi,
    etunimet = KoskiSpecificMockOppijat.ysiluokkalainen.etunimet,
    sukupuoli = None,
    kotikunta = Some("Jyväskylä"),
    luokka = Some("8C"),
    opiskeluoikeudenAlkamispäivä = Some(date(2008, 8, 15)),
    viimeisinTila = "lasna",
    tilaHakupaivalla = "lasna",
    suorituksenTila = "valmis",
    suorituksenAlkamispaiva = "2014-08-15",
    suorituksenVahvistuspaiva = "2015-05-30",
    voimassaolevatVuosiluokat = "9",
    jaaLuokalle = false,
    aidinkieli = "9",
    pakollisenAidinkielenOppimaara = "Suomen kieli ja kirjallisuus",
    omanÄidinkielenLaajuusJaArvosana = "Oppiaine puuttuu",
    omanÄidinkielenKieli = "Oppiaine puuttuu",
    kieliA1 = "8",
    kieliA1Oppimaara = "englanti",
    kieliA2 = "Oppiaine puuttuu",
    kieliA2Oppimaara = "Oppiaine puuttuu",
    kieliB = "8",
    kieliBOppimaara = "ruotsi",
    aidinkielenomainenKieli = "8",
    aidinkielenomainenKieliOppimaara = "suomi",
    uskonto = "10",
    elamankatsomustieto = "Oppiaine puuttuu",
    historia = "8",
    yhteiskuntaoppi = "10",
    matematiikka = "9",
    kemia = "7",
    fysiikka = "9",
    biologia = "9*",
    maantieto = "9",
    musiikki = "7",
    kuvataide = "8",
    kotitalous = "8",
    terveystieto = "8",
    kasityo = "9",
    liikunta = "9",
    ymparistooppi = "Oppiaine puuttuu",
    opintoohjaus = "Oppiaine puuttuu",
    kayttaymisenArvio = "S",
    paikallistenOppiaineidenKoodit = "TH",
    pakollisetPaikalliset = "",
    valinnaisetPaikalliset = "Tietokoneen hyötykäyttö (TH) 9",
    valinnaisetValtakunnalliset = "ruotsi (B1) S,Kotitalous (KO) S,Liikunta (LI) S,saksa (B2) 9",
    valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "saksa (B2) 4.0 9",
    valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "ruotsi (B1) 1.0 S,Kotitalous (KO) 1.0 S,Liikunta (LI) 0.5 S",
    numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetEiLaajuutta = "Tietokoneen hyötykäyttö (TH)",
    vahvistetutToimintaAlueidenSuoritukset = "",
    majoitusetu = false,
    kuljetusetu = false,
    kotiopetus = false,
    ulkomailla = false,
    aloittanutEnnenOppivelvollisuutta = false,
    pidennettyOppivelvollisuus = false,
    joustavaPerusopetus = false,
    vuosiluokkiinSitoutumatonOpetus = false,
    vammainen = false,
    vaikeastiVammainen = false,
    sisäoppilaitosmainenMajoitus = false,
    koulukoti = false,
    erityisenTuenPaatosVoimassa = false,
    erityisenTuenPaatosToimialueittain = false,
    tuenPäätöksenJakso = false,
    opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = false,
    toimintaAlueittainOpiskelu = false,
    tavoitekokonaisuuksittainOpiskelu = false
  )

  val ynjevinExpectedKasiLuokkaRowWithLisätiedot = defaultYnjeviExpectedKasiLuokkaRow.copy(
    majoitusetu = true,
    kuljetusetu = false,
    kotiopetus = false,
    ulkomailla = false,
    aloittanutEnnenOppivelvollisuutta = false,
    pidennettyOppivelvollisuus = false,
    joustavaPerusopetus = true,
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = false,
    vaikeastiVammainen = false,
    sisäoppilaitosmainenMajoitus = true,
    koulukoti = true,
    erityisenTuenPaatosVoimassa = false,
    erityisenTuenPaatosToimialueittain = false
  )

  val kahdeksannenLuokanLuokalleJääntiRow = defaultYnjeviExpectedKasiLuokkaRow.copy(
    jaaLuokalle = true,
    viimeisinTila = "lasna",
    suorituksenAlkamispaiva = "2013-08-15",
    suorituksenTila = "valmis",
    voimassaolevatVuosiluokat = "9",
    aidinkieli = "4",
    pakollisenAidinkielenOppimaara = "Suomen kieli ja kirjallisuus",
    kieliA1 = "4",
    kieliA1Oppimaara = "englanti",
    kieliB = "4",
    kieliBOppimaara = "ruotsi",
    aidinkielenomainenKieli = "4",
    aidinkielenomainenKieliOppimaara = "suomi",
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
    kayttaymisenArvio = "",
    paikallistenOppiaineidenKoodit = "",
    pakollisetPaikalliset = "",
    valinnaisetPaikalliset = "",
    valinnaisetValtakunnalliset = "",
    valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetEiLaajuutta = ""
  )

  val yhdeksännenLuokanLuokalleJääntiRow = kahdeksannenLuokanLuokalleJääntiRow.copy(
    oppijaOid = KoskiSpecificMockOppijat.vuosiluokkalainen.oid,
    hetu = KoskiSpecificMockOppijat.vuosiluokkalainen.hetu,
    sukunimi = KoskiSpecificMockOppijat.vuosiluokkalainen.sukunimi,
    etunimet = KoskiSpecificMockOppijat.vuosiluokkalainen.etunimet,
    kotikunta = None,
    luokka = Some("9A"),
    viimeisinTila = "lasna",
    suorituksenTila = "valmis",
    voimassaolevatVuosiluokat = "",
    suorituksenAlkamispaiva = "2014-08-16",
    suorituksenVahvistuspaiva = "2016-05-30"
  )

  val kaisanPäättötodistusRow = defaultYnjeviExpectedKasiLuokkaRow.copy(
    oppijaOid = KoskiSpecificMockOppijat.koululainen.oid,
    hetu = KoskiSpecificMockOppijat.koululainen.hetu,
    sukunimi = KoskiSpecificMockOppijat.koululainen.sukunimi,
    etunimet = KoskiSpecificMockOppijat.koululainen.etunimet,
    kotikunta = None,
    sukupuoli = None,
    luokka = Some("9C"),
    viimeisinTila = "valmistunut",
    tilaHakupaivalla = "lasna",
    suorituksenTila = "valmis",
    suorituksenAlkamispaiva = "",
    suorituksenVahvistuspaiva = "2016-06-04",
    voimassaolevatVuosiluokat = "",
    kayttaymisenArvio = ""
  )

  val leilanRow = defaultYnjeviExpectedKasiLuokkaRow.copy(
    oppijaOid = KoskiSpecificMockOppijat.lukioKesken.oid,
    hetu = KoskiSpecificMockOppijat.lukioKesken.hetu,
    sukunimi = KoskiSpecificMockOppijat.lukioKesken.sukunimi,
    etunimet = KoskiSpecificMockOppijat.lukioKesken.etunimet,
    kotikunta = None,
    luokka = Some("8C"),
    opiskeluoikeudenAlkamispäivä = Some(date(2008, 1, 1)),
    viimeisinTila = "lasna",
    tilaHakupaivalla = "lasna",
    suorituksenTila = "kesken",
    suorituksenAlkamispaiva = "2026-08-01",
    suorituksenVahvistuspaiva = "",
    voimassaolevatVuosiluokat = "8",
    aidinkieli = "Oppiaine puuttuu",
    pakollisenAidinkielenOppimaara = "Oppiaine puuttuu",
    kieliA1 = "Oppiaine puuttuu",
    kieliA1Oppimaara = "Oppiaine puuttuu",
    kieliB = "Oppiaine puuttuu",
    kieliBOppimaara = "Oppiaine puuttuu",
    aidinkielenomainenKieli = "Oppiaine puuttuu",
    aidinkielenomainenKieliOppimaara = "Oppiaine puuttuu",
    uskonto = "Oppiaine puuttuu",
    historia = "8*", // * == rajattu oppimäärä
    yhteiskuntaoppi = "Oppiaine puuttuu",
    matematiikka = "Oppiaine puuttuu",
    kemia = "8 (7.lk)",
    fysiikka = "Oppiaine puuttuu",
    biologia = "Oppiaine puuttuu",
    maantieto = "Oppiaine puuttuu",
    musiikki = "Oppiaine puuttuu",
    kuvataide = "Oppiaine puuttuu",
    kotitalous = "Oppiaine puuttuu",
    terveystieto = "Oppiaine puuttuu",
    kasityo = "Oppiaine puuttuu",
    liikunta = "Oppiaine puuttuu",
    kayttaymisenArvio = "S",
    paikallistenOppiaineidenKoodit = "",
    pakollisetPaikalliset = "",
    valinnaisetPaikalliset = "",
    valinnaisetValtakunnalliset = "",
    valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
    valinnaisetEiLaajuutta = "",
    tuenPäätöksenJakso = true,
    opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = true,
    tavoitekokonaisuuksittainOpiskelu = true,
  )

  private def insertTestData = {
    val alkupäivä = Some(date(2015, 1, 1))
    val loppupäivä = Some(date(2016, 1, 1))
    addPerusopetus(KoskiSpecificMockOppijat.eero, createVuosiluokanSuoritus(Some(date(2014, 1, 1)), Some(date(2014, 12, 12))))
    addPerusopetus(KoskiSpecificMockOppijat.eerola, createVuosiluokanSuoritus(alkupäivä, loppupäivä))
    addPerusopetus(KoskiSpecificMockOppijat.markkanen, createVuosiluokanSuoritus(Some(date(2015, 1, 1)), None, vuosiluokka = 9), createVuosiluokanSuoritus(Some(date(2014, 1, 1)), Some(date(2015, 1, 1))))
    addPerusopetus(KoskiSpecificMockOppijat.teija, createVuosiluokanSuoritus(Some(date(2014, 1, 1)), Some(date(2014, 12, 12))), createVuosiluokanSuoritus(alkupäivä, None, vuosiluokka = 8))
    addPerusopetus(KoskiSpecificMockOppijat.tero, createVuosiluokanSuoritus(alkupäivä, loppupäivä), createVuosiluokanSuoritus(Some(date(2014, 1, 1)), Some(date(2015, 2, 2)), vuosiluokka = 8))
    reloadRaportointikanta
  }

  private def addPerusopetus(oppija: Henkilö, suoritukset: PerusopetuksenPäätasonSuoritus*) = {
    val opiskeluoikeus = PerusopetusExampleData.opiskeluoikeus(suoritukset = suoritukset.toList).copy(tila = opiskeluoikeusKesken)
    putOpiskeluoikeus(opiskeluoikeus, oppija) { verifyResponseStatusOk() }
  }

  private def opiskeluoikeusKesken = {
    NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 1, 1), opiskeluoikeusLäsnä)))
  }

  private def createVuosiluokanSuoritus(alku: Option[LocalDate], loppu: Option[LocalDate], vuosiluokka: Int = 8) = {
    kahdeksannenLuokanSuoritus.copy(alkamispäivä = alku, vahvistus = loppu.map(vahvistusPaikkakunnalla(_)).getOrElse(None), koulutusmoduuli = PerusopetuksenLuokkaAste(vuosiluokka, perusopetuksenDiaarinumero))
  }

  private def withAdditionalSuoritukset(oppija: LaajatOppijaHenkilöTiedot, vuosiluokanSuoritus: List[PerusopetuksenPäätasonSuoritus], lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None)(f: => Any) = {
    resetFixtures
    val oo = getOpiskeluoikeudet(oppija.oid).collect { case oo: PerusopetuksenOpiskeluoikeus => oo }.head
    val lisatyllaVuosiluokanSuorituksella = oo.copy(suoritukset = (vuosiluokanSuoritus ::: oo.suoritukset),
      lisätiedot = lisätiedot,
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä))))
    putOppija(Oppija(oppija, List(lisatyllaVuosiluokanSuorituksella))) {
      verifyResponseStatusOk()
      reloadRaportointikanta
      f
    }
  }

  private def withLisätiedotFixture[T <: PerusopetuksenOpiskeluoikeus](oppija: LaajatOppijaHenkilöTiedot, lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot)(f: => Any) = {
    val oo = lastOpiskeluoikeus(oppija.oid).asInstanceOf[T].copy(lisätiedot = Some(lisätiedot))
    putOppija(Oppija(oppija, List(oo))) {
      verifyResponseStatusOk()
      reloadRaportointikanta
      f
    }
  }

  private val toimintaAlueOsasuoritukset = Some(List(
    toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", kuvaus = None)),
    toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S", kuvaus = None)),
    toimintaAlueenSuoritus("3"),
    toimintaAlueenSuoritus("4")
  ))

  private def defaultQuery(lang: String) = makeQueryString(MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2016, 1, 1), "9", lang)

  private def makeQueryString(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String, lang: String) = {
    s"oppilaitosOid=$oppilaitosOid&paiva=${paiva.toString}&vuosiluokka=$vuosiluokka&lang=$lang"
  }

  private val mennytAikajakso = Aikajakso(date(2000, 1, 1), Some(date(2001, 1, 1)))
  private val voimassaolevaAikajakso = Aikajakso(date(2008, 1, 1), None)
  private val aikajakso = voimassaolevaAikajakso.copy(loppu = Some(date(2018, 1, 1)))
  private val aikajaksot = Some(List(aikajakso))
  private val erityisenTuenPäätös = ErityisenTuenPäätös(
    alku = Some(date(2008, 1, 1)),
    loppu = Some(date(2018, 1, 1)),
    opiskeleeToimintaAlueittain = true,
    erityisryhmässä = Some(true),
  )

  private val perusopetuksenOpiskeluoikeudenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    perusopetuksenAloittamistaLykätty = None,
    pidennettyOppivelvollisuus = Some(voimassaolevaAikajakso),
    erityisenTuenPäätös = Some(erityisenTuenPäätös),
    erityisenTuenPäätökset = Some(List(
      erityisenTuenPäätös.copy(
        alku = Some(date(2016, 1, 1)),
        loppu = None
      ),
    )),
    joustavaPerusopetus = Some(voimassaolevaAikajakso),
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = Some(List(voimassaolevaAikajakso.copy(
      loppu = Some(voimassaolevaAikajakso.alku.plusDays(10))
    ))),
    vaikeastiVammainen = Some(List(voimassaolevaAikajakso.copy(
      alku = voimassaolevaAikajakso.alku.plusDays(11)
    ))),
    majoitusetu = Some(voimassaolevaAikajakso),
    kuljetusetu = Some(mennytAikajakso),
    oikeusMaksuttomaanAsuntolapaikkaan = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = aikajaksot,
    koulukoti = aikajaksot
  )
}
