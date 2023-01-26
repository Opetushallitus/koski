package fi.oph.koski.raportit

import fi.oph.koski.api.OpiskeluoikeusTestMethodsAikuistenPerusopetus
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.{ExampleData, ExamplesAikuistenPerusopetus, YleissivistavakoulutusExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportit.aikuistenperusopetus._
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}

class AikuistenPerusopetusRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with DirtiesFixtures
    with OpiskeluoikeusTestMethodsAikuistenPerusopetus {

  private lazy val today = LocalDate.now
  private lazy val repository = AikuistenPerusopetusRaporttiRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val tSv: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "sv")

  override protected def alterFixture(): Unit = {
    lisääMaksuttomuusJaPäätasonSuorituksia(
      vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
      List(
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritusAI1,
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritusYH
      )
    )
    reloadRaportointikanta
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = AikuistenPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu),
    suoritukset = List(
      aikuistenPerusopetuksenOppimääränSuoritus(diaari).copy(osasuoritukset = None, vahvistus = None)
    ),
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(AikuistenPerusopetuksenOpiskeluoikeusjakso(
      ExampleData.longTimeAgo,
      ExampleData.opiskeluoikeusLäsnä,
      Some(ExampleData.valtionosuusRahoitteinen))
    ))
  )

  private def aikuistenPerusopetuksenOppimääränSuoritus(diaari: Option[String] = Some("19/011/2015")) = {
    ExamplesAikuistenPerusopetus.aikuistenPerusopetukseOppimääränSuoritus(
      AikuistenPerusopetus(diaari),
      (if (diaari == Some("OPH-1280-2017")) {
        ExamplesAikuistenPerusopetus.oppiaineidenSuoritukset2017
      } else {
        ExamplesAikuistenPerusopetus.oppiaineidenSuoritukset2015
      })
    )
  }

  "Aikuisten perusopetuksen alkuvaiheen suoritustietoraportti" - {
    "Raportti näyttää oikealta" - {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusAlkuvaiheRaportti
      )
      lazy val titleAndRowsWithColumns = sheets.map(s => (s.title, zipRowsWithColumTitles(s)))

      "Oppiaineita tai kursseja ei päädy duplikaattina raportille" in {
        verifyNoDuplicates(sheets.map(_.title))
        sheets.map(_.columnSettings.map(_.title)).foreach(verifyNoDuplicates)
      }

      "Sarakkeidein järjestys oppiaine tason välilehdellä" in {
        sheets.head.columnSettings.map(_.title) should equal(Seq(
          "Opiskeluoikeuden oid",
          "Lähdejärjestelmä",
          "Koulutustoimijan nimi",
          "Oppilaitoksen nimi",
          "Toimipisteen nimi",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä",
          "Päivitetty",
          "Yksilöity",
          "Oppijan oid",
          "hetu",
          "Sukunimi",
          "Etunimet",
          "Opiskeluoikeuden alkamispäivä",
          "Viimeisin opiskeluoikeuden tila",
          "Opiskeluoikeuden tilat aikajakson aikana",
          "Suorituksen tyyppi",
          "Opiskeluoikeudella päättövaiheen suoritus",
          "Tutkintokoodi/koulutusmoduulin koodi",
          "Suorituksen nimi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Ryhmä",
          "Ulkomaanjaksot",
          "Majoitusetu",
          "Sisäoppilaitosmainen majoitus",
          "Maksuttomuus",
          "Maksullisuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Yhteislaajuus (kaikki kurssit)",
          "Yhteislaajuus (suoritetut kurssit)",
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)",
          "Yhteislaajuus (tunnustetut kurssit)",
          "Yhteislaajuus (eri vuonna korotetut kurssit)",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "A1 Englanti valtakunnallinen",
          "MA Matematiikka valtakunnallinen",
          "YH Yhteiskuntatietous ja kulttuurintuntemus valtakunnallinen",
          "TE Terveystieto valtakunnallinen",
          "OP Opinto-ohjaus ja työelämän taidot valtakunnallinen",
          "YL Ympäristö- ja luonnontieto valtakunnallinen"
        ))
      }

      "Sarakkeiden järjestys oppiaineen kursseja käsittelevällä välilehdellä" in {
        val yh = sheets.find(_.title == "YH v Yhteiskuntatietous ja kulttuurintuntemus")
        yh shouldBe defined
        yh.get.columnSettings.map(_.title) should equal(Seq(
          "Oppijan oid",
          "hetu",
          "Sukunimi",
          "Etunimet",
          "Toimipisteen nimi",
          "Suorituksen tyyppi",
          "LYK1 Arkielämä ja yhteiskunnan palvelut valtakunnallinen",
          "LYK2 Demokraattinen yhteiskunta valtakunnallinen",
          "LYKX Kulttuurinen moniarvoisuus paikallinen",
          "LYKY Tasa-arvo yhteiskunnassa paikallinen"
        ))
      }

      "Oppiaine tason välilehti" - {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.head

        "On ensimmäinen" in {
          title should equal("Oppiaineet ja lisätiedot")
        }

        "Oppimäärän suoritus" in {
          lazy val expectedaikuisOpiskelijaRow = Map(
            "Opiskeluoikeuden oid" -> "",
            "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
            "Lähdejärjestelmä" -> None,
            "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
            "Päivitetty" -> today,
            "Koulutustoimijan nimi" -> "Jyväskylän yliopisto",
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Yksilöity" -> true,
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "Opiskeluoikeuden alkamispäivä" -> Some(date(2008, 8, 15)),
            "Viimeisin opiskeluoikeuden tila" -> Some("valmistunut"),
            "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
            "Suorituksen tyyppi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "Opiskeluoikeudella päättövaiheen suoritus" -> true,
            "Tutkintokoodi/koulutusmoduulin koodi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "Suorituksen nimi" -> Some("Aikuisten perusopetuksen oppimäärän alkuvaihe"),
            "Suorituksen tila" -> "valmis",
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Läsnäolopäiviä aikajakson aikana" -> 1462,
            "Rahoitukset" -> "1",
            "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
            "Ryhmä" -> None,
            "Ulkomaanjaksot" -> None,
            "Majoitusetu" -> None,
            "Sisäoppilaitosmainen majoitus" -> None,
            "Maksuttomuus" -> None,
            "Maksullisuus" -> None,
            "Oikeutta maksuttomuuteen pidennetty" -> None,
            "hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Yhteislaajuus (kaikki kurssit)" -> 27.0,
            "Yhteislaajuus (suoritetut kurssit)" -> 25.0,
            "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
            "Yhteislaajuus (tunnustetut kurssit)" -> 2.0,
            "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 14.0 kurssia",
            "A1 Englanti valtakunnallinen" -> "Arvosana 7, 4.0 kurssia",
            "MA Matematiikka valtakunnallinen" -> "Arvosana 10, 3.0 kurssia",
            "YH Yhteiskuntatietous ja kulttuurintuntemus valtakunnallinen" -> "Arvosana 8, 4.0 kurssia",
            "TE Terveystieto valtakunnallinen" -> "Arvosana 10, 1.0 kurssia",
            "OP Opinto-ohjaus ja työelämän taidot valtakunnallinen" -> "Arvosana S, 0.0 kurssia",
            "YL Ympäristö- ja luonnontieto valtakunnallinen" -> "Arvosana 8, 1.0 kurssia"
          )

          verifyOppijanRow(aikuisOpiskelija, expectedaikuisOpiskelijaRow, oppiaineetRowsWithColumns)
        }
      }

      "Kurssit tason välilehdet" - {
        lazy val kurssit = titleAndRowsWithColumns.tail
        "Välilehtien nimet, sisältää oppiaineet aakkosjärjestyksessä titlen mukaan" in {
          val kurssiVälilehtienTitlet = kurssit.map { case (title, _) => title }

          kurssiVälilehtienTitlet should equal(Seq(
            "AI v Suomen kieli ja kirjallisuus",
            "A1 v Englanti",
            "MA v Matematiikka",
            "YH v Yhteiskuntatietous ja kulttuurintuntemus",
            "TE v Terveystieto",
            "OP v Opinto-ohjaus ja työelämän taidot",
            "YL v Ympäristö- ja luonnontieto"
          ))
        }

        "YH" in {
          val expectedaikuisOpiskelijaYhKurssitRow = Map(
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Suorituksen tyyppi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "LYK1 Arkielämä ja yhteiskunnan palvelut valtakunnallinen" -> kurssintiedot(arvosana = "9"),
            "LYK2 Demokraattinen yhteiskunta valtakunnallinen" -> kurssintiedot(arvosana = "9"),
            "LYKX Kulttuurinen moniarvoisuus paikallinen" -> kurssintiedot(arvosana = "9"),
            "LYKY Tasa-arvo yhteiskunnassa paikallinen" -> kurssintiedot(arvosana = "9")
          )
          val (_, yh) = findRowsWithColumnsByTitle("YH v Yhteiskuntatietous ja kulttuurintuntemus", kurssit)
          verifyOppijanRow(aikuisOpiskelija, expectedaikuisOpiskelijaYhKurssitRow, yh, addOpiskeluoikeudenOid = false)
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

    "Latautuu eri lokalisaatiolla" in {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusAlkuvaiheRaportti,
        localizationReader = tSv
      )
      sheets.forall(sheet => sheet.rows.flatten.nonEmpty) shouldBe true
    }
  }

  "Aikuisten perusopetuksen alkuvaiheen suoritustietoraportti osasuoritusten aikarajauksella" - {
    "Raportti näyttää oikealta" - {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusAlkuvaiheRaportti,
        osasuoritustenAikarajaus = true
      )
      lazy val titleAndRowsWithColumns = sheets.map(s => (s.title, zipRowsWithColumTitles(s)))

      "Kurssit tason välilehdet" - {
        lazy val kurssit = titleAndRowsWithColumns.tail
        "Välilehtien nimet, sisältää oppiaineet aakkosjärjestyksessä titlen mukaan" in {
          val kurssiVälilehtienTitlet = kurssit.map { case (title, _) => title }

          kurssiVälilehtienTitlet should equal(Seq(
            "AI v Suomen kieli ja kirjallisuus",
            "A1 v Englanti",
            "MA v Matematiikka",
            "YH v Yhteiskuntatietous ja kulttuurintuntemus",
            "TE v Terveystieto",
            "OP v Opinto-ohjaus ja työelämän taidot",
            "YL v Ympäristö- ja luonnontieto"
          ))
        }

        "YH" in {
          val expectedaikuisOpiskelijaYhKurssitRow = Map(
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Suorituksen tyyppi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe"
          )
          val (_, yh) = findRowsWithColumnsByTitle("YH v Yhteiskuntatietous ja kulttuurintuntemus", kurssit)
          verifyOppijanRow(aikuisOpiskelija, expectedaikuisOpiskelijaYhKurssitRow, yh, addOpiskeluoikeudenOid = false)
        }
      }

      "Oppiaine tason välilehti" - {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.head

        "On ensimmäinen" in {
          title should equal("Oppiaineet ja lisätiedot")
        }

        "Oppimäärän suoritus" in {
          lazy val expectedaikuisOpiskelijaRow = Map(
            "Opiskeluoikeuden oid" -> "",
            "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
            "Lähdejärjestelmä" -> None,
            "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
            "Päivitetty" -> today,
            "Koulutustoimijan nimi" -> "Jyväskylän yliopisto",
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Yksilöity" -> true,
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "Opiskeluoikeuden alkamispäivä" -> Some(date(2008, 8, 15)),
            "Viimeisin opiskeluoikeuden tila" -> Some("valmistunut"),
            "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
            "Suorituksen tyyppi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "Opiskeluoikeudella päättövaiheen suoritus" -> true,
            "Tutkintokoodi/koulutusmoduulin koodi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "Suorituksen nimi" -> Some("Aikuisten perusopetuksen oppimäärän alkuvaihe"),
            "Suorituksen tila" -> "valmis",
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Läsnäolopäiviä aikajakson aikana" -> 1462,
            "Rahoitukset" -> "1",
            "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
            "Ryhmä" -> None,
            "Ulkomaanjaksot" -> None,
            "Majoitusetu" -> None,
            "Sisäoppilaitosmainen majoitus" -> None,
            "Maksuttomuus" -> None,
            "Maksullisuus" -> None,
            "Oikeutta maksuttomuuteen pidennetty" -> None,
            "hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Yhteislaajuus (kaikki kurssit)" -> 0.0,
            "Yhteislaajuus (suoritetut kurssit)" -> 0.0,
            "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
            "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
            "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 0.0 kurssia",
            "A1 Englanti valtakunnallinen" -> "Arvosana 7, 0.0 kurssia",
            "MA Matematiikka valtakunnallinen" -> "Arvosana 10, 0.0 kurssia",
            "YH Yhteiskuntatietous ja kulttuurintuntemus valtakunnallinen" -> "Arvosana 8, 0.0 kurssia",
            "TE Terveystieto valtakunnallinen" -> "Arvosana 10, 0.0 kurssia",
            "OP Opinto-ohjaus ja työelämän taidot valtakunnallinen" -> "Arvosana S, 0.0 kurssia",
            "YL Ympäristö- ja luonnontieto valtakunnallinen" -> "Arvosana 8, 0.0 kurssia"
          )

          verifyOppijanRow(aikuisOpiskelija, expectedaikuisOpiskelijaRow, oppiaineetRowsWithColumns)
        }
      }
    }

    "Latautuu eri lokalisaatiolla" in {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusAlkuvaiheRaportti,
        osasuoritustenAikarajaus = true,
        localizationReader = tSv
      )
      sheets.forall(sheet => sheet.rows.flatten.nonEmpty) shouldBe true
    }
  }

  "Aikuisten perusopetuksen päättövaiheen suoritustietoraportti" - {
    "Raportti näyttää oikealta" - {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusPäättövaiheRaportti
      )
      lazy val titleAndRowsWithColumns = sheets.map(s => (s.title, zipRowsWithColumTitles(s)))

      "Sarakkeidein järjestys oppiaine tason välilehdellä" in {
        sheets.head.columnSettings.map(_.title) should equal(Seq(
          "Opiskeluoikeuden oid",
          "Lähdejärjestelmä",
          "Koulutustoimijan nimi",
          "Oppilaitoksen nimi",
          "Toimipisteen nimi",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä",
          "Päivitetty",
          "Yksilöity",
          "Oppijan oid",
          "hetu",
          "Sukunimi",
          "Etunimet",
          "Opiskeluoikeuden alkamispäivä",
          "Viimeisin opiskeluoikeuden tila",
          "Opiskeluoikeuden tilat aikajakson aikana",
          "Suorituksen tyyppi",
          "Opiskeluoikeudella alkuvaiheen suoritus",
          "Tutkintokoodi/koulutusmoduulin koodi",
          "Suorituksen nimi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Ryhmä",
          "Ulkomaanjaksot",
          "Majoitusetu",
          "Sisäoppilaitosmainen majoitus",
          "Maksuttomuus",
          "Maksullisuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Yhteislaajuus (kaikki kurssit)",
          "Yhteislaajuus (suoritetut kurssit)",
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)",
          "Yhteislaajuus (tunnustetut kurssit)",
          "Yhteislaajuus (eri vuonna korotetut kurssit)",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "A1 Englanti valtakunnallinen",
          "B1 Ruotsi valtakunnallinen",
          "B2 Saksa valtakunnallinen",
          "MA Matematiikka valtakunnallinen",
          "BI Biologia valtakunnallinen",
          "GE Maantieto valtakunnallinen",
          "FY Fysiikka valtakunnallinen",
          "KE Kemia valtakunnallinen",
          "HI Historia valtakunnallinen",
          "YH Yhteiskuntaoppi valtakunnallinen",
          "KT Ortodoksinen uskonto valtakunnallinen",
          "TE Terveystieto valtakunnallinen",
          "LI Liikunta valtakunnallinen",
          "MU Musiikki valtakunnallinen",
          "KU Kuvataide valtakunnallinen",
          "KO Kotitalous valtakunnallinen",
          "KS Käsityö valtakunnallinen",
          "TH Tietokoneen hyötykäyttö paikallinen"
        ))
      }

      "Sarakkeiden järjestys oppiaineen kursseja käsittelevällä välilehdellä" in {
        val yh = sheets.find(_.title == "YH v Yhteiskuntaoppi")
        yh shouldBe defined
        yh.get.columnSettings.map(_.title) should equal(Seq(
          "Oppijan oid",
          "hetu",
          "Sukunimi",
          "Etunimet",
          "Toimipisteen nimi",
          "Suorituksen tyyppi"
        ))
      }

      "Oppiaine tason välilehti" - {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.head

        "On ensimmäinen" in {
          title should equal("Oppiaineet ja lisätiedot")
        }

        "Oppimäärän suoritus" in {
          lazy val expectedaikuisOpiskelijaRow = Map(
            "Opiskeluoikeuden oid" -> "",
            "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
            "Lähdejärjestelmä" -> None,
            "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
            "Päivitetty" -> today,
            "Koulutustoimijan nimi" -> "Jyväskylän yliopisto",
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Yksilöity" -> true,
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "Opiskeluoikeuden alkamispäivä" -> Some(date(2008, 8, 15)),
            "Viimeisin opiskeluoikeuden tila" -> Some("valmistunut"),
            "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
            "Suorituksen tyyppi" -> "aikuistenperusopetuksenoppimaara",
            "Opiskeluoikeudella alkuvaiheen suoritus" -> true,
            "Tutkintokoodi/koulutusmoduulin koodi" -> "201101",
            "Suorituksen nimi" -> Some("Perusopetus"),
            "Suorituksen tila" -> "valmis",
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Läsnäolopäiviä aikajakson aikana" -> 1462,
            "Rahoitukset" -> "1",
            "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
            "Ryhmä" -> None,
            "Ulkomaanjaksot" -> None,
            "Majoitusetu" -> None,
            "Sisäoppilaitosmainen majoitus" -> None,
            "Maksuttomuus" -> None,
            "Maksullisuus" -> None,
            "Oikeutta maksuttomuuteen pidennetty" -> None,
            "hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Yhteislaajuus (kaikki kurssit)" -> 5.0,
            "Yhteislaajuus (suoritetut kurssit)" -> 3.0,
            "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 1.0,
            "Yhteislaajuus (tunnustetut kurssit)" -> 2.0,
            "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 1.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 5.0 kurssia (joista 1.0 hylättyjä)",
            "A1 Englanti valtakunnallinen" -> "Arvosana 8, 0.0 kurssia",
            "B1 Ruotsi valtakunnallinen" -> "Arvosana 8, 0.0 kurssia,Arvosana S, 0.0 vuosiviikkotuntia",
            "B2 Saksa valtakunnallinen" -> "Arvosana 9, 0.0 vuosiviikkotuntia",
            "MA Matematiikka valtakunnallinen" -> "Arvosana 9, 0.0 kurssia",
            "BI Biologia valtakunnallinen" -> "Arvosana 9, 0.0 kurssia",
            "GE Maantieto valtakunnallinen" -> "Arvosana 9, 0.0 kurssia",
            "FY Fysiikka valtakunnallinen" -> "Arvosana 9, 0.0 kurssia",
            "KE Kemia valtakunnallinen" -> "Arvosana 7, 0.0 kurssia",
            "HI Historia valtakunnallinen" -> "Arvosana 8, 0.0 kurssia",
            "YH Yhteiskuntaoppi valtakunnallinen" -> "Arvosana 10, 0.0 kurssia",
            "KT Ortodoksinen uskonto valtakunnallinen" -> "Arvosana 10, 0.0 kurssia",
            "TE Terveystieto valtakunnallinen" -> "Arvosana 8, 0.0 kurssia",
            "LI Liikunta valtakunnallinen" -> "Arvosana 9, 0.0 kurssia,Arvosana S, 0.0 vuosiviikkotuntia",
            "MU Musiikki valtakunnallinen" -> "Arvosana 7, 0.0 kurssia",
            "KU Kuvataide valtakunnallinen" -> "Arvosana 8, 0.0 kurssia",
            "KO Kotitalous valtakunnallinen" -> "Arvosana 8, 0.0 kurssia,Arvosana S, 0.0 vuosiviikkotuntia",
            "KS Käsityö valtakunnallinen" -> "Arvosana 9, 0.0 kurssia",
            "TH Tietokoneen hyötykäyttö paikallinen" -> "Arvosana 9, 0.0 kurssia"
          )

          verifyOppijanRow(aikuisOpiskelija, expectedaikuisOpiskelijaRow, oppiaineetRowsWithColumns)
        }
      }
    }

    "Latautuu eri lokalisaatiolla" in {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusPäättövaiheRaportti,
        localizationReader = tSv
      )
      sheets.forall(sheet => sheet.rows.flatten.nonEmpty) shouldBe true
    }
  }

  "Aikuisten perusopetuksen oppiaineen oppimäärän suoritustietoraportti" - {
    "Raportti näyttää oikealta" - {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2022, 1, 1),
        date(2026, 1, 1),
        AikuistenPerusopetusOppiaineenOppimääräRaportti
      )
      lazy val titleAndRowsWithColumns = sheets.map(s => (s.title, zipRowsWithColumTitles(s)))

      "Oppiaineita tai kursseja ei päädy duplikaattina raportille" in {
        verifyNoDuplicates(sheets.map(_.title))
        sheets.map(_.columnSettings.map(_.title)).foreach(verifyNoDuplicates)
      }

      "Sarakkeidein järjestys oppiaine tason välilehdellä" in {
        sheets.head.columnSettings.map(_.title) should equal(Seq(
          "Opiskeluoikeuden oid",
          "Lähdejärjestelmä",
          "Koulutustoimijan nimi",
          "Oppilaitoksen nimi",
          "Toimipisteen nimi",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä",
          "Päivitetty",
          "Yksilöity",
          "Oppijan oid",
          "hetu",
          "Sukunimi",
          "Etunimet",
          "Opiskeluoikeuden alkamispäivä",
          "Viimeisin opiskeluoikeuden tila",
          "Opiskeluoikeuden tilat aikajakson aikana",
          "Suorituksen tyyppi",
          "Opiskeluoikeudella alkuvaiheen suoritus",
          "Opiskeluoikeudella päättövaiheen suoritus",
          "Tutkintokoodi/koulutusmoduulin koodi",
          "Suorituksen nimi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Ryhmä",
          "Ulkomaanjaksot",
          "Majoitusetu",
          "Sisäoppilaitosmainen majoitus",
          "Maksuttomuus",
          "Maksullisuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Yhteislaajuus (kaikki kurssit)",
          "Yhteislaajuus (suoritetut kurssit)",
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)",
          "Yhteislaajuus (tunnustetut kurssit)",
          "Yhteislaajuus (eri vuonna korotetut kurssit)",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "MA Matematiikka valtakunnallinen",
          "YH Yhteiskuntaoppi valtakunnallinen"
        ))
      }

      "Sarakkeiden järjestys oppiaineen kursseja käsittelevällä välilehdellä" in {
        val yh = sheets.find(_.title == "YH v Yhteiskuntaoppi")
        yh shouldBe defined
        yh.get.columnSettings.map(_.title) should equal(Seq(
          "Oppijan oid",
          "hetu",
          "Sukunimi",
          "Etunimet",
          "Toimipisteen nimi",
          "Suorituksen tyyppi",
          "YH1 Yhteiskuntajärjestelmä sekä julkiset palvelut valtakunnallinen"
        ))
      }

      "Oppiaine tason välilehti" - {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.head

        "On ensimmäinen" in {
          title should equal("Oppiaineet ja lisätiedot")
        }

        "Oppiaineiden suoritus" in {
          lazy val common = Map(
            "Opiskeluoikeuden oid" -> "",
            "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
            "Lähdejärjestelmä" -> None,
            "Koulutustoimijan nimi" -> "Jyväskylän yliopisto",
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
            "Päivitetty" -> today,
            "Yksilöity" -> true,
            "Oppijan oid" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.oid,
            "Opiskeluoikeuden alkamispäivä" -> Some(date(2021, 1, 1)),
            "Viimeisin opiskeluoikeuden tila" -> Some("lasna"),
            "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
            "Opiskeluoikeudella alkuvaiheen suoritus" -> true,
            "Opiskeluoikeudella päättövaiheen suoritus" -> true,
            "Suorituksen tyyppi" -> "perusopetuksenoppiaineenoppimaara",
            "Suorituksen tila" -> "valmis",
            "Suorituksen vahvistuspäivä" -> None,
            "Läsnäolopäiviä aikajakson aikana" -> 1462,
            "Majoitusetu" -> None,
            "Rahoitukset" -> "1, 1",
            "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
            "Ryhmä" -> None,
            "Ulkomaanjaksot" -> None,
            "Sisäoppilaitosmainen majoitus" -> None,
            "Maksuttomuus" -> Some("2022-01-01 – 2025-01-01"),
            "Maksullisuus" -> Some("2025-01-02 – "),
            "Oikeutta maksuttomuuteen pidennetty" -> Some("2022-01-01 – 2025-01-01"),
            "hetu" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.hetu,
            "Sukunimi" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.sukunimi,
            "Etunimet" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.etunimet
          )

          lazy val aiOppiaineenRow = common + (
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Yhteislaajuus (kaikki kurssit)" -> 1.0,
            "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
            "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
            "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
            "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 1.0 kurssia",
            "YH Yhteiskuntaoppi valtakunnallinen" -> "",
            "MA Matematiikka valtakunnallinen" -> "",
            "Tutkintokoodi/koulutusmoduulin koodi" -> "AI",
            "Suorituksen nimi" -> Some("Äidinkieli ja kirjallisuus")
          )

          lazy val yhOppiaineenRow = common + (
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Yhteislaajuus (kaikki kurssit)" -> 1.0,
            "Yhteislaajuus (suoritetut kurssit)" -> 1.0,
            "Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)" -> 0.0,
            "Yhteislaajuus (tunnustetut kurssit)" -> 0.0,
            "Yhteislaajuus (eri vuonna korotetut kurssit)" -> 0.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "",
            "YH Yhteiskuntaoppi valtakunnallinen" -> "Arvosana 10, 1.0 kurssia",
            "MA Matematiikka valtakunnallinen" -> "",
            "Tutkintokoodi/koulutusmoduulin koodi" -> "YH",
            "Suorituksen nimi" -> Some("Yhteiskuntaoppi")
          )

          verifyOppijanRows(vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            Seq(
              aiOppiaineenRow,
              yhOppiaineenRow
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
            "MA v Matematiikka",
            "YH v Yhteiskuntaoppi"
          ))
        }

        "YH" in {
          lazy val expectedaikuisOpiskelijaYhKurssitRow = Map(
            "Oppijan oid" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.oid,
            "hetu" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.hetu,
            "Sukunimi" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.sukunimi,
            "Etunimet" -> vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.etunimet,
            "Toimipisteen nimi" -> "Jyväskylän normaalikoulu",
            "Suorituksen tyyppi" -> "perusopetuksenoppiaineenoppimaara",
            "YH1 Yhteiskuntajärjestelmä sekä julkiset palvelut valtakunnallinen" -> kurssintiedot(arvosana = "9")
          )
          val (_, yh) = findRowsWithColumnsByTitle("YH v Yhteiskuntaoppi", kurssit)
          verifyOppijanRow(
            vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
            expectedaikuisOpiskelijaYhKurssitRow,
            yh,
            addOpiskeluoikeudenOid = false
          )
        }
      }
    }

    "Latautuu eri lokalisaatiolla" in {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusOppiaineenOppimääräRaportti,
        localizationReader = tSv
      )
      sheets.forall(sheet => sheet.rows.flatten.nonEmpty) shouldBe true
    }
  }

  private def buildReport(
    organisaatioOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    raportinTyyppi: AikuistenPerusopetusRaporttiType,
    osasuoritustenAikarajaus: Boolean = false,
    localizationReader: LocalizationReader = t
  ) = {
    AikuistenPerusopetusRaportti(
      repository,
      raportinTyyppi,
      organisaatioOid,
      alku,
      loppu,
      osasuoritustenAikarajaus = osasuoritustenAikarajaus,
      localizationReader
    ).build()
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

  private def lisääMaksuttomuusJaPäätasonSuorituksia(oppija: LaajatOppijaHenkilöTiedot, päätasonSuoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus]) = {
    val oo = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen
    putOppija(Oppija(oppija, List(
      oo.copy(
        oid = None,
        tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
          List(AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2021, 1, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))
        ),
        lisätiedot = Some(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
          maksuttomuus = Some(List(
            Maksuttomuus(date(2021, 1, 1), Some(date(2021, 12, 31)), true),
            Maksuttomuus(date(2022, 1, 1), Some(date(2025, 1, 1)), true),
            Maksuttomuus(date(2025, 1, 2), None, false),
          )),
          oikeuttaMaksuttomuuteenPidennetty = Some(List(OikeuttaMaksuttomuuteenPidennetty(
            date(2022, 1, 1),
            date(2025, 1, 1)
          )))
        )),
        suoritukset = päätasonSuoritukset ::: oo.suoritukset)))
    ) {
      verifyResponseStatusOk()
    }
  }

  private def kurssintiedot(arvosana: String, laajuus: String = "1.0") =
    s"Arvosana $arvosana, Laajuus $laajuus"
}
