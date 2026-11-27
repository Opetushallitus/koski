package fi.oph.koski.raportit

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AhvenanmaanPerusopetusExampleData._
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.perusopetus.{AhvenanmaanPerusopetuksenVuosiluokkaRaportti, AhvenanmaanPerusopetusRow, PerusopetuksenRaportitRepository}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class AhvenanmaanPerusopetuksenVuosiluokkaRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with PutOpiskeluoikeusTestMethods[AhvenanmaanPerusopetuksenOpiskeluoikeus]
    with DirtiesFixtures {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AhvenanmaanPerusopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus = opiskeluoikeus

  private lazy val repository = PerusopetuksenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  private val perusoppilas = KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas
  // Kotikunta on Maarianhamina
  private val luokalleJäävä = KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021KotikuntaAhvenanmaalla

  private val kevätlukukausi2025 = date(2025, 1, 15)
  private val kevätlukukausi2026 = date(2026, 5, 15)

  override protected def alterFixture(): Unit = {
    setupOppijaWithOpiskeluoikeus(luokalleJäävänOpiskeluoikeus, luokalleJäävä) {
      verifyResponseStatusOk()
    }
    reloadRaportointikanta()
  }

  private def raportti(vuosiluokka: String, päivä: LocalDate, kotikuntaPvm: Option[LocalDate] = None, oppilaitokset: Seq[String] = Seq(MockOrganisaatiot.övernäsSkola), kieli: LocalizationReader = t) =
    AhvenanmaanPerusopetuksenVuosiluokkaRaportti.buildRaportti(repository, oppilaitokset, päivä, kotikuntaPvm, vuosiluokka, kieli)

  private def oppijanRivi(rivit: Seq[AhvenanmaanPerusopetusRow], oppija: LaajatOppijaHenkilöTiedot) =
    rivit.filter(_.oppijaOid == oppija.oid)

  "Ahvenanmaan perusopetuksen vuosiluokkaraportti" - {
    "Vuosiluokan suorituksen tiedot" in {
      val rivit = oppijanRivi(raportti("8", kevätlukukausi2025), perusoppilas)
      rivit should equal(List(kahdeksannenLuokanRivi.copy(opiskeluoikeusOid = opiskeluoikeusOid(perusoppilas))))
    }

    "Ruotsinkielisellä raportilla opiskeluoikeuden tiloista näytetään ruotsinkieliset nimet" in {
      val ruotsi = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "sv", Some(KoskiApplicationForTests.koodistoViitePalvelu))
      val rivi = oppijanRivi(raportti("8", kevätlukukausi2025, kieli = ruotsi), perusoppilas).head
      rivi.viimeisinTila should equal("Utexaminerad")
      rivi.tilaHakupaivalla should equal("Närvarande")
    }

    "Päättävien raportilla oppimäärän suoritus ja päättövuoden luokka" in {
      val rivit = oppijanRivi(raportti("9", kevätlukukausi2026), perusoppilas)
      rivit should equal(List(kahdeksannenLuokanRivi.copy(
        opiskeluoikeusOid = opiskeluoikeusOid(perusoppilas),
        suorituksenAlkamispaiva = "",
        suorituksenVahvistuspaiva = "2026-06-04",
        luokka = Some("9A"),
      )))
    }

    "Luokalle jäävä" - {
      lazy val rivi = oppijanRivi(raportti("9", kevätlukukausi2026), luokalleJäävä).head

      "on päättävien raportilla vuosiluokan suorituksella" in {
        rivi.jaaLuokalle should be(true)
        rivi.luokka should equal(Some("9B"))
        rivi.suorituksenAlkamispaiva should equal("2025-08-15")
      }

      "kotikunta on ahvenanmaalainen kunta" in {
        rivi.kotikunta should equal(Some("Maarianhamina"))
      }

      "kotikunta haetaan kotikuntahistoriasta" in {
        val historiasta = oppijanRivi(raportti("9", kevätlukukausi2026, kotikuntaPvm = Some(date(2026, 1, 1))), luokalleJäävä).head
        historiasta.kotikunta should equal(Some("Maarianhamina"))
      }

      "mukautettu oppimäärä merkitään tähdellä ja laajuus näytetään, jos se on siirretty" in {
        rivi.matematiikka should equal("6* (3.0)")
        rivi.ruotsi should equal("7")
        rivi.mukautetutYhteisetOppiaineet should equal("MA")
      }

      "valinnaiset ja paikalliset oppiaineet arvosanoineen ja laajuuksineen" in {
        rivi.valinnaisetValtakunnalliset should equal("suomi (B1) 7 (2.0)")
        rivi.valinnaisetPaikalliset should equal("Kokkikurssi (KOK) G")
        rivi.paikallistenOppiaineidenKoodit should equal("KOK")
        rivi.pakollisetPaikalliset should equal("")
      }

      "vastuu ja yhteistyö sekä kotiopetus" in {
        rivi.vastuuJaYhteistyoArvio should equal("G")
        rivi.kotiopetus should be(true)
      }
    }

    "Muut kuin oppivelvolliset eivät ole raportilla" in {
      val aikuinen = KoskiSpecificMockOppijat.ahvenanmaanAikuisopiskelija
      (1 to 9).flatMap(vuosiluokka => raportti(vuosiluokka.toString, kevätlukukausi2026)).map(_.oppijaOid) should not contain aikuinen.oid
    }

    "Manner-Suomen perusopetus ei ole raportilla" in {
      raportti("9", date(2016, 5, 15), oppilaitokset = Seq(MockOrganisaatiot.jyväskylänNormaalikoulu)) shouldBe empty
    }
  }

  private def opiskeluoikeusOid(oppija: LaajatOppijaHenkilöTiedot): String =
    lastOpiskeluoikeus(oppija.oid).oid.get

  private lazy val luokalleJäävänOpiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus = {
    val ysiluokka = ysiluokanSuoritus.copy(
      luokka = "9B",
      jääLuokalle = true,
      vastuuJaYhteistyöArvio = Some(AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi()),
      osasuoritukset = Some(List(
        oppiaineenSuoritus(oppiaine("SV", "Svenska"), 7, date(2026, 6, 4)),
        oppiaineenSuoritus(oppiaine("MA", "Matematik").copy(laajuus = Some(LaajuusVuosiviikkotunneissa(3))), 6, date(2026, 6, 4))
          .copy(mukautettuOppimäärä = true),
        oppiaineenSuoritus(vierasKieli("B1", "FI").copy(pakollinen = false, laajuus = Some(LaajuusVuosiviikkotunneissa(2))), 7, date(2026, 6, 4)),
        AhvenanmaanPerusopetuksenOppiaineenSuoritus(
          koulutusmoduuli = AhvenanmaanPerusopetuksenPaikallinenOppiaine(
            tunniste = PaikallinenKoodi("KOK", Finnish("Kokkikurssi")),
            kuvaus = Finnish("Kokkikurssi"),
          ),
          arviointi = sanallinen("G"),
        ),
      ))
    )
    opiskeluoikeus.copy(
      tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(List(
        AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2017, 8, 15), opiskeluoikeusLäsnä)
      )),
      lisätiedot = Some(AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot(
        kotiopetusjaksot = Some(List(Aikajakso(date(2026, 3, 1), Some(date(2026, 5, 31)))))
      )),
      suoritukset = List(kahdeksannenLuokanSuoritus, ysiluokka),
    )
  }

  private val puuttuu = "Oppiaine puuttuu"

  private lazy val kahdeksannenLuokanRivi = AhvenanmaanPerusopetusRow(
    opiskeluoikeusOid = "",
    oppilaitoksenNimi = "Övernäs skola",
    oppilaitosRaportointipäivänä = None,
    lähdejärjestelmä = None,
    lähdejärjestelmänId = None,
    päivitetty = LocalDate.now(),
    yksiloity = true,
    oppijaOid = perusoppilas.oid,
    oppijaMasterOid = Some(perusoppilas.oid),
    hetu = perusoppilas.hetu,
    sukunimi = perusoppilas.sukunimi,
    etunimet = perusoppilas.etunimet,
    sukupuoli = None,
    kotikunta = None,
    opiskeluoikeudenAlkamispäivä = Some(date(2017, 8, 15)),
    viimeisinTila = "valmistunut",
    tilaHakupaivalla = "lasna",
    suorituksenTila = "valmis",
    suorituksenAlkamispaiva = "2024-08-15",
    suorituksenVahvistuspaiva = "2025-06-04",
    jaaLuokalle = false,
    luokka = Some("8A"),
    voimassaolevatVuosiluokat = "",
    ruotsi = "9",
    ruotsiToisenaKielena = puuttuu,
    kieliA1 = "9",
    kieliA1Oppimaara = "englanti",
    kieliA2 = puuttuu,
    kieliA2Oppimaara = puuttuu,
    kieliB1 = puuttuu,
    kieliB1Oppimaara = puuttuu,
    kieliB2 = puuttuu,
    kieliB2Oppimaara = puuttuu,
    matematiikka = "8",
    ymparistooppi = puuttuu,
    biologia = "8",
    maantieto = "7",
    fysiikkaJaKemia = puuttuu,
    fysiikka = "7",
    kemia = "8",
    terveystieto = "8",
    uskontoJaElamankatsomustieto = "8",
    historia = "9",
    musiikki = "9",
    yhteiskuntaoppi = "8",
    kuvataide = "8",
    kasityo = puuttuu,
    tekstiilityo = "8",
    tekninenTyo = "7",
    liikunta = "9",
    kotitalous = "8",
    oppilaanohjaus = puuttuu,
    vastuuJaYhteistyoArvio = "",
    mukautetutYhteisetOppiaineet = "",
    paikallistenOppiaineidenKoodit = "",
    pakollisetPaikalliset = "",
    valinnaisetPaikalliset = "",
    valinnaisetValtakunnalliset = "suomi (B1) 7",
    vahvistetutToimintaAlueidenSuoritukset = "",
    kotiopetus = false,
  )
}
