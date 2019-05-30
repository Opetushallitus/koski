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
      lazy val titleAndRowsWithColumns = buildLukioraportti(jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1))
      "Oppiaineita tai kursseja ei päädy duplikaattina raportille" in {
        val sheets = LukioRaportti.buildRaportti(repository, jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1))
        verifyNoDuplicates(sheets.map(_.title))
        sheets.map(_.columnSettings.map(_.title)).foreach(verifyNoDuplicates)
      }
      "Oppiaine tason välilehti" - {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.head
        "On ensimmäinen" in {
          title should equal("Oppiaineet ja lisätiedot")
        }
        "Oppimäärän suoritus" in {
          verifyOppijanRow(lukiolainen, expectedLukiolainenRow, oppiaineetRowsWithColumns)
        }
        "Oppiaineiden suoritus" in {
          verifyOppijanRows(lukionAineopiskelijaAktiivinen, Seq(expectedAineopiskelijaHistoriaRow, expectedAineopiskelijaKemiaRow, expectedAineopiskelijaFilosofiaRow), oppiaineetRowsWithColumns)
        }
      }
      "Kurssit tason välilehdet" - {
        lazy val kurssit = titleAndRowsWithColumns.tail
        "Välilehtien nimet, sisältää oppiaineet aakkosjärjestyksessä titlen mukaan" in {
          val kurssiVälilehtienTitlet = kurssit.map { case (title, _) => title }

          kurssiVälilehtienTitlet should equal(Seq(
            "A1 v englanti", "AI v Suomen kieli ja kirjallisuus", "B1 v ruotsi", "B3 v latina",
            "BI v Biologia", "FI v Filosofia", "FY v Fysiikka", "GE v Maantieto", "HI v Historia",
            "ITT p Tanssi ja liike", "KE v Kemia", "KT v Islam", "KU v Kuvataide", "LI v Liikunta",
            "MA v Matematiikka pitkä oppimäärä", "MU v Musiikki", "OA v Oman äidinkielen opinnot",
            "PS v Psykologia", "TE v Terveystieto", "TO v Teemaopinnot", "YH v Yhteiskuntaoppi"
          ))
        }
        "Historia" in {
          val (_, historia) = findRowsWithColumnsByTitle("HI v Historia", kurssit)
          verifyOppijanRow(lukiolainen, expectedLukiolainenHistorianKurssitRow, historia, addOpiskeluoikeudenOid = false)
          verifyOppijanRow(lukionAineopiskelijaAktiivinen, expectedAineopiskelijaHistoriaKurssitRow, historia, addOpiskeluoikeudenOid = false)
        }
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

  private def buildLukioraportti(organisaatioOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate) = {
    val sheets = LukioRaportti.buildRaportti(repository, organisaatioOid, alku, loppu)
    sheets.map(s => (s.title, zipRowsWithColumTitles(s)))
  }

  private def zipRowsWithColumTitles(sheet: DynamicDataSheet) = {
    sheet.rows.map(_.zip(sheet.columnSettings)).map(_.map { case (data, column) => column.title -> data }.toMap)
  }

  private def verifyOppijanRow(oppija: OppijaHenkilö, expected: Map[String, Any], all: Seq[Map[String, Any]], addOpiskeluoikeudenOid: Boolean = true) = {
    val expectedResult = if (addOpiskeluoikeudenOid) {
      val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
      opiskeluoikeudenOid shouldBe defined
      expected + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get)
    } else {
      expected
    }

    findFirstByOid(oppija.oid, all) should be(expectedResult)
  }

  private def verifyOppijanRows(oppija: OppijaHenkilö, expected: Seq[Map[String, Any]], all: Seq[Map[String, Any]]) = {
    val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
    opiskeluoikeudenOid shouldBe defined
    findByOid(oppija.oid, all).toSet should equal(expected.map(_ + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get)).toSet)
  }

  private def findRowsWithColumnsByTitle(title: String, all: Seq[(String, Seq[Map[String, Any]])]) = {
    val found = all.filter(_._1 == title)
    found.length should equal(1)
    found.head
  }

  private def findFirstByOid(oid: String, maps: Seq[Map[String, Any]]) = {
    val found = findByOid(oid, maps)
    found.length shouldBe (1)
    found.head
  }

  private def findByOid(oid: String, maps: Seq[Map[String, Any]]) = maps.filter(_.get("Oppijan oid").exists(_ == Some(oid)))

  private def verifyNoDuplicates(strs: Seq[String]) = strs.toSet.size should equal(strs.size)

  lazy val oid = "123"

  lazy val expectedLukiolainenRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Oppijan oid" -> Some(lukiolainen.oid),
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
    "Ulkomaanajaksot" -> Some(366),
    "Erityisen koulutustehtävän tehtävät" -> Some("Erityisenä koulutustehtävänä taide"),
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
    "Islam (KT) valtakunnallinen" -> "Arvosana 8, 3 kurssia",
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
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Oppijan oid" -> Some(lukionAineopiskelijaAktiivinen.oid),
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
    "Ulkomaanajaksot" -> None,
    "Erityisen koulutustehtävän tehtävät" -> None,
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
    "Islam (KT) valtakunnallinen" -> "",
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

  private def kurssintiedot(arvosana: String, laajuus: String = "1.0", tyyppi: String) = s"Arvosana $arvosana,Laajuus $laajuus,$tyyppi"

  lazy val expectedLukiolainenHistorianKurssitRow = Map(
    "Oppijan oid" -> Some(lukiolainen.oid),
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> Some(lukiolainen.sukunimi),
    "Etunimet" -> Some(lukiolainen.etunimet),
    "Ihminen ympäristön ja yhteiskuntien muutoksessa HI1 valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "Kansainväliset suhteet HI2 valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "pakollinen"),
    "Itsenäisen Suomen historia HI3 valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "Eurooppalaisen maailmankuvan kehitys HI4 valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen")
  )

  lazy val expectedAineopiskelijaHistoriaKurssitRow = Map(
    "Oppijan oid" -> Some(lukionAineopiskelijaAktiivinen.oid),
    "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
    "Sukunimi" -> Some(lukionAineopiskelijaAktiivinen.sukunimi),
    "Etunimet" -> Some(lukionAineopiskelijaAktiivinen.etunimet),
    "Ihminen ympäristön ja yhteiskuntien muutoksessa HI1 valtakunnallinen" -> "",
    "Kansainväliset suhteet HI2 valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "pakollinen"),
    "Itsenäisen Suomen historia HI3 valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "Eurooppalaisen maailmankuvan kehitys HI4 valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen")
  )
}
