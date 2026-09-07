package fi.oph.koski.raportit

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.aapajoenKoulu
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

// TOR-2650: Perustuu KoskiSpecificMockOppijat.scala:n kotikuntalaskelma*-testioppijoihin, jotka
// ovat kaikki Aapajoen koulussa (valittu tarkoituksella Jyväskylän normaalikoulun sijaan — ks.
// perustelu KoskiSpecificDatabaseFixtureCreator.scala:n kommentista). Nämä fixturet on
// tietoisesti pidetty committoimattomina (ks. suunnitelman 13.2 §, kohta 9) — testit siis
// nojaavat tällä hetkellä paikallisesti lisättyyn, ei vielä committoituun dataan.
class KotikuntalaskelmaSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with DirtiesFixtures {
  // Kaikki kotikuntalaskelma-testioppijat on syntymäajoitettu niin että ikäryhmä ratkeaa
  // yksikäsitteisesti minä tahansa vuoden 2026 päivänä (ikäryhmäjako on kalenterivuosipohjainen,
  // ei päivätarkka), joten mikä tahansa 2026-päivä kelpaa raportointipäiväksi.
  private val raportointipäivä = date(2026, 9, 1)

  override protected def alterFixture(): Unit = {
    reloadRaportointikanta()
  }

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  private val kotikuntalaskelmaBuilder = Kotikuntalaskelma(application.raportointiDatabase.db, application.organisaatioService)

  private lazy val aggregaattiRivit = kotikuntalaskelmaBuilder
    .build(Seq(aapajoenKoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[KotikuntalaskelmaRow])

  private lazy val oppijatRivit = kotikuntalaskelmaBuilder
    .buildOppijat(Seq(aapajoenKoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[KotikuntalaskelmaOppijaRow])

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
      // Kuusi Kaisa (6v, Jyväskylä) ja KuusitoistaErityinen Essi (16v, erityisen tuen perusteella,
      // Jyväskylä) ovat molemmat samalla kotikuntarivillä.
      val jyväskyläRivi = aggregaattiRivit.find(_.oppilaanKotikunta.contains("Jyväskylä"))
      jyväskyläRivi shouldBe defined
      jyväskyläRivi.get.kuusi should be >= 1
      jyväskyläRivi.get.kuusitoistaErityisenTuenPerusteella should be >= 1

      // SeitsemanKaksitoista Sami (9v), KolmetoistaViisitoista Kalle (14v) ja
      // KuusitoistaEiErityista Ilmari (16v, ei erityisen tuen perusteella) ovat kaikki Helsingissä.
      val helsinkiRivi = aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki"))
      helsinkiRivi shouldBe defined
      helsinkiRivi.get.seitsemänKaksitoista should be >= 1
      helsinkiRivi.get.kolmetoistaViisitoista should be >= 1
      helsinkiRivi.get.kuusitoistaEiErityisenTuenPerusteella should be >= 1

      // Sama ikäryhmä ei saa näkyä väärällä kotikuntarivillä: Jyväskylän rivillä ei pidä olla
      // "ei erityisen tuen perusteella" -tapauksia eikä Helsingin rivillä erityisen tuen tapauksia,
      // koska nämä kaksi 16-vuotiasta testioppijaa on tarkoituksella sijoitettu eri kotikuntiin.
      jyväskyläRivi.get.kuusitoistaEiErityisenTuenPerusteella should be(0)
      helsinkiRivi.get.kuusitoistaErityisenTuenPerusteella should be(0)
    }

    "Aggregaattivälilehti - turvakiellon alaiset ja hetuttomat oppijat eivät paljasta kotikuntaansa" in {
      // Hetuton Heikki-Lapsi (11v), Turvakielto Lapsi (10v, oikea kotikunta Helsinki) ja
      // Turvakielto Toinen-Lapsi (7v, oikea kotikunta Jyväskylä) osuvat kaikki samaan
      // ikäryhmään (7-12v) mutta eivät saa näkyä minkään nimetyn kotikunnan rivillä.
      val tyhjäKotikuntaRivi = aggregaattiRivit.find(_.oppilaanKotikunta.isEmpty)
      tyhjäKotikuntaRivi shouldBe defined
      tyhjäKotikuntaRivi.get.kotikunnanKoodi shouldBe empty
      tyhjäKotikuntaRivi.get.seitsemänKaksitoista should be >= 3

      // Turvakielto Lapsen oikea kotikunta (Helsinki) ei saa vuotaa Helsingin riville: sillä
      // rivillä pitäisi olla vain SeitsemanKaksitoista Sami (yksi oppija), ei kahta.
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki")).get.seitsemänKaksitoista should be(1)
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

    "Oppijat-välilehti - luokka-aste vastaa suunnilleen oppijan ikää" in {
      val kallenOid = KoskiSpecificMockOppijat.kotikuntalaskelmaKolmetoistaViisitoista.oid
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(kallenOid))

      rivi shouldBe defined
      rivi.get.luokkaAste shouldBe Some("8")
      rivi.get.luokka shouldBe Some("8A")
    }

    "Oppijat-välilehti - turvakiellon alaisen oppijan tunnistetiedot piilotetaan mutta ikäryhmälippu näkyy" in {
      val turvakieltoRivit = oppijatRivit.filter(_.oppijaNumero.contains("Turvakielto"))

      // Vähintään kotikuntalaskelmaTurvakielto ja kotikuntalaskelmaTurvakielto2 pitäisi löytyä.
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

      // Ikäryhmälippu sen sijaan näkyy normaalisti myös turvakiellon alaiselle oppijalle, jotta
      // koulutustoimija näkee mistä aggregaattivälilehden luku tulee (ks. suunnitelman 10.1 §).
      turvakieltoRivit.exists(_.seitsemänKaksitoista) shouldBe true
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

      // Muutoin tämä oppija (10v, Jyväskylä) osuisi Jyväskylän rivin 7-12v-ikäryhmään — sen
      // pitäisi silti pysyä poissa "not aj.kotiopetus" -ehdon takia.
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Jyväskylä")).get.seitsemänKaksitoista should be(0)
    }

    "Esiopetusoppija näkyy raportilla omalla, perusopetuksesta erillisellä haarallaan" in {
      val esiopetusOid = KoskiSpecificMockOppijat.kotikuntalaskelmaEsiopetus.oid
      val rivi = oppijatRivit.find(_.oppijaNumero.contains(esiopetusOid))

      rivi shouldBe defined
      rivi.get.etunimet shouldBe Some("Elias")
      rivi.get.kotikunta shouldBe Some("Helsinki")
      rivi.get.kuusi shouldBe true

      // Aggregaattivälilehdellä esiopetusoppija näkyy Helsingin rivillä kuusivuotiaana, erillään
      // Kuusi Kaisasta (joka on Jyväskylässä).
      aggregaattiRivit.find(_.oppilaanKotikunta.contains("Helsinki")).get.kuusi should be >= 1
    }
  }
}
