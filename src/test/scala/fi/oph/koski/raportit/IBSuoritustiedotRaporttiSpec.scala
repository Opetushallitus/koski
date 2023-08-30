package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{ibPreIB2019, ibPredicted}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.ressunLukio
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => localDate}

class IBSuoritustiedotRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with BeforeAndAfterAll {
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private val application = KoskiApplicationForTests
  private val raporttiRepository = IBSuoritustiedotRaporttiRepository(application.raportointiDatabase.db)
  private val raporttiBuilder = IBSuoritustiedotRaportti(raporttiRepository, t)

  private lazy val raporttiRivitIB = raporttiBuilder.build(
      ressunLukio,
      localDate(2012, 1, 1),
      localDate(2022, 1, 1),
      false,
      IBTutkinnonSuoritusRaportti
    )(session(defaultUser)).map(sheet => zipRowsWithColumTitles(sheet))

  private lazy val raporttiRivitPreIB = raporttiBuilder.build(
      ressunLukio,
      localDate(2012, 1, 1),
      localDate(2022, 1, 1),
      false,
      PreIBSuoritusRaportti
    )(session(defaultUser)).map(sheet => zipRowsWithColumTitles(sheet))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "IB suoritustiedot raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/ibsuoritustietojentarkistus?oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="ib_suoritustiedot_ib-tutkinto_${ressunLukio}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=ibsuoritustietojentarkistus&oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/ibsuoritustietojentarkistus?oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="ib_prestationsuppgifter_ib-examen_${ressunLukio}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=ibsuoritustietojentarkistus&oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=sv")))
      }
    }

    "Raporttia ei voi ladata väärin muodostuneella hakuparametrilla" in {
      authGet(s"api/raportit/ibsuoritustietojentarkistus?oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=virheellinenTyyppi&osasuoritustenAikarajaus=false&lang=sv&password=salasana") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam(s"Raportin lataaminen epäonnistui, tuntematon raportin tyyppi virheellinenTyyppi"))
      }
    }

    "Raportilla olevat rivit näyttävät tiedot oikein" - {

      "suoritustiedot sheetillä IB-tutkinnon suorituksille" in {
        val suoritusTiedotSheet = raporttiRivitIB.head
        suoritusTiedotSheet.size shouldBe 3

        lazy val expectedPetteri = Map(
          "Opiskeluoikeuden oid" -> "",
          "Lähdejärjestelmä" -> None,
          "Koulutustoimijan nimi" -> "Helsingin kaupunki",
          "Oppilaitoksen nimi" -> "Ressun lukio",
          "Toimipisteen nimi" -> "Ressun lukio",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
          "Päivitetty" -> LocalDate.now,
          "Yksilöity" -> true,
          "Oppijan oid" -> ibPredicted.oid,
          "hetu" -> ibPredicted.hetu,
          "Sukunimi" -> ibPredicted.sukunimi,
          "Etunimet" -> ibPredicted.etunimet,
          "Opiskeluoikeuden alkamispäivä" -> Some(LocalDate.of(2012, 9, 1)),
          "Viimeisin opiskeluoikeuden tila" -> Some("lasna"),
          "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
          "Päätason suoritusten nimet" -> Some("IB-tutkinto (International Baccalaureate)"),
          "Opiskeluoikeuden päättymispäivä" -> None,
          "Rahoitukset" -> "1",
          "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
          "Maksuttomuus" -> None,
          "Oikeutta maksuttomuuteen pidennetty" -> None,
          "Pidennetty päättymispäivä" -> false,
          "Ulkomainen vaihto-opiskelija" -> false,
          "Erityisen koulutustehtävän jaksot" -> None,
          "Ulkomaanjaksot" -> None,
          "Sisäoppilaitosmainen majoitus" -> None,
          "Yhteislaajuus (kaikki kurssit)" -> 45.0,
          "Yhteislaajuus (suoritetut kurssit)" -> 45.0,
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 2.0,
          "Yhteislaajuus (tunnustetut kurssit)" -> 0,
          "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0,
          "A2 Englanti valtakunnallinen" -> "Arvosana 6, 6.0 kurssia",
          "BIO Biology valtakunnallinen" -> "Arvosana 5, 9.0 kurssia (joista 1.0 hylättyjä)",
          "HIS History valtakunnallinen" -> "Arvosana 6, 6.0 kurssia (joista 1.0 hylättyjä)",
          "MATST Mathematical studies valtakunnallinen" -> "Arvosana 5, 6.0 kurssia",
          "PSY Psychology valtakunnallinen" -> "Arvosana 7, 9.0 kurssia",
          "A Suomi valtakunnallinen" -> "Arvosana 4, 9.0 kurssia",
        )

        verifyOppijanRow(ibPredicted, expectedPetteri, suoritusTiedotSheet)
      }

      "suoritustiedot sheetillä Pre-IB suorituksille" in {
        val suoritusTiedotSheet = raporttiRivitPreIB.head
        suoritusTiedotSheet.size shouldBe 5

        lazy val expectedPetteri = Map(
          "Opiskeluoikeuden oid" -> "",
          "Lähdejärjestelmä" -> None,
          "Koulutustoimijan nimi" -> "Helsingin kaupunki",
          "Oppilaitoksen nimi" -> "Ressun lukio",
          "Toimipisteen nimi" -> "Ressun lukio",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
          "Päivitetty" -> LocalDate.now,
          "Yksilöity" -> true,
          "Oppijan oid" -> ibPredicted.oid,
          "hetu" -> ibPredicted.hetu,
          "Sukunimi" -> ibPredicted.sukunimi,
          "Etunimet" -> ibPredicted.etunimet,
          "Opiskeluoikeuden alkamispäivä" -> Some(LocalDate.of(2012, 9, 1)),
          "Viimeisin opiskeluoikeuden tila" -> Some("lasna"),
          "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
          "Päätason suoritusten nimet" -> Some("Pre-IB"),
          "Opiskeluoikeuden päättymispäivä" -> None,
          "Rahoitukset" -> "1",
          "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
          "Maksuttomuus" -> None,
          "Oikeutta maksuttomuuteen pidennetty" -> None,
          "Pidennetty päättymispäivä" -> false,
          "Ulkomainen vaihto-opiskelija" -> false,
          "Erityisen koulutustehtävän jaksot" -> None,
          "Ulkomaanjaksot" -> None,
          "Sisäoppilaitosmainen majoitus" -> None,
          "Yhteislaajuus (kaikki kurssit/opintopisteet)" -> 32.0,
          "Yhteislaajuus (suoritetut kurssit/opintopisteet)" -> 32.0,
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit/opintopisteet)" -> 0,
          "Yhteislaajuus (tunnustetut kurssit/opintopisteet)" -> 0,
          "Yhteislaajuus (eri vuonna korotetut kurssit/opintopisteet)" -> 0,
          "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 8, 3.0 kurssia",
          "A1 Englanti valtakunnallinen" -> "Arvosana 10, 3.0 kurssia",
          "B1 Ruotsi valtakunnallinen" -> "Arvosana 7, 2.0 kurssia",
          "B2 Ranska valtakunnallinen" -> "Arvosana 9, 1.0 kurssia",
          "B3 Espanja valtakunnallinen" -> "Arvosana 6, 1.0 kurssia",
          "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 7, 4.0 kurssia",
          "BI Biologia valtakunnallinen" -> "Arvosana 8, 2.0 kurssia",
          "GE Maantieto valtakunnallinen" -> "Arvosana 10, 1.0 kurssia",
          "FY Fysiikka valtakunnallinen" -> "Arvosana 7, 1.0 kurssia",
          "KE Kemia valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
          "FI Filosofia valtakunnallinen" -> "Arvosana 7, 1.0 kurssia",
          "PS Psykologia valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
          "HI Historia valtakunnallinen" -> "Arvosana 8, 3.0 kurssia",
          "YH Yhteiskuntaoppi valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
          "KT Katolinen uskonto valtakunnallinen" -> "",
          "KT Uskonto/Elämänkatsomustieto valtakunnallinen" -> "Arvosana 10, 1.0 kurssia",
          "TE Terveystieto valtakunnallinen" -> "Arvosana 7, 1.0 kurssia",
          "LI Liikunta valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
          "MU Musiikki valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
          "KU Kuvataide valtakunnallinen" -> "Arvosana 9, 1.0 kurssia",
          "OP Opinto-ohjaus valtakunnallinen" -> "Arvosana 7, 1.0 kurssia",
          "TO Teemaopinnot valtakunnallinen" -> "Arvosana S, 1.0 kurssia",
          "LD Lukiodiplomit valtakunnallinen" -> "",
          "MS Muut suoritukset valtakunnallinen" -> "",
          "A Englanti valtakunnallinen" -> "",
          "A Espanja valtakunnallinen" -> "",
          "ITT Tanssi ja liike paikallinen" -> ""
        )

        verifyOppijanRow(ibPredicted, expectedPetteri, suoritusTiedotSheet)
      }

      "suoritustiedot sheetillä Pre-IB 2019 suorituksille" in {
        val suoritusTiedotSheet = raporttiRivitPreIB.head
        suoritusTiedotSheet.size shouldBe 5

        lazy val expectedPate = Map(
          "Opiskeluoikeuden oid" -> "",
          "Lähdejärjestelmä" -> None,
          "Koulutustoimijan nimi" -> "Helsingin kaupunki",
          "Oppilaitoksen nimi" -> "Ressun lukio",
          "Toimipisteen nimi" -> "Ressun lukio",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
          "Päivitetty" -> LocalDate.now,
          "Yksilöity" -> true,
          "Oppijan oid" -> ibPreIB2019.oid,
          "hetu" -> ibPreIB2019.hetu,
          "Sukunimi" -> ibPreIB2019.sukunimi,
          "Etunimet" -> ibPreIB2019.etunimet,
          "Opiskeluoikeuden alkamispäivä" -> Some(LocalDate.of(2012, 9, 1)),
          "Viimeisin opiskeluoikeuden tila" -> Some("valmistunut"),
          "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna, valmistunut",
          "Päätason suoritusten nimet" -> Some("Pre-IB 2019"),
          "Opiskeluoikeuden päättymispäivä" -> Some(LocalDate.of(2016, 6, 4)),
          "Rahoitukset" -> "1, 1",
          "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
          "Maksuttomuus" -> None,
          "Oikeutta maksuttomuuteen pidennetty" -> None,
          "Pidennetty päättymispäivä" -> false,
          "Ulkomainen vaihto-opiskelija" -> false,
          "Erityisen koulutustehtävän jaksot" -> None,
          "Ulkomaanjaksot" -> None,
          "Sisäoppilaitosmainen majoitus" -> None,
          "Yhteislaajuus (kaikki kurssit/opintopisteet)" -> 34.0,
          "Yhteislaajuus (suoritetut kurssit/opintopisteet)" -> 32.0,
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit/opintopisteet)" -> 0,
          "Yhteislaajuus (tunnustetut kurssit/opintopisteet)" -> 2.0,
          "Yhteislaajuus (eri vuonna korotetut kurssit/opintopisteet)" -> 0,
          "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 4.0 opintopistettä",
          "A1 Englanti valtakunnallinen" -> "",
          "B1 Ruotsi valtakunnallinen" -> "",
          "B2 Ranska valtakunnallinen" -> "",
          "B3 Espanja valtakunnallinen" -> "",
          "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 10, 4.0 opintopistettä",
          "BI Biologia valtakunnallinen" -> "",
          "GE Maantieto valtakunnallinen" -> "",
          "FY Fysiikka valtakunnallinen" -> "Arvosana 8, 0.0 kurssia",
          "KE Kemia valtakunnallinen" -> "Arvosana 7, 2.0 opintopistettä",
          "FI Filosofia valtakunnallinen" -> "",
          "PS Psykologia valtakunnallinen" -> "",
          "HI Historia valtakunnallinen" -> "",
          "YH Yhteiskuntaoppi valtakunnallinen" -> "",
          "KT Katolinen uskonto valtakunnallinen" -> "Arvosana 9, 2.0 opintopistettä",
          "KT Uskonto/Elämänkatsomustieto valtakunnallinen" -> "",
          "TE Terveystieto valtakunnallinen" -> "",
          "LI Liikunta valtakunnallinen" -> "Arvosana 8, 3.0 opintopistettä",
          "MU Musiikki valtakunnallinen" -> "",
          "KU Kuvataide valtakunnallinen" -> "",
          "OP Opinto-ohjaus valtakunnallinen" -> "",
          "TO Teemaopinnot valtakunnallinen" -> "Ei arvosanaa",
          "LD Lukiodiplomit valtakunnallinen" -> "Ei arvosanaa",
          "MS Muut suoritukset valtakunnallinen" -> "Ei arvosanaa",
          "A Englanti valtakunnallinen" -> "Arvosana 9, 4.0 opintopistettä",
          "A Espanja valtakunnallinen" -> "Arvosana 6, 4.0 opintopistettä",
          "ITT Tanssi ja liike paikallinen" -> "Arvosana 6, 2.0 opintopistettä"
        )

        verifyOppijanRow(ibPreIB2019, expectedPate, suoritusTiedotSheet)
      }
    }
  }

  private def zipRowsWithColumTitles(sheet: DynamicDataSheet) = {
    sheet.rows.map(_.zip(sheet.columnSettings)).map(_.map { case (data, column) => column.title -> data }.toMap)
  }

  private def verifyOppijanRow(oppija: LaajatOppijaHenkilöTiedot, expected: Map[String, Any], all: Seq[Map[String, Any]], addOpiskeluoikeudenOid: Boolean = true) = {
    val expectedResult = if (addOpiskeluoikeudenOid) {
      val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
      opiskeluoikeudenOid shouldBe defined
      expected + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get)
    } else {
      expected
    }

    findFirstByOid(oppija.oid, all) should be(expectedResult)
  }

  private def findFirstByOid(oid: String, maps: Seq[Map[String, Any]]) = {
    val found = findByOid(oid, maps)
    found.length shouldBe (1)
    found.head
  }

  private def findByOid(oid: String, maps: Seq[Map[String, Any]]) = maps.filter(_.get("Oppijan oid").exists(_ == oid))

  private def session(user: KoskiMockUser): KoskiSpecificSession= user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
