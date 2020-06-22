package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsLukio
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.henkilo.MockOppijat._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import fi.oph.koski.schema._
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class LukioRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with OpiskeluoikeusTestMethodsLukio {

  override def beforeAll(): Unit = {
    lisääPäätasonSuorituksia(lukionAineopiskelijaAktiivinen, List(LukioExampleData.lukionOppiaineenOppimääränSuoritusA1Englanti, LukioExampleData.lukionOppiaineenOppimääränSuoritusPitkäMatematiikka))
    loadRaportointikantaFixtures
  }

  lazy val today = LocalDate.now
  lazy val repository = LukioRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  lazy val lukioRaportti = LukioRaportti(repository)

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
          "Päivitetty",
          "Yksilöity",
          "Oppijan oid",
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "Opiskeluoikeuden alkamispäivä",
          "Opiskeluoikeuden viimeisin tila",
          "Opiskeluoikeuden tilat aikajakson aikana",
          "Opetussuunnitelma",
          "Suorituksen tyyppi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Ryhmä",
          "Pidennetty päättymispäivä",
          "Ulkomainen vaihto-opiskelija",
          "Yksityisopiskelija",
          "Ulkomaanjaksot",
          "Erityisen koulutustehtävän tehtävät",
          "Erityisen koulutustehtävän jaksot",
          "Sisäoppilaitosmainen majoitus",
          "Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa",
          "Yhteislaajuus",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "A1 Englanti valtakunnallinen",
          "B1 Ruotsi valtakunnallinen",
          "B3 Latina valtakunnallinen",
          "MA Matematiikka, pitkä oppimäärä valtakunnallinen",
          "BI Biologia valtakunnallinen",
          "GE Maantieto valtakunnallinen",
          "FY Fysiikka valtakunnallinen",
          "KE Kemia valtakunnallinen",
          "FI Filosofia valtakunnallinen",
          "PS Psykologia valtakunnallinen",
          "HI Historia valtakunnallinen",
          "YH Yhteiskuntaoppi valtakunnallinen",
          "KT Islam valtakunnallinen",
          "TE Terveystieto valtakunnallinen",
          "LI Liikunta valtakunnallinen",
          "MU Musiikki valtakunnallinen",
          "KU Kuvataide valtakunnallinen",
          "TO Teemaopinnot valtakunnallinen",
          "OA Oman äidinkielen opinnot valtakunnallinen",
          "XX Ei tiedossa valtakunnallinen",
          "ITT Tanssi ja liike paikallinen"
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
          "Toimipiste",
          "Opetussuunnitelma",
          "Suorituksen tyyppi",
          "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen",
          "HI1 Ihminen, ympäristö ja kulttuuri valtakunnallinen",
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
          verifyOppijanRows(lukionAineopiskelijaAktiivinen,
            Seq(
              AktiivinenAineopiskelija.englanninOppiaineenRow,
              AktiivinenAineopiskelija.matematiikanOppiaineRow,
              AktiivinenAineopiskelija.historiaOppiaineenRow,
              AktiivinenAineopiskelija.kemiaOppiaineenRow,
              AktiivinenAineopiskelija.filosofiaOppiaineenRow
            ),
            oppiaineetRowsWithColumns
          )
          verifyOppijanRows(lukionEiTiedossaAineopiskelija,
            Seq(
              EiTiedossaOppiaineenOpiskelija.historiaOppiaineenRow,
              EiTiedossaOppiaineenOpiskelija.kemiaOppiaineenRow,
              EiTiedossaOppiaineenOpiskelija.filosofiaOppiaineenRow,
              EiTiedossaOppiaineenOpiskelija.eiTiedossaOppiaineenRow
            ),
            oppiaineetRowsWithColumns
          )
        }
      }
      "Kurssit tason välilehdet" - {
        lazy val kurssit = titleAndRowsWithColumns.tail
        "Välilehtien nimet, sisältää oppiaineet aakkosjärjestyksessä titlen mukaan" in {
          val kurssiVälilehtienTitlet = kurssit.map { case (title, _) => title }

          kurssiVälilehtienTitlet should equal(Seq(
            "AI v Suomen kieli ja kirjallisuus",
            "A1 v Englanti",
            "B1 v Ruotsi",
            "B3 v Latina",
            "MA v Matematiikka, pitkä oppimäärä",
            "BI v Biologia",
            "GE v Maantieto",
            "FY v Fysiikka",
            "KE v Kemia",
            "FI v Filosofia",
            "PS v Psykologia",
            "HI v Historia",
            "YH v Yhteiskuntaoppi",
            "KT v Islam",
            "TE v Terveystieto",
            "LI v Liikunta",
            "MU v Musiikki",
            "KU v Kuvataide",
            "TO v Teemaopinnot",
            "OA v Oman äidinkielen opinnot",
            "XX v Ei tiedossa",
            "ITT p Tanssi ja liike"
          ))
        }
        "Historia" in {
          val (_, historia) = findRowsWithColumnsByTitle("HI v Historia", kurssit)
          verifyOppijanRow(lukiolainen, expectedLukiolainenHistorianKurssitRow, historia, addOpiskeluoikeudenOid = false)
          verifyOppijanRow(lukionAineopiskelijaAktiivinen, AktiivinenAineopiskelija.historiaKurssitRow, historia, addOpiskeluoikeudenOid = false)
        }
        "Matematikka" in {
          val (_, matematiikka) = findRowsWithColumnsByTitle("MA v Matematiikka, pitkä oppimäärä", kurssit)
          verifyOppijanRow(lukionAineopiskelijaAktiivinen, AktiivinenAineopiskelija.matematiikanKurssitRow, matematiikka, addOpiskeluoikeudenOid = false)
        }
        "Ei tiedossa oppiaine" in {
          val (_, eiTiedossa) = findRowsWithColumnsByTitle("XX v Ei tiedossa", kurssit)
          verifyOppijanRow(lukionEiTiedossaAineopiskelija, EiTiedossaOppiaineenOpiskelija.eiTiedossaKurssitRow, eiTiedossa, addOpiskeluoikeudenOid = false)
        }
      }
    }

    "Opiskeluoikeus aikajaksojen siivous" - {
      "Jatkuva sama tila näytetään yhtenä tilana" in {
        YleissivistäväUtils.removeContinuousSameTila(Seq(
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
      "Alkaa ja päättyy ennen hakuväliä" in {
        YleissivistäväUtils.lengthInDaysInDateRange(Aikajakso(date(2012, 1, 1), Some(date(2014, 1, 1))), date(2014, 1, 2), date(2018, 1, 1)) shouldEqual (0)
      }
      "Alkaa hakuvälin jälkeen" in {
        YleissivistäväUtils.lengthInDaysInDateRange(Aikajakso(date(2018, 1, 2), Some(date(2020, 1, 1))), date(2010, 1, 2), date(2018, 1, 1)) shouldEqual (0)
      }
      "Alkanut ennen hakuväliä ja jatkuu hakuvälin yli" in {
        YleissivistäväUtils.lengthInDaysInDateRange(Aikajakso(date(2012, 1, 1), Some(date(2016, 1, 1))), date(2013, 1, 1), date(2015, 1, 1)) shouldEqual (731)
      }
      "Alkanut ennen hakuväliä ja jaksolle ei ole merkattu päättymistä" in {
        YleissivistäväUtils.lengthInDaysInDateRange(Aikajakso(date(2012, 1, 2), None), date(2013, 1, 1), date(2014, 1, 1)) shouldEqual (366)
      }
      "Alkanut ennen hakuväliä ja päättyy hakuvälillä" in {
        YleissivistäväUtils.lengthInDaysInDateRange(Aikajakso(date(2012, 1, 1), Some(date(2014, 1, 1))), date(2013, 1, 1), date(2018, 1, 1)) shouldEqual (366)
      }
      "Alkanut hakuvälillä ja päättyy hakuvälillä" in {
        YleissivistäväUtils.lengthInDaysInDateRange(Aikajakso(date(2011, 1, 1), Some(date(2012, 1, 1))), date(2010, 6, 6), date(2013, 1, 1)) shouldEqual (366)
      }
    }
  }

  private def buildLukioraportti(organisaatioOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate) = {
    lukioRaportti.buildRaportti(organisaatioOid, alku, loppu)
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

  private def verifyOppijanRows(oppija: LaajatOppijaHenkilöTiedot, expected: Seq[Map[String, Any]], all: Seq[Map[String, Any]]) = {
    val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
    opiskeluoikeudenOid shouldBe defined
    val found = findByOid(oppija.oid, all)
    found.length should equal(expected.length)
    found.toSet should equal(expected.map(_ + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get)).toSet)
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

  private def findByOid(oid: String, maps: Seq[Map[String, Any]]) = maps.filter(_.get("Oppijan oid").exists(_ == oid))

  private def verifyNoDuplicates(strs: Seq[String]) = strs.toSet.size should equal(strs.size)

  lazy val oid = "123"

  private def lisääPäätasonSuorituksia(oppija: LaajatOppijaHenkilöTiedot, päätasonSuoritukset: List[LukionPäätasonSuoritus]) = {
    val oo = getOpiskeluoikeus(oppija.oid, OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo).asInstanceOf[LukionOpiskeluoikeus]
    putOppija(Oppija(oppija, List(oo.copy(suoritukset = päätasonSuoritukset ::: oo.suoritukset)))) {
      verifyResponseStatusOk()
      loadRaportointikantaFixtures
    }
  }

  private def kurssintiedot(arvosana: String, laajuus: String = "1.0", tyyppi: String) = s"$tyyppi,Arvosana $arvosana,Laajuus $laajuus"

  lazy val expectedLukiolainenRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Päivitetty" -> today,
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Yksilöity" -> true,
    "Oppijan oid" -> lukiolainen.oid,
    "Opiskeluoikeuden alkamispäivä" -> Some(date(2012, 9, 1)),
    "Opiskeluoikeuden viimeisin tila" -> Some("valmistunut"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
    "Suorituksen tyyppi" -> "lukionoppimaara",
    "Suorituksen tila" -> "valmis",
    "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 8)),
    "Läsnäolopäiviä aikajakson aikana" -> 1218,
    "Rahoitukset" -> "1,1",
    "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
    "Ryhmä" -> Some("12A"),
    "Pidennetty päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Yksityisopiskelija" -> false,
    "Ulkomaanjaksot" -> Some(366),
    "Erityisen koulutustehtävän tehtävät" -> Some("Kieliin painottuva koulutus"),
    "Erityisen koulutustehtävän jaksot" -> Some(1),
    "Sisäoppilaitosmainen majoitus" -> Some(366),
    "Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa" -> Some("Pikkuvanha yksilö"),
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> lukiolainen.sukunimi,
    "Etunimet" -> lukiolainen.etunimet,
    "Yhteislaajuus" -> 89.5,
    "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 8 kurssia",
    "XX Ei tiedossa valtakunnallinen" -> "",
    "A1 Englanti valtakunnallinen" -> "Arvosana 9, 9 kurssia",
    "B1 Ruotsi valtakunnallinen" -> "Arvosana 7, 5 kurssia",
    "B3 Latina valtakunnallinen" -> "Arvosana 9, 2 kurssia",
    "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 9, 15 kurssia (joista 1 hylättyjä)",
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

  lazy val expectedLukiolainenHistorianKurssitRow = Map(
    "Oppijan oid" -> lukiolainen.oid,
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> lukiolainen.sukunimi,
    "Etunimet" -> lukiolainen.etunimet,
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
    "Suorituksen tyyppi" -> "lukionoppimaara",
    "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "HI1 Ihminen, ympäristö ja kulttuuri valtakunnallinen" -> "",
    "HI2 Kansainväliset suhteet valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "pakollinen"),
    "HI3 Itsenäisen Suomen historia valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
    "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen")
  )

  lazy val defaultAineopiskelijaRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Päivitetty" -> today,
    "Yksilöity" -> true,
    "Oppijan oid" -> lukionAineopiskelijaAktiivinen.oid,
    "Opiskeluoikeuden alkamispäivä" -> Some(date(2015, 9, 1)),
    "Opiskeluoikeuden viimeisin tila" -> Some("lasna"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
    "Suorituksen tyyppi" -> "lukionoppiaineenoppimaara",
    "Suorituksen tila" -> "valmis",
    "Suorituksen vahvistuspäivä" -> None,
    "Läsnäolopäiviä aikajakson aikana" -> 123,
    "Rahoitukset" -> "1",
    "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
    "Ryhmä" -> None,
    "Pidennetty päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Yksityisopiskelija" -> false,
    "Ulkomaanjaksot" -> None,
    "Erityisen koulutustehtävän tehtävät" -> None,
    "Erityisen koulutustehtävän jaksot" -> None,
    "Sisäoppilaitosmainen majoitus" -> None,
    "Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa" -> None,
    "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
    "Sukunimi" -> lukionAineopiskelijaAktiivinen.sukunimi,
    "Etunimet" -> lukionAineopiskelijaAktiivinen.etunimet,
    "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "",
    "XX Ei tiedossa valtakunnallinen" -> "",
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

  private object AktiivinenAineopiskelija {
    private lazy val default = defaultAineopiskelijaRow + (
      "Oppijan oid" -> lukionAineopiskelijaAktiivinen.oid,
      "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
      "Sukunimi" -> lukionAineopiskelijaAktiivinen.sukunimi,
      "Etunimet" -> lukionAineopiskelijaAktiivinen.etunimet
    )

    lazy val englanninOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus" -> 3.0,
      "A1 Englanti valtakunnallinen" -> "Arvosana 7, 3 kurssia"
    )

    lazy val matematiikanOppiaineRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus" -> 5.0,
      "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 8, 5 kurssia"
    )

    lazy val historiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus" -> 4.0,
      "HI Historia valtakunnallinen" -> "Arvosana 9, 4 kurssia"
    )

    lazy val kemiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2015, 1, 10)),
      "Yhteislaajuus" -> 1.0,
      "KE Kemia valtakunnallinen" -> "Arvosana 8, 1 kurssi"
    )

    lazy val filosofiaOppiaineenRow = default + (
      "Suorituksen tila" -> "kesken",
      "Suorituksen vahvistuspäivä" -> None,
      "Yhteislaajuus" -> 1.0,
      "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1 kurssi"
    )

    lazy val eiSuorituksiaKurssitRow = Map(
      "Oppijan oid" -> lukionAineopiskelijaAktiivinen.oid,
      "Hetu" -> lukionAineopiskelijaAktiivinen.hetu,
      "Sukunimi" -> lukionAineopiskelijaAktiivinen.sukunimi,
      "Etunimet" -> lukionAineopiskelijaAktiivinen.etunimet,
      "Toimipiste" -> "Jyväskylän normaalikoulu",
      "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
      "Suorituksen tyyppi" -> "lukionoppiaineenoppimaara"
    )

    lazy val historiaKurssitRow = eiSuorituksiaKurssitRow + (
      "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen" -> "",
      "HI1 Ihminen, ympäristö ja kulttuuri valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
      "HI2 Kansainväliset suhteet valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "pakollinen"),
      "HI3 Itsenäisen Suomen historia valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen"),
      "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen")
    )

    lazy val matematiikanKurssitRow = eiSuorituksiaKurssitRow + (
      "MAA1 Funktiot ja yhtälöt, pa, vuositaso 1 paikallinen" -> "",
      "MAA2 Polynomifunktiot ja -yhtälöt valtakunnallinen"  -> kurssintiedot(arvosana = "6", tyyppi = "pakollinen"),
      "MAA3 Geometria valtakunnallinen" -> "pakollinen,Arvosana 7,Laajuus 1.0",
      "MAA4 Vektorit valtakunnallinen" -> "pakollinen,Arvosana 8,Laajuus 1.0",
      "MAA5 Analyyttinen geometria valtakunnallinen" -> "pakollinen,Arvosana 9,Laajuus 1.0",
      "MAA6 Derivaatta valtakunnallinen" -> "pakollinen,Arvosana 10,Laajuus 1.0",
      "MAA7 Trigonometriset funktiot valtakunnallinen" -> "",
      "MAA8 Juuri- ja logaritmifunktiot valtakunnallinen" -> "",
      "MAA9 Integraalilaskenta valtakunnallinen" -> "",
      "MAA10 Todennäköisyys ja tilastot valtakunnallinen" -> "",
      "MAA11 Lukuteoria ja todistaminen valtakunnallinen" -> "",
      "MAA12 Algoritmit matematiikassa valtakunnallinen" -> "",
      "MAA13 Differentiaali- ja integraalilaskennan jatkokurssi valtakunnallinen" -> "",
      "MAA14 Kertauskurssi, ksy, vuositaso 3 paikallinen" -> "",
      "MAA16 Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2 paikallinen" -> ""
    )
  }

  private object EiTiedossaOppiaineenOpiskelija {
    private lazy val default = defaultAineopiskelijaRow + (
      "Oppijan oid" -> lukionEiTiedossaAineopiskelija.oid,
      "Hetu" -> lukionEiTiedossaAineopiskelija.hetu,
      "Sukunimi" -> lukionEiTiedossaAineopiskelija.sukunimi,
      "Etunimet" -> lukionEiTiedossaAineopiskelija.etunimet
    )

    lazy val eiTiedossaOppiaineenRow = default + (
      "XX Ei tiedossa valtakunnallinen" -> "Arvosana 9, 1 kurssi",
      "Suorituksen tila" -> "kesken",
      "Yhteislaajuus" -> 1.0,
      "Opetussuunnitelma" -> None
    )

    lazy val historiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus" -> 4.0,
      "HI Historia valtakunnallinen" -> "Arvosana 9, 4 kurssia"
    )

    lazy val kemiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2015, 1, 10)),
      "Yhteislaajuus" -> 1.0,
      "KE Kemia valtakunnallinen" -> "Arvosana 8, 1 kurssi"
    )

    lazy val filosofiaOppiaineenRow = default + (
      "Suorituksen tila" -> "kesken",
      "Suorituksen vahvistuspäivä" -> None,
      "Yhteislaajuus" -> 1.0,
      "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1 kurssi"
    )

    lazy val EiTiedossaOppiaineenRow = default + (
      "Suorituksen tila" -> "kesken",
      "Suorituksen vahvistuspäivä" -> None,
      "Yhteislaajuus" -> 1.0,
      "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1 kurssi"
    )

    lazy val eiTiedossaKurssitRow = Map(
      "Oppijan oid" -> lukionEiTiedossaAineopiskelija.oid,
      "Hetu" -> lukionEiTiedossaAineopiskelija.hetu,
      "Sukunimi" -> lukionEiTiedossaAineopiskelija.sukunimi,
      "Etunimet" -> lukionEiTiedossaAineopiskelija.etunimet,
      "Toimipiste" -> "Jyväskylän normaalikoulu",
      "Opetussuunnitelma" -> None,
      "Suorituksen tyyppi" -> "lukionoppiaineenoppimaara",
      "FI1 Johdatus filosofiseen ajatteluun valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "pakollinen")
    )
  }
}
