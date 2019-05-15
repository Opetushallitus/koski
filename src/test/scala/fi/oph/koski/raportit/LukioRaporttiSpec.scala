package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.MockOppijat._
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import fi.oph.koski.schema.{Aikajakso, Organisaatio}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class LukioRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with OpiskeluoikeusTestMethods {

  override def beforeAll(): Unit = {
    resetFixtures
    loadRaportointikantaFixtures
  }

  lazy val repository = LukioRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)

  "Lukion suoritustietoraportti" - {

    "Raportti näyttää oikealta" - {
      lazy val rowsWithColumns = buildLukioraportti(jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1))
      "Oppimäärän suoritus" in {
        verifyOppijanRow(lukiolainen, expectedYlioppilasRow, rowsWithColumns)
      }
      "Oppiaineiden suoritus" in {
        verifyOppijanRows(lukionAineopiskelijaAktiivinen, Seq(expectedAineopiskelijaHistoriaRow, expectedAineopiskelijaKemiaRow, expectedAineopiskelijaFilosofiaRow), rowsWithColumns)
      }
    }

    "Opiskeluoikeus aikajaksojen siivous" - {
      import fi.oph.koski.raportit.LukioRaportti.removeContinuousSameTila
      "Jatkuva sama tila näytetään yhtenä tilana" in {
        removeContinuousSameTila(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-02-02"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-02"), "loma", Date.valueOf("2016-02-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-02-02"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-02-02"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-02-02"), "loma", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-02-02"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-02-02"), "lasna", Date.valueOf("2016-01-01")))
        ).map(_.tila) should equal(Seq("lasna", "loma", "lasna", "loma", "lasna"))
      }
    }

    "Päivien lukumäärän laskenta jaksosta" - {
      import fi.oph.koski.raportit.LukioRaportti.lengthInDaysInDateRange
      "Alkaa ja päättyy ennen hakuväliä" in {
        lengthInDaysInDateRange(Aikajakso(date(2012, 1, 1), Some(date(2014, 1, 1))), date(2014, 1, 2), date(2018, 1, 1)) shouldEqual (0)
      }
      "Alkaa hakuvälin jälkeen" in {
        lengthInDaysInDateRange(Aikajakso(date(2018, 1, 2), Some(date(2020, 1, 1))), date(2010, 1, 2), date(2018, 1, 1)) shouldEqual (0)
      }
      "Alkanut ennen hakuväliä ja jatkuu hakuvälin yli" in {
        lengthInDaysInDateRange(Aikajakso(date(2012, 1, 1), Some(date(2016, 1, 1))), date(2013, 1, 1), date(2015, 1, 1)) shouldEqual (731)
      }
      "Alkanut ennen hakuväliä ja jaksolle ei ole merkattu päättymistä" in {
        lengthInDaysInDateRange(Aikajakso(date(2012, 1, 2), None), date(2013, 1, 1), date(2014, 1, 1)) shouldEqual (366)
      }
      "Alkanut ennen hakuväliä ja päättyy hakuvälillä" in {
        lengthInDaysInDateRange(Aikajakso(date(2012, 1, 1), Some(date(2014, 1, 1))), date(2013, 1, 1), date(2018, 1, 1)) shouldEqual (366)
      }
      "Alkanut hakuvälillä ja päättyy hakuvälillä" in {
        lengthInDaysInDateRange(Aikajakso(date(2011, 1, 1), Some(date(2012, 1, 1))), date(2010, 6, 6), date(2013, 1, 1)) shouldEqual (366)
      }
    }
  }

  lazy val oid = "123"

  lazy val expectedYlioppilasRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Oppijan oid" -> s"${lukiolainen.oid}",
    "Opiskeluoikeuden viimeisin tila" -> Some("valmistunut"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Suorituksen tyyppi" -> "lukionoppimaara",
    "Suorituksen tila" -> "valmis",
    "Suorituksen alkamispäivä" -> None,
    "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 8)),
    "Läsnäolopäiviä aikajakson aikana" -> 1218,
    "Rahoitukset" -> "",
    "Ryhmä" -> Some("12A"),
    "Pidennetty Päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Yksityisopiskelija" -> false,
    "Oikeus maksuttomaan asuntolapaikkaan" -> true,
    "Ulkomaanajaksot" -> Some(366),
    "Erityisen koulutustehtävän jaksot" -> Some(1),
    "Sisäoppilaitosmainen majoitus" -> Some(366),
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> Some(lukiolainen.sukunimi),
    "Etunimet" -> Some(lukiolainen.etunimet),
    "Yhteislaajuus" -> 89.5,
    "Suomen kieli ja kirjallisuus (AI) valtakunnallinen" -> "Arvosana 9, 8 kurssia",
    "englanti (A1) valtakunnallinen" -> "Arvosana 9, 9 kurssia",
    "ruotsi (B1) valtakunnallinen" -> "Arvosana 7, 5 kurssia",
    "latina (B3) valtakunnallinen" -> "Arvosana 9, 2 kurssia",
    "Matematiikka, pitkä oppimäärä (MA) valtakunnallinen" -> "Arvosana 9, 15 kurssia",
    "Biologia (BI) valtakunnallinen" -> "Arvosana 9, 8 kurssia",
    "Maantieto (GE) valtakunnallinen" -> "Arvosana 8, 2 kurssia",
    "Fysiikka (FY) valtakunnallinen" -> "Arvosana 8, 13 kurssia",
    "Kemia (KE) valtakunnallinen" -> "Arvosana 8, 8 kurssia",
    "Uskonto/Elämänkatsomustieto (KT) valtakunnallinen" -> "Arvosana 8, 3 kurssia",
    "Filosofia (FI) valtakunnallinen" -> "Arvosana 8, 1 kurssi",
    "Psykologia (PS) valtakunnallinen" -> "Arvosana 9, 1 kurssi",
    "Historia (HI) valtakunnallinen" -> "Arvosana 7, 4 kurssia",
    "Yhteiskuntaoppi (YH) valtakunnallinen" -> "Arvosana 8, 2 kurssia",
    "Liikunta (LI) valtakunnallinen" -> "Arvosana 9, 3 kurssia",
    "Musiikki (MU) valtakunnallinen" -> "Arvosana 8, 1 kurssi",
    "Kuvataide (KU) valtakunnallinen" -> "Arvosana 9, 2 kurssia",
    "Terveystieto (TE) valtakunnallinen" -> "Arvosana 9, 1 kurssi",
    "Tanssi ja liike (ITT) paikallinen" -> "Arvosana 10, 1 kurssi",
    "Teemaopinnot (TO) valtakunnallinen" -> "Arvosana S, 1 kurssi",
    "Oman äidinkielen opinnot (OA) valtakunnallinen" -> "Arvosana S, 1 kurssi"
  )

  lazy val defaultExpectedAineopiskelijaRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Oppijan oid" -> lukionAineopiskelijaAktiivinen.oid,
    "Opiskeluoikeuden viimeisin tila" -> Some("lasna"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Suorituksen tyyppi" -> "lukionoppiaineenoppimaara",
    "Suorituksen tila" -> "valmis",
    "Suorituksen alkamispäivä" -> None,
    "Suorituksen vahvistuspäivä" -> None,
    "Läsnäolopäiviä aikajakson aikana" -> 123,
    "Rahoitukset" -> "",
    "Ryhmä" -> None,
    "Pidennetty Päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Yksityisopiskelija" -> false,
    "Oikeus maksuttomaan asuntolapaikkaan" -> false,
    "Ulkomaanajaksot" -> None,
    "Erityisen koulutustehtävän jaksot" -> None,
    "Sisäoppilaitosmainen majoitus" -> None,
    "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
    "Sukunimi" -> Some(lukionAineopiskelijaAktiivinen.sukunimi),
    "Etunimet" -> Some(lukionAineopiskelijaAktiivinen.etunimet),
    "Suomen kieli ja kirjallisuus (AI) valtakunnallinen" -> "",
    "englanti (A1) valtakunnallinen" -> "",
    "ruotsi (B1) valtakunnallinen" -> "",
    "latina (B3) valtakunnallinen" -> "",
    "Matematiikka, pitkä oppimäärä (MA) valtakunnallinen" -> "",
    "Biologia (BI) valtakunnallinen" -> "",
    "Maantieto (GE) valtakunnallinen" -> "",
    "Fysiikka (FY) valtakunnallinen" -> "",
    "Kemia (KE) valtakunnallinen" -> "",
    "Uskonto/Elämänkatsomustieto (KT) valtakunnallinen" -> "",
    "Filosofia (FI) valtakunnallinen" -> "",
    "Psykologia (PS) valtakunnallinen" -> "",
    "Historia (HI) valtakunnallinen" -> "",
    "Yhteiskuntaoppi (YH) valtakunnallinen" -> "",
    "Liikunta (LI) valtakunnallinen" -> "",
    "Musiikki (MU) valtakunnallinen" -> "",
    "Kuvataide (KU) valtakunnallinen" -> "",
    "Terveystieto (TE) valtakunnallinen" -> "",
    "Tanssi ja liike (ITT) paikallinen" -> "",
    "Teemaopinnot (TO) valtakunnallinen" -> "",
    "Oman äidinkielen opinnot (OA) valtakunnallinen" -> ""
  )

  lazy val expectedAineopiskelijaHistoriaRow = defaultExpectedAineopiskelijaRow + (
    "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
    "Yhteislaajuus" -> 4.0,
    "Historia (HI) valtakunnallinen" -> "Arvosana 9, 4 kurssia"
  )

  lazy val expectedAineopiskelijaKemiaRow = defaultExpectedAineopiskelijaRow + (
    "Suorituksen vahvistuspäivä" -> Some(date(2015, 1, 10)),
    "Yhteislaajuus" -> 1.0,
    "Kemia (KE) valtakunnallinen" -> "Arvosana 8, 1 kurssi"
  )

  lazy val expectedAineopiskelijaFilosofiaRow = defaultExpectedAineopiskelijaRow + (
    "Suorituksen tila" -> "kesken",
    "Suorituksen vahvistuspäivä" -> None,
    "Yhteislaajuus" -> 1.0,
    "Filosofia (FI) valtakunnallinen" -> "Arvosana 9, 1 kurssi"
  )

  private def buildLukioraportti(organisaatioOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate) = {
    val sheet = LukioRaportti.buildRaportti(repository, organisaatioOid, alku, loppu)
    zipRowsWithColumTitles(sheet)
  }

  private def zipRowsWithColumTitles(sheet: DynamicDataSheet) = {
    sheet.rows.map(_.zip(sheet.columnSettings)).map(_.map { case (data, column) => column.title -> data }.toMap)
  }

  private def verifyOppijanRow(oppija: OppijaHenkilö, expected: Map[String, Any], all: Seq[Map[String, Any]]) = {
    val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
    opiskeluoikeudenOid shouldBe defined
    findFirstByOid(oppija.oid, all) should be(expected + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get))
  }

  private def verifyOppijanRows(oppija: OppijaHenkilö, expected: Seq[Map[String, Any]], all: Seq[Map[String, Any]]) = {
    val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
    findByOid(oppija.oid, all).toSet should equal(expected.map(_ + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get)).toSet)
  }

  private def findFirstByOid(oid: String, maps: Seq[Map[String, Any]]) = {
    val found = findByOid(oid, maps)
    found.length shouldBe (1)
    found.head
  }

  private def findByOid(oid: String, maps: Seq[Map[String, Any]]) = maps.filter(_.get("Oppijan oid").exists(_ == oid))
}
