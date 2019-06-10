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
      lazy val sheets = buildLukioraportti(jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1))
      lazy val titleAndRowsWithColumns = sheets.map(s => (s.title, zipRowsWithColumTitles(s)))
      "Oppiaineita tai kursseja ei päädy duplikaattina raportille" in {
        verifyNoDuplicates(sheets.map(_.title))
        sheets.map(_.columnSettings.map(_.title)).foreach(verifyNoDuplicates)
      }
      "Sarakkeidein järjestys oppiaine tason välilehdellä" in {
        sheets.head.columnSettings.map(_.title) should equal(Seq(
          "Opiskeluoikeuden oid",
          "Lähdejärjestelmä",
          "Koulutustoimija",
          "Oppilaitoksen nimi",
          "Toimipiste",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä",
          "Oppijan oid",
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "Opiskeluoikeuden alkamispäivä",
          "Opiskeluoikeuden viimeisin tila",
          "Opiskeluoikeuden tilat aikajakson aikana",
          "Suorituksen koulutustyyppi",
          "Suorituksen tyyppi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Ryhmä",
          "Pidennetty Päättymispäivä",
          "Ulkomainen vaihto-opiskelija",
          "Yksityisopiskelija",
          "Ulkomaanjaksot",
          "Erityisen koulutustehtävän tehtävät",
          "Erityisen koulutustehtävän jaksot",
          "Sisäoppilaitosmainen majoitus",
          "Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa",
          "Yhteislaajuus",
          "BI Biologia valtakunnallinen",
          "A1 Englanti valtakunnallinen",
          "FI Filosofia valtakunnallinen",
          "FY Fysiikka valtakunnallinen",
          "HI Historia valtakunnallinen",
          "KT Islam valtakunnallinen",
          "KE Kemia valtakunnallinen",
          "KU Kuvataide valtakunnallinen",
          "B3 Latina valtakunnallinen",
          "LI Liikunta valtakunnallinen",
          "GE Maantieto valtakunnallinen",
          "MA Matematiikka, pitkä oppimäärä valtakunnallinen",
          "MU Musiikki valtakunnallinen",
          "OA Oman äidinkielen opinnot valtakunnallinen",
          "PS Psykologia valtakunnallinen",
          "B1 Ruotsi valtakunnallinen",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "ITT Tanssi ja liike paikallinen",
          "TO Teemaopinnot valtakunnallinen",
          "TE Terveystieto valtakunnallinen",
          "YH Yhteiskuntaoppi valtakunnallinen"
        ))
      }
      "Sarakkeiden järjestys oppiaineen kursseja käsittelevällä välilehdellä" in {
        val historia = sheets.find(_.title == "HI v Historia")
        historia shouldBe defined
        historia.get.columnSettings.map(_.title) should equal(Seq(
          "Oppijan oid",
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen",
          "HI2 Kansainväliset suhteet valtakunnallinen",
          "HI3 Itsenäisen Suomen historia valtakunnallinen",
          "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen"
        ))
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
            "BI v Biologia",
            "A1 v Englanti",
            "FI v Filosofia",
            "FY v Fysiikka",
            "HI v Historia",
            "KT v Islam",
            "KE v Kemia",
            "KU v Kuvataide",
            "B3 v Latina",
            "LI v Liikunta",
            "GE v Maantieto",
            "MA v Matematiikka, pitkä oppimäärä",
            "MU v Musiikki",
            "OA v Oman äidinkielen opinnot",
            "PS v Psykologia",
            "B1 v Ruotsi",
            "AI v Suomen kieli ja kirjallisuus",
            "ITT p Tanssi ja liike",
            "TO v Teemaopinnot",
            "TE v Terveystieto",
            "YH v Yhteiskuntaoppi"
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
    LukioRaportti.buildRaportti(repository, organisaatioOid, alku, loppu)
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

  private def kurssintiedot(arvosana: String, laajuus: String = "1.0", tyyppi: String) = s"$tyyppi,Arvosana $arvosana,Laajuus $laajuus"

  lazy val expectedLukiolainenRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Oppijan oid" -> Some(lukiolainen.oid),
    "Opiskeluoikeuden alkamispäivä" -> Some(date(2012, 9, 1)),
    "Opiskeluoikeuden viimeisin tila" -> Some("valmistunut"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Suorituksen koulutustyyppi" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
    "Suorituksen tyyppi" -> "lukionoppimaara",
    "Suorituksen tila" -> "valmis",
    "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 8)),
    "Läsnäolopäiviä aikajakson aikana" -> 1218,
    "Rahoitukset" -> "",
    "Ryhmä" -> Some("12A"),
    "Pidennetty Päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Yksityisopiskelija" -> false,
    "Ulkomaanjaksot" -> Some(366),
    "Erityisen koulutustehtävän tehtävät" -> Some("Erityisenä koulutustehtävänä taide"),
    "Erityisen koulutustehtävän jaksot" -> Some(1),
    "Sisäoppilaitosmainen majoitus" -> Some(366),
    "Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa" -> Some("Pikkuvanha yksilö"),
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> Some(lukiolainen.sukunimi),
    "Etunimet" -> Some(lukiolainen.etunimet),
    "Yhteislaajuus" -> 89.5,
    "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 8 kurssia",
    "A1 Englanti valtakunnallinen" -> "Arvosana 9, 9 kurssia",
    "B1 Ruotsi valtakunnallinen" -> "Arvosana 7, 5 kurssia",
    "B3 Latina valtakunnallinen" -> "Arvosana 9, 2 kurssia",
    "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 9, 15 kurssia",
    "BI Biologia valtakunnallinen" -> "Arvosana 9, 8 kurssia",
    "GE Maantieto valtakunnallinen" -> "Arvosana 8, 2 kurssia",
    "FY Fysiikka valtakunnallinen" -> "Arvosana 8, 13 kurssia",
    "KE Kemia valtakunnallinen" -> "Arvosana 8, 8 kurssia",
    "KT Islam valtakunnallinen" -> "Arvosana 8, 3 kurssia",
    "FI Filosofia valtakunnallinen" -> "Arvosana 8, 1 kurssi",
    "PS Psykologia valtakunnallinen" -> "Arvosana 9, 1 kurssi",
    "HI Historia valtakunnallinen" -> "Arvosana 7, 4 kurssia",
    "YH Yhteiskuntaoppi valtakunnallinen" -> "Arvosana 8, 2 kurssia",
    "LI Liikunta valtakunnallinen" -> "Arvosana 9, 3 kurssia",
    "MU Musiikki valtakunnallinen" -> "Arvosana 8, 1 kurssi",
    "KU Kuvataide valtakunnallinen" -> "Arvosana 9, 2 kurssia",
    "TE Terveystieto valtakunnallinen" -> "Arvosana 9, 1 kurssi",
    "ITT Tanssi ja liike paikallinen" -> "Arvosana 10, 1 kurssi",
    "TO Teemaopinnot valtakunnallinen" -> "Arvosana S, 1 kurssi",
    "OA Oman äidinkielen opinnot valtakunnallinen" -> "Arvosana S, 1 kurssi"
  )

  lazy val defaultExpectedAineopiskelijaRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Oppijan oid" -> Some(lukionAineopiskelijaAktiivinen.oid),
    "Opiskeluoikeuden alkamispäivä" -> Some(date(2015, 9, 1)),
    "Opiskeluoikeuden viimeisin tila" -> Some("lasna"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Suorituksen koulutustyyppi" -> None,
    "Suorituksen tyyppi" -> "lukionoppiaineenoppimaara",
    "Suorituksen tila" -> "valmis",
    "Suorituksen vahvistuspäivä" -> None,
    "Läsnäolopäiviä aikajakson aikana" -> 123,
    "Rahoitukset" -> "",
    "Ryhmä" -> None,
    "Pidennetty Päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Yksityisopiskelija" -> false,
    "Ulkomaanjaksot" -> None,
    "Erityisen koulutustehtävän tehtävät" -> None,
    "Erityisen koulutustehtävän jaksot" -> None,
    "Sisäoppilaitosmainen majoitus" -> None,
    "Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa" -> None,
    "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
    "Sukunimi" -> Some(lukionAineopiskelijaAktiivinen.sukunimi),
    "Etunimet" -> Some(lukionAineopiskelijaAktiivinen.etunimet),
    "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "",
    "A1 Englanti valtakunnallinen" -> "",
    "B1 Ruotsi valtakunnallinen" -> "",
    "B3 Latina valtakunnallinen" -> "",
    "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "",
    "BI Biologia valtakunnallinen" -> "",
    "GE Maantieto valtakunnallinen" -> "",
    "FY Fysiikka valtakunnallinen" -> "",
    "KE Kemia valtakunnallinen" -> "",
    "KT Islam valtakunnallinen" -> "",
    "FI Filosofia valtakunnallinen" -> "",
    "PS Psykologia valtakunnallinen" -> "",
    "HI Historia valtakunnallinen" -> "",
    "YH Yhteiskuntaoppi valtakunnallinen" -> "",
    "LI Liikunta valtakunnallinen" -> "",
    "MU Musiikki valtakunnallinen" -> "",
    "KU Kuvataide valtakunnallinen" -> "",
    "TE Terveystieto valtakunnallinen" -> "",
    "ITT Tanssi ja liike paikallinen" -> "",
    "TO Teemaopinnot valtakunnallinen" -> "",
    "OA Oman äidinkielen opinnot valtakunnallinen" -> ""
  )

  lazy val expectedAineopiskelijaHistoriaRow = defaultExpectedAineopiskelijaRow + (
    "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
    "Yhteislaajuus" -> 4.0,
    "HI Historia valtakunnallinen" -> "Arvosana 9, 4 kurssia"
  )

  lazy val expectedAineopiskelijaKemiaRow = defaultExpectedAineopiskelijaRow + (
    "Suorituksen vahvistuspäivä" -> Some(date(2015, 1, 10)),
    "Yhteislaajuus" -> 1.0,
    "KE Kemia valtakunnallinen" -> "Arvosana 8, 1 kurssi"
  )

  lazy val expectedAineopiskelijaFilosofiaRow = defaultExpectedAineopiskelijaRow + (
    "Suorituksen tila" -> "kesken",
    "Suorituksen vahvistuspäivä" -> None,
    "Yhteislaajuus" -> 1.0,
    "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1 kurssi"
  )


  lazy val expectedLukiolainenHistorianKurssitRow = Map(
    "Oppijan oid" -> Some(lukiolainen.oid),
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> Some(lukiolainen.sukunimi),
    "Etunimet" -> Some(lukiolainen.etunimet),
    "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "HI2 Kansainväliset suhteet valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "pakollinen"),
    "HI3 Itsenäisen Suomen historia valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen")
  )

  lazy val expectedAineopiskelijaHistoriaKurssitRow = Map(
    "Oppijan oid" -> Some(lukionAineopiskelijaAktiivinen.oid),
    "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
    "Sukunimi" -> Some(lukionAineopiskelijaAktiivinen.sukunimi),
    "Etunimet" -> Some(lukionAineopiskelijaAktiivinen.etunimet),
    "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen" -> "",
    "HI2 Kansainväliset suhteet valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "pakollinen"),
    "HI3 Itsenäisen Suomen historia valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen")
  )
}
