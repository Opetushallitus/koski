package fi.oph.koski.raportit

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä, suomenKieli, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.oppilaitos
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.organisaatio.MockOrganisaatiot.{aapajoenKoulu, helsinginKaupunki, jyväskylänNormaalikoulu}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class KotikuntalaskelmaSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsPerusopetus with BeforeAndAfterAll with DirtiesFixtures {
  private val raportointipäivä = date(2026, 9, 1)

  override protected def alterFixture(): Unit = {
    reloadRaportointikanta()
  }

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  private val kotikuntalaskelmaBuilder = Kotikuntalaskelma(application.raportointiDatabase.db)

  private lazy val aggregaattiRivit = kotikuntalaskelmaBuilder
    .build(Seq(aapajoenKoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[KotikuntalaskelmaRow])

  private lazy val oppijatRivit = kotikuntalaskelmaBuilder
    .buildOppijat(Seq(aapajoenKoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[KotikuntalaskelmaOppijaRow])

  private def uudetOppijatRivit = kotikuntalaskelmaBuilder
    .buildOppijat(Seq(aapajoenKoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[KotikuntalaskelmaOppijaRow])

  private def perusopetuksenOpiskeluoikeus(oppija: LaajatOppijaHenkilöTiedot): PerusopetuksenOpiskeluoikeus =
    getOpiskeluoikeudet(oppija.oid).collect { case oo: PerusopetuksenOpiskeluoikeus => oo }.head

  private def vuosiluokka(luokkaAste: Int, luokka: String, alkamispäivä: LocalDate, vahvistuspäivä: Option[LocalDate] = None) =
    PerusopetuksenVuosiluokanSuoritus(
      koulutusmoduuli = PerusopetuksenLuokkaAste(luokkaAste, PerusopetusExampleData.perusopetuksenDiaarinumero),
      luokka = luokka,
      toimipiste = oppilaitos(aapajoenKoulu),
      suorituskieli = suomenKieli,
      alkamispäivä = Some(alkamispäivä),
      vahvistus = vahvistuspäivä.flatMap(vahvistusPaikkakunnalla(_, oppilaitos(aapajoenKoulu))),
      osasuoritukset = vahvistuspäivä.map(_ => List(
        PerusopetusExampleData.suoritus(PerusopetusExampleData.oppiaine("HI", PerusopetusExampleData.vuosiviikkotuntia(2)))
          .copy(arviointi = PerusopetusExampleData.arviointi(8))
      ))
    )

  private def korvaaSuoritukset(oppija: LaajatOppijaHenkilöTiedot)(muutos: List[PerusopetuksenPäätasonSuoritus] => List[PerusopetuksenPäätasonSuoritus]): Unit = {
    val oo = perusopetuksenOpiskeluoikeus(oppija)
    putOppija(Oppija(oppija, List(oo.copy(suoritukset = muutos(oo.suoritukset))))) {
      verifyResponseStatusOk()
    }
    reloadRaportointikanta()
  }

  private def oppijanRivi(oppija: LaajatOppijaHenkilöTiedot): KotikuntalaskelmaOppijaRow =
    uudetOppijatRivit.find(_.oppijaNumero.contains(oppija.oid)).get

  "Kunnan kuntalaiset muualla" - {
    lazy val helsinkiläiset = kotikuntalaskelmaBuilder.kunnanOppijat(Kunta.helsinki, raportointipäivä, "Esiopetus")

    "Kunnan organisaatio-oidista saadaan kuntakoodi" in {
      Kunta.validateAndGetKuntaKoodi(application.organisaatioService, application.koodistoPalvelu, helsinginKaupunki) shouldBe Right(Kunta.helsinki)
    }

    "Toisen kunnan koulussa opiskelevat kuntalaiset näkyvät opiskelupaikkoineen" in {
      val oidit = Seq(
        KoskiSpecificMockOppijat.kotikuntalaskelmaSeitsemanKaksitoista,
        KoskiSpecificMockOppijat.kotikuntalaskelmaKolmetoistaViisitoista,
        KoskiSpecificMockOppijat.kotikuntalaskelmaKuusitoistaEiErityista,
        KoskiSpecificMockOppijat.kotikuntalaskelmaEsiopetus
      ).map(_.oid)

      oidit.foreach { oid =>
        val rivi = helsinkiläiset.find(_.oppijaNumero.contains(oid))
        rivi.map(_.oppilaitos) shouldBe Some(Some("Aapajoen koulu"))
        rivi.get.kotikunta shouldBe Some("Helsinki")
      }
    }

    "Muiden kuntien asukkaat eivät näy" in {
      helsinkiläiset.find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaKuusivuotias.oid)) shouldBe None
      kotikuntalaskelmaBuilder.kunnanOppijat("851", raportointipäivä, "Esiopetus")
        .find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaSeitsemanKaksitoista.oid)) shouldBe None
    }

    "Turvakiellon alaiset kuntalaiset jäävät pois" in {
      helsinkiläiset.find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaTurvakielto.oid)) shouldBe None
      helsinkiläiset.exists(_.oppijaNumero.contains("Turvakielto")) shouldBe false
    }
  }

  "Kotikuntalaskelma" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/kotikuntalaskelma?oppilaitosOid=$aapajoenKoulu&paiva=$raportointipäivä&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessageForOperation(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=kotikuntalaskelma&oppilaitosOid=$aapajoenKoulu&paiva=$raportointipäivä&lang=fi"
            )
          )
        )
      }
    }

    "Aggregaattivälilehti - eri-ikäiset ja eri kotikunnissa asuvat oppijat päätyvät oikeisiin ikäryhmä- ja kotikuntariveihin" in {
      val jyväskyläRivi = aggregaattiRivit.find(_.oppilaanKotikunta.contains("Jyväskylä"))
      jyväskyläRivi shouldBe defined
      jyväskyläRivi.get.kuusi should be >= 1
      jyväskyläRivi.get.kuusitoistaErityisenTuenPerusteella should be >= 1

      val helsinkiRivi = aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki"))
      helsinkiRivi shouldBe defined
      helsinkiRivi.get.seitsemänKaksitoista should be >= 1
      helsinkiRivi.get.kolmetoistaViisitoista should be >= 1
      helsinkiRivi.get.kuusitoistaEiErityisenTuenPerusteella should be >= 1

      jyväskyläRivi.get.kuusitoistaEiErityisenTuenPerusteella should be(0)
      helsinkiRivi.get.kuusitoistaErityisenTuenPerusteella should be(0)
    }

    "Aggregaattivälilehti - turvakiellon alaiset oppijat lasketaan todellisiin kotikuntiinsa" in {
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki")).get.seitsemänKaksitoista should be(2)
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Jyväskylä")).get.seitsemänKaksitoista should be(1)
    }

    "Aggregaattivälilehti - hetuttomat oppijat lasketaan omalle, nimetylle rivilleen" in {
      val hetutonRivi = aggregaattiRivit.find(_.oppilaanKotikunta.contains(t.get("raportti-excel-default-value-hetuton")))

      hetutonRivi shouldBe defined
      hetutonRivi.get.kotikunnanKoodi shouldBe empty
      hetutonRivi.get.seitsemänKaksitoista should be(1)
      aggregaattiRivit.find(_.oppilaanKotikunta.isEmpty) shouldBe None
    }

    "Oppijat-välilehdellä on yksi rivi jokaista aggregaattivälilehdellä laskettua oppijaa kohden" in {
      oppijatRivit.length shouldBe aggregaattiRivit.map(_.yhteensä).sum
    }

    "Oppijat-välilehti - esiopetusoppijan luokka-asteena näytetään esiopetus eikä koulutuskoodia" in {
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaEsiopetus.oid))

      rivi.get.luokkaAste shouldBe Some(t.get("raportti-excel-default-value-esiopetus-lyhyt"))
      rivi.get.luokka shouldBe None
    }

    "Oppijat-välilehti - tavallisen oppijan tiedot näytetään sellaisenaan" in {
      val kaisanOid = KoskiSpecificMockOppijat.kotikuntalaskelmaKuusivuotias.oid
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(kaisanOid))

      rivi shouldBe defined
      rivi.get.etunimet shouldBe Some("Kaisa")
      rivi.get.sukunimi shouldBe Some("Kuusi")
      rivi.get.kotikunta shouldBe Some("Jyväskylä")
      rivi.get.oppilaitos shouldBe Some("Aapajoen koulu")
      rivi.get.luokkaAste shouldBe Some("1")
      rivi.get.kuusi shouldBe true
      rivi.get.seitsemänKaksitoista shouldBe false
      rivi.get.kolmetoistaViisitoista shouldBe false
      rivi.get.kuusitoistaErityisenTuenPerusteella shouldBe false
      rivi.get.kuusitoistaEiErityisenTuenPerusteella shouldBe false
    }

    "Raportointipäivänä kotikuntaa vaihtanut oppija lasketaan vain uuteen kotikuntaansa" in {
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Vantaa")) shouldBe None
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki")).get.kolmetoistaViisitoista should be(1)

      val kalle = oppijatRivit.find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaKolmetoistaViisitoista.oid))
      kalle.get.kotikunta shouldBe Some("Helsinki")
    }

    "Oppijat-välilehti - luokka-aste vastaa suunnilleen oppijan ikää" in {
      val kallenOid = KoskiSpecificMockOppijat.kotikuntalaskelmaKolmetoistaViisitoista.oid
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(kallenOid))

      rivi shouldBe defined
      rivi.get.luokkaAste shouldBe Some("8")
      rivi.get.luokka shouldBe Some("8A")
    }

    "Oppijat-välilehti - turvakiellon alaisen oppijan tunnistetiedot piilotetaan mutta ikäryhmälippu näkyy" in {
      val turvakieltoRivit = oppijatRivit.filter(_.oppijaNumero.contains("Turvakielto"))

      turvakieltoRivit.length should be >= 2

      turvakieltoRivit.foreach { rivi =>
        rivi.oppijaNumero shouldBe Some("Turvakielto")
        rivi.hetu shouldBe None
        rivi.yksiloity shouldBe None
        rivi.etunimet shouldBe None
        rivi.sukunimi shouldBe None
        rivi.kotikunta shouldBe None
        rivi.oppilaitos shouldBe None
        rivi.luokkaAste shouldBe None
        rivi.luokka shouldBe None
      }
      turvakieltoRivit.exists(_.seitsemänKaksitoista) shouldBe true
    }

    "Oppijat-välilehti - turvakiellon alaiset oppijat ovat listan lopussa" in {
      val ensimmäinenTurvakieltoIndeksi = oppijatRivit.indexWhere(_.oppijaNumero.contains("Turvakielto"))

      ensimmäinenTurvakieltoIndeksi should be >= 0
      oppijatRivit.drop(ensimmäinenTurvakieltoIndeksi).foreach { rivi =>
        rivi.oppijaNumero shouldBe Some("Turvakielto")
      }
    }

    "Oppijat-välilehti - hetuton oppija näkyy rivinä mutta ilman kotikuntaa" in {
      val hetutonOid = KoskiSpecificMockOppijat.kotikuntalaskelmaHetuton.oid
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(hetutonOid))

      rivi shouldBe defined
      rivi.get.etunimet shouldBe Some("Heikki-Lapsi")
      rivi.get.kotikunta shouldBe None
      rivi.get.seitsemänKaksitoista shouldBe true
    }

    "Kotiopetuksessa oleva oppija ei näy raportilla lainkaan" in {
      val kotiopetusOid = KoskiSpecificMockOppijat.kotikuntalaskelmaKotiopetus.oid

      oppijatRivit.find(_.oppijaNumero.contains(kotiopetusOid)) shouldBe None
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Jyväskylä")).get.seitsemänKaksitoista should be(1)
    }

    "Esiopetusoppija näkyy raportilla omalla, perusopetuksesta erillisellä haarallaan" in {
      val esiopetusOid = KoskiSpecificMockOppijat.kotikuntalaskelmaEsiopetus.oid
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(esiopetusOid))

      rivi shouldBe defined
      rivi.get.etunimet shouldBe Some("Elias")
      rivi.get.kotikunta shouldBe Some("Helsinki")
      rivi.get.kuusi shouldBe true
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki")).get.kuusi should be >= 1
    }

    "Kansainvälisen koulun oppija, jonka koulutusmoduuli alkoi edellisenä lukuvuotena, ei näy raportilla" in {
      val oid = KoskiSpecificMockOppijat.kotikuntalaskelmaKansainvalinenEdellinenLukuvuosi.oid

      oppijatRivit.find(_.oppijaNumero.contains(oid)) shouldBe None
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki")).get.seitsemänKaksitoista should be(2)
    }

    // Muokkaavat fixtuurin opiskeluoikeuksia, joten viimeisinä
    "Oppijat-välilehden luokkatiedot valitaan samalta, raportointipäivänä ajankohtaiselta suoritukselta" - {
      "Vuosiluokan rinnalla oleva perusopetuksen oppimäärä ei näy luokka-asteena" in {
        val kaisa = KoskiSpecificMockOppijat.kotikuntalaskelmaKuusivuotias
        korvaaSuoritukset(kaisa)(PerusopetusExampleData.perusopetuksenOppimääränSuoritusKesken.copy(toimipiste = oppilaitos(aapajoenKoulu)) :: _)

        val rivi = oppijanRivi(kaisa)
        rivi.luokkaAste shouldBe Some("1")
        rivi.luokka shouldBe Some("1A")
      }

      "Raportointipäivän jälkeen alkava vuosiluokka ei ole vielä ajankohtainen" in {
        val kalle = KoskiSpecificMockOppijat.kotikuntalaskelmaKolmetoistaViisitoista
        korvaaSuoritukset(kalle)(_ => List(
          vuosiluokka(8, "8A", date(2025, 8, 1), vahvistuspäivä = Some(date(2026, 5, 30))),
          vuosiluokka(9, "9A", raportointipäivä.plusDays(14))
        ))

        val rivi = oppijanRivi(kalle)
        rivi.luokkaAste shouldBe Some("8")
        rivi.luokka shouldBe Some("8A")
      }

      "Luokka otetaan samalta vuosiluokalta kuin luokka-aste" in {
        val ilmari = KoskiSpecificMockOppijat.kotikuntalaskelmaKuusitoistaEiErityista
        korvaaSuoritukset(ilmari)(_ => List(
          vuosiluokka(8, "Sininen", date(2022, 8, 1), vahvistuspäivä = Some(date(2023, 5, 30))),
          vuosiluokka(9, "9A", date(2023, 8, 1))
        ))

        val rivi = oppijanRivi(ilmari)
        rivi.luokkaAste shouldBe Some("9")
        rivi.luokka shouldBe Some("9A")
      }

      "Raportointipäivänä koulua vaihtanut oppija näytetään läsnä-tilaisen opiskeluoikeutensa tiedoilla" in {
        val sami = KoskiSpecificMockOppijat.kotikuntalaskelmaSeitsemanKaksitoista
        val vanha = perusopetuksenOpiskeluoikeus(sami)
        val eronnut = vanha.copy(
          suoritukset = List(vuosiluokka(3, "3B", date(2022, 8, 1))),
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(date(2022, 8, 1), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(raportointipäivä, opiskeluoikeusEronnut)
          ))
        )
        val uusi = PerusopetuksenOpiskeluoikeus(
          oppilaitos = Some(oppilaitos(jyväskylänNormaalikoulu)),
          suoritukset = List(vuosiluokka(3, "3A", raportointipäivä).copy(toimipiste = oppilaitos(jyväskylänNormaalikoulu))),
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(raportointipäivä, opiskeluoikeusLäsnä)))
        )
        putOppija(Oppija(sami, List(eronnut, uusi))) {
          verifyResponseStatusOk()
        }
        reloadRaportointikanta()

        val rivi = kotikuntalaskelmaBuilder
          .buildOppijat(Seq(aapajoenKoulu, jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
          .rows.map(_.asInstanceOf[KotikuntalaskelmaOppijaRow])
          .find(_.oppijaNumero.contains(sami.oid)).get
        rivi.oppilaitos shouldBe Some("Jyväskylän normaalikoulu")
        rivi.luokkaAste shouldBe Some("3")
        rivi.luokka shouldBe Some("3A")
      }
    }
  }
}
