package fi.oph.koski.raportit

import fi.oph.koski.api.OpiskeluoikeusTestMethodsLukio2015
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportit.lukio.{LukioRaportitRepository, LukioRaportti}
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}

class LukioRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with DirtiesFixtures with OpiskeluoikeusTestMethodsLukio2015 {

  override protected def alterFixture(): Unit = {
    lisääPäätasonSuorituksia(lukionAineopiskelijaAktiivinen, List(LukioExampleData.lukionOppiaineenOppimääränSuoritusA1Englanti, LukioExampleData.lukionOppiaineenOppimääränSuoritusPitkäMatematiikka))
    reloadRaportointikanta
  }

  private lazy val today = LocalDate.now
  private lazy val repository = LukioRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val tSv: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "sv")
  private lazy val lukioRaportti = LukioRaportti(repository, t)
  private lazy val lukioRaporttiRuotsiksi = LukioRaportti(repository, tSv)

  "Lukion suoritustietoraportti" - {

    "Raportti näyttää oikealta" - {
      lazy val sheets = buildLukioraportti(jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1), osasuoritustenAikarajaus = false)
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
          "Oppimäärä suoritettu",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Ryhmä",
          "Pidennetty päättymispäivä",
          "Ulkomainen vaihto-opiskelija",
          "Ulkomaanjaksot",
          "Erityisen koulutustehtävän tehtävät",
          "Erityisen koulutustehtävän jaksot",
          "Sisäoppilaitosmainen majoitus",
          "Yhteislaajuus (kaikki kurssit)",
          "Yhteislaajuus (suoritetut kurssit)",
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)",
          "Yhteislaajuus (tunnustetut kurssit)",
          "Yhteislaajuus (eri vuonna korotetut kurssit)",
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
        "Musiikki (tunnustettu)" in {
          val (_, musiikki) = findRowsWithColumnsByTitle("MU v Musiikki", kurssit)
          val musiikinKurssitRow = Map(
            "Oppijan oid" -> "1.2.246.562.24.00000000013",
            "Sukunimi" -> "Lukiolainen",
            "Etunimet" -> "Liisa",
            "Hetu" -> Some("020655-2479"),
            "Suorituksen tyyppi" -> "lukionoppimaara",
            "Toimipiste" -> "Jyväskylän normaalikoulu",
            "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
            "MU1 Musiikki ja minä valtakunnallinen" -> "Pakollinen, Arvosana 8, Laajuus 1.0, Tunnustettu"
          )
          verifyOppijanRow(lukiolainen, musiikinKurssitRow, musiikki, addOpiskeluoikeudenOid = false)
        }
        "Ei tiedossa oppiaine" in {
          val (_, eiTiedossa) = findRowsWithColumnsByTitle("XX v Ei tiedossa", kurssit)
          verifyOppijanRow(lukionEiTiedossaAineopiskelija, EiTiedossaOppiaineenOpiskelija.eiTiedossaKurssitRow, eiTiedossa, addOpiskeluoikeudenOid = false)
        }
      }
    }

    "Raportti latautuu eri lokalisaatiolla" in {
      lazy val sheets = lukioRaporttiRuotsiksi.buildRaportti(jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1), osasuoritustenAikarajaus = false)
      sheets.forall(sheet => sheet.rows.flatten.nonEmpty) shouldBe true
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

    "Raportin sisältö on käännetty kielivalinnan mukaan" - {

      lazy val tSv: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "sv")
      lazy val titleAndRowsWithColumnsSv = LukioRaportti(repository, tSv)
        .buildRaportti(jyväskylänNormaalikoulu, date(2012, 1, 1), date(2016, 1, 1), osasuoritustenAikarajaus = false)
        .map(s => (s.title, zipRowsWithColumTitles(s)))
      lazy val kurssitNimetSv = titleAndRowsWithColumnsSv.tail.map(_._1)

      "ruotsiksi soveltuvin osin" in {
        kurssitNimetSv should not contain ("AI v Suomen kieli ja kirjallisuus")
        kurssitNimetSv should not contain ("A1 v Englanti")
        kurssitNimetSv should not contain ("B1 v Ruotsi")
        kurssitNimetSv should not contain ("MA v Matematiikka, pitkä oppimäärä")
        kurssitNimetSv should not contain ("OA v Oman äidinkielen opinnot")

        kurssitNimetSv should contain ("AI v Finska och litteratur")
        kurssitNimetSv should contain ("A1 v Engelska")
        kurssitNimetSv should contain ("B1 v Svenska")
        kurssitNimetSv should contain ("MA v Matematik, lång lärokurs")
        kurssitNimetSv should contain ("OA v Studier i det egna modersmålet")
      }
    }
  }

  private def buildLukioraportti(organisaatioOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, osasuoritustenAikarajaus: Boolean) = {
    lukioRaportti.buildRaportti(organisaatioOid, alku, loppu, osasuoritustenAikarajaus)
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

  // TODO: Lukion oppiaineiden oppimäärien ryhmittelevä suoritus ei ole LukionPäätasonSuoritus: ota huomioon
  private def lisääPäätasonSuorituksia(oppija: LaajatOppijaHenkilöTiedot, päätasonSuoritukset: List[LukionPäätasonSuoritus]) = {
    val oo = getOpiskeluoikeus(oppija.oid, OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo).asInstanceOf[LukionOpiskeluoikeus]
    putOppija(Oppija(oppija, List(oo.copy(suoritukset = päätasonSuoritukset ::: oo.suoritukset)))) {
      verifyResponseStatusOk()
      reloadRaportointikanta
    }
  }

  private def kurssintiedot(arvosana: String, laajuus: String = "1.0", tyyppi: String) = s"$tyyppi, Arvosana $arvosana, Laajuus $laajuus"

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
    "Oppimäärä suoritettu" -> true,
    "Läsnäolopäiviä aikajakson aikana" -> 1218,
    "Rahoitukset" -> "1,1",
    "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
    "Ryhmä" -> Some("12A"),
    "Pidennetty päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Ulkomaanjaksot" -> Some(366),
    "Erityisen koulutustehtävän tehtävät" -> Some("Kieliin painottuva koulutus"),
    "Erityisen koulutustehtävän jaksot" -> Some(366),
    "Sisäoppilaitosmainen majoitus" -> Some(366),
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> lukiolainen.sukunimi,
    "Etunimet" -> lukiolainen.etunimet,
    "Yhteislaajuus (kaikki kurssit)" -> 90.5,
    "Yhteislaajuus (suoritetut kurssit)" -> 89.5,
    "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 1.0,
    "Yhteislaajuus (tunnustetut kurssit)" -> 1.0,
    "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 1.0,
    "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 8.0 kurssia",
    "XX Ei tiedossa valtakunnallinen" -> "",
    "A1 Englanti valtakunnallinen" -> "Arvosana 9, 9.0 kurssia",
    "B1 Ruotsi valtakunnallinen" -> "Arvosana 7, 5.0 kurssia",
    "B3 Latina valtakunnallinen" -> "Arvosana 9, 2.0 kurssia",
    "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 9, 15.0 kurssia (joista 1.0 hylättyjä)",
    "BI Biologia valtakunnallinen" -> "Arvosana 9, 7.5 kurssia",
    "GE Maantieto valtakunnallinen" -> "Arvosana 8, 2.0 kurssia",
    "FY Fysiikka valtakunnallinen" -> "Arvosana 8, 13.0 kurssia",
    "KE Kemia valtakunnallinen" -> "Arvosana 8, 8.0 kurssia",
    "KT Islam valtakunnallinen" -> "Arvosana 8, 3.0 kurssia",
    "FI Filosofia valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
    "PS Psykologia valtakunnallinen" -> "Arvosana 9, 1.0 kurssia",
    "HI Historia valtakunnallinen" -> "Arvosana 7, 4.0 kurssia",
    "YH Yhteiskuntaoppi valtakunnallinen" -> "Arvosana 8, 2.0 kurssia",
    "LI Liikunta valtakunnallinen" -> "Arvosana 9, 3.0 kurssia",
    "MU Musiikki valtakunnallinen" -> "Arvosana 8, 1.0 kurssia",
    "KU Kuvataide valtakunnallinen" -> "Arvosana 9, 2.0 kurssia",
    "TE Terveystieto valtakunnallinen" -> "Arvosana 9, 1.0 kurssia",
    "ITT Tanssi ja liike paikallinen" -> "Arvosana 10, 1.0 kurssia",
    "TO Teemaopinnot valtakunnallinen" -> "Arvosana S, 1.0 kurssia",
    "OA Oman äidinkielen opinnot valtakunnallinen" -> "Arvosana S, 1.0 kurssia"
  )

  lazy val expectedLukiolainenHistorianKurssitRow = Map(
    "Oppijan oid" -> lukiolainen.oid,
    "Hetu" -> lukiolainen.hetu,
    "Sukunimi" -> lukiolainen.sukunimi,
    "Etunimet" -> lukiolainen.etunimet,
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
    "Suorituksen tyyppi" -> "lukionoppimaara",
    "HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "Pakollinen"),
    "HI1 Ihminen, ympäristö ja kulttuuri valtakunnallinen" -> "",
    "HI2 Kansainväliset suhteet valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "Pakollinen"),
    "HI3 Itsenäisen Suomen historia valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "Pakollinen"),
    "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "Pakollinen")
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
    "Oppimäärä suoritettu" -> false,
    "Läsnäolopäiviä aikajakson aikana" -> 123,
    "Rahoitukset" -> "1",
    "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
    "Ryhmä" -> None,
    "Pidennetty päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Ulkomaanjaksot" -> None,
    "Erityisen koulutustehtävän tehtävät" -> None,
    "Erityisen koulutustehtävän jaksot" -> None,
    "Sisäoppilaitosmainen majoitus" -> None,
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
      "Etunimet" -> lukionAineopiskelijaAktiivinen.etunimet,
    )

    lazy val englanninOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus (kaikki kurssit)" -> 3.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 3.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "A1 Englanti valtakunnallinen" -> "Arvosana 7, 3.0 kurssia"
    )

    lazy val matematiikanOppiaineRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus (kaikki kurssit)" -> 5.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 5.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "MA Matematiikka, pitkä oppimäärä valtakunnallinen" -> "Arvosana 8, 5.0 kurssia"
    )

    lazy val historiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus (kaikki kurssit)" -> 4.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 4.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "HI Historia valtakunnallinen" -> "Arvosana 9, 4.0 kurssia"
    )

    lazy val kemiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2015, 1, 10)),
      "Yhteislaajuus (kaikki kurssit)" -> 1.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "KE Kemia valtakunnallinen" -> "Arvosana 8, 1.0 kurssia"
    )

    lazy val filosofiaOppiaineenRow = default + (
      "Suorituksen tila" -> "kesken",
      "Suorituksen vahvistuspäivä" -> None,
      "Yhteislaajuus (kaikki kurssit)" -> 1.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1.0 kurssia"
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
      "HI1 Ihminen, ympäristö ja kulttuuri valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "Pakollinen"),
      "HI2 Kansainväliset suhteet valtakunnallinen" -> kurssintiedot(arvosana = "8", tyyppi = "Pakollinen"),
      "HI3 Itsenäisen Suomen historia valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "Pakollinen"),
      "HI4 Eurooppalaisen maailmankuvan kehitys valtakunnallinen" -> kurssintiedot(arvosana = "6", tyyppi = "Pakollinen")
    )

    lazy val matematiikanKurssitRow = eiSuorituksiaKurssitRow + (
      "MAA1 Funktiot ja yhtälöt, pa, vuositaso 1 paikallinen" -> "",
      "MAA2 Polynomifunktiot ja -yhtälöt valtakunnallinen"  -> kurssintiedot(arvosana = "6", tyyppi = "Pakollinen"),
      "MAA3 Geometria valtakunnallinen" -> "Pakollinen, Arvosana 7, Laajuus 1.0",
      "MAA4 Vektorit valtakunnallinen" -> "Pakollinen, Arvosana 8, Laajuus 1.0",
      "MAA5 Analyyttinen geometria valtakunnallinen" -> "Pakollinen, Arvosana 9, Laajuus 1.0",
      "MAA6 Derivaatta valtakunnallinen" -> "Pakollinen, Arvosana 10, Laajuus 1.0",
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
      "Etunimet" -> lukionEiTiedossaAineopiskelija.etunimet,
    )

    lazy val eiTiedossaOppiaineenRow = default + (
      "XX Ei tiedossa valtakunnallinen" -> "Arvosana 9, 1.0 kurssia",
      "Suorituksen tila" -> "kesken",
      "Yhteislaajuus (kaikki kurssit)" -> 1.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "Opetussuunnitelma" -> None
    )

    lazy val historiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2016, 1, 10)),
      "Yhteislaajuus (kaikki kurssit)" -> 4.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 4.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "HI Historia valtakunnallinen" -> "Arvosana 9, 4.0 kurssia"
    )

    lazy val kemiaOppiaineenRow = default + (
      "Suorituksen vahvistuspäivä" -> Some(date(2015, 1, 10)),
      "Yhteislaajuus (kaikki kurssit)" -> 1.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "KE Kemia valtakunnallinen" -> "Arvosana 8, 1.0 kurssia"
    )

    lazy val filosofiaOppiaineenRow = default + (
      "Suorituksen tila" -> "kesken",
      "Suorituksen vahvistuspäivä" -> None,
      "Yhteislaajuus (kaikki kurssit)" -> 1.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1.0 kurssia"
    )

    lazy val EiTiedossaOppiaineenRow = default + (
      "Suorituksen tila" -> "kesken",
      "Suorituksen vahvistuspäivä" -> None,
      "Yhteislaajuus (kaikki kurssit)" -> 1.0,
      "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
      "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
      "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
      "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
      "FI Filosofia valtakunnallinen" -> "Arvosana 9, 1.0 kurssia"
    )

    lazy val eiTiedossaKurssitRow = Map(
      "Oppijan oid" -> lukionEiTiedossaAineopiskelija.oid,
      "Hetu" -> lukionEiTiedossaAineopiskelija.hetu,
      "Sukunimi" -> lukionEiTiedossaAineopiskelija.sukunimi,
      "Etunimet" -> lukionEiTiedossaAineopiskelija.etunimet,
      "Toimipiste" -> "Jyväskylän normaalikoulu",
      "Opetussuunnitelma" -> None,
      "Suorituksen tyyppi" -> "lukionoppiaineenoppimaara",
      "FI1 Johdatus filosofiseen ajatteluun valtakunnallinen" -> kurssintiedot(arvosana = "7", tyyppi = "Pakollinen")
    )
  }
}
