package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAikuistenPerusopetus
import fi.oph.koski.documentation.{ExampleData, ExamplesAikuistenPerusopetus, YleissivistavakoulutusExampleData}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.henkilo.MockOppijat._
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import fi.oph.koski.schema._
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class AikuistenPerusopetusRaporttiSpec
  extends FreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with BeforeAndAfterAll
    with OpiskeluoikeusTestMethodsAikuistenPerusopetus {

  private lazy val today = LocalDate.now
  private lazy val repository = AikuistenPerusopetusRaporttiRepository(KoskiApplicationForTests.raportointiDatabase.db)

  override def beforeAll(): Unit = {
    lisääPäätasonSuorituksia(
      aikuisOpiskelija,
      List(
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritusAI1,
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritusYH
      )
    )
    loadRaportointikantaFixtures
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
        AikuistenPerusopetusAlkuvaiheRaportti()
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
          "Suorituksen tyyppi",
          "Opiskeluoikeudella päättövaiheen suoritus",
          "Tutkintokoodi/koulutusmoduulin koodi",
          "Suorituksen nimi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Ryhmä",
          "Ulkomaanjaksot",
          "Majoitusetu",
          "Sisäoppilaitosmainen majoitus",
          "Yhteislaajuus",
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
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "Toimipiste",
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
            "Koulutustoimija" -> "Jyväskylän yliopisto",
            "Toimipiste" -> "Jyväskylän normaalikoulu",
            "Yksilöity" -> true,
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "Opiskeluoikeuden alkamispäivä" -> Some(date(2008, 8, 15)),
            "Opiskeluoikeuden viimeisin tila" -> Some("valmistunut"),
            "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
            "Suorituksen tyyppi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "Opiskeluoikeudella päättövaiheen suoritus" -> true,
            "Tutkintokoodi/koulutusmoduulin koodi" -> "aikuistenperusopetuksenoppimaaranalkuvaihe",
            "Suorituksen nimi" -> Some("Aikuisten perusopetuksen oppimäärän alkuvaihe"),
            "Suorituksen tila" -> "valmis",
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Läsnäolopäiviä aikajakson aikana" -> 1462,
            "Rahoitukset" -> "1",
            "Ryhmä" -> None,
            "Ulkomaanjaksot" -> None,
            "Majoitusetu" -> None,
            "Sisäoppilaitosmainen majoitus" -> None,
            "Hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Yhteislaajuus" -> 0.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 15 kurssia",
            "A1 Englanti valtakunnallinen" -> "Arvosana 7, 4 kurssia",
            "MA Matematiikka valtakunnallinen" -> "Arvosana 10, 3 kurssia",
            "YH Yhteiskuntatietous ja kulttuurintuntemus valtakunnallinen" -> "Arvosana 8, 4 kurssia",
            "TE Terveystieto valtakunnallinen" -> "Arvosana 10, 1 kurssi",
            "OP Opinto-ohjaus ja työelämän taidot valtakunnallinen" -> "Arvosana S, 0 kurssia",
            "YL Ympäristö- ja luonnontieto valtakunnallinen" -> "Arvosana 8, 1 kurssi"
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
            "Hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Toimipiste" -> "Jyväskylän normaalikoulu",
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
  }

  "Aikuisten perusopetuksen oppiaineen oppimäärän suoritustietoraportti" - {
    "Raportti näyttää oikealta" - {
      lazy val sheets = buildReport(
        jyväskylänNormaalikoulu,
        date(2012, 1, 1),
        date(2016, 1, 1),
        AikuistenPerusopetusOppiaineenOppimääräRaportti()
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
          "Suorituksen tyyppi",
          "Opiskeluoikeudella alkuvaiheen suoritus",
          "Opiskeluoikeudella päättövaiheen suoritus",
          "Tutkintokoodi/koulutusmoduulin koodi",
          "Suorituksen nimi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset",
          "Ryhmä",
          "Ulkomaanjaksot",
          "Majoitusetu",
          "Sisäoppilaitosmainen majoitus",
          "Yhteislaajuus",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "YH Yhteiskuntaoppi valtakunnallinen"
        ))
      }

      "Sarakkeiden järjestys oppiaineen kursseja käsittelevällä välilehdellä" in {
        val yh = sheets.find(_.title == "YH v Yhteiskuntaoppi")
        yh shouldBe defined
        yh.get.columnSettings.map(_.title) should equal(Seq(
          "Oppijan oid",
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "Toimipiste",
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
            "Koulutustoimija" -> "Jyväskylän yliopisto",
            "Toimipiste" -> "Jyväskylän normaalikoulu",
            "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
            "Päivitetty" -> today,
            "Yksilöity" -> true,
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "Opiskeluoikeuden alkamispäivä" -> Some(date(2008, 8, 15)),
            "Opiskeluoikeuden viimeisin tila" -> Some("valmistunut"),
            "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
            "Opiskeluoikeudella alkuvaiheen suoritus" -> true,
            "Opiskeluoikeudella päättövaiheen suoritus" -> true,
            "Suorituksen tyyppi" -> "perusopetuksenoppiaineenoppimaara",
            "Suorituksen tila" -> "valmis",
            "Suorituksen vahvistuspäivä" -> None,
            "Läsnäolopäiviä aikajakson aikana" -> 1462,
            "Majoitusetu" -> None,
            "Rahoitukset" -> "1",
            "Ryhmä" -> None,
            "Ulkomaanjaksot" -> None,
            "Sisäoppilaitosmainen majoitus" -> None,
            "Hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet
          )

          lazy val aiOppiaineenRow = common + (
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Yhteislaajuus" -> 1.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 1 kurssi",
            "YH Yhteiskuntaoppi valtakunnallinen" -> "",
            "Tutkintokoodi/koulutusmoduulin koodi" -> "AI",
            "Suorituksen nimi" -> Some("Äidinkieli ja kirjallisuus")
          )

          lazy val yhOppiaineenRow = common + (
            "Suorituksen vahvistuspäivä" -> Some(date(2016, 6, 4)),
            "Yhteislaajuus" -> 1.0,
            "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "",
            "YH Yhteiskuntaoppi valtakunnallinen" -> "Arvosana 10, 1 kurssi",
            "Tutkintokoodi/koulutusmoduulin koodi" -> "YH",
            "Suorituksen nimi" -> Some("Yhteiskuntaoppi")
          )

          verifyOppijanRows(aikuisOpiskelija,
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
            "YH v Yhteiskuntaoppi"
          ))
        }

        "YH" in {
          lazy val expectedaikuisOpiskelijaYhKurssitRow = Map(
            "Oppijan oid" -> aikuisOpiskelija.oid,
            "Hetu" -> aikuisOpiskelija.hetu,
            "Sukunimi" -> aikuisOpiskelija.sukunimi,
            "Etunimet" -> aikuisOpiskelija.etunimet,
            "Toimipiste" -> "Jyväskylän normaalikoulu",
            "Suorituksen tyyppi" -> "perusopetuksenoppiaineenoppimaara",
            "YH1 Yhteiskuntajärjestelmä sekä julkiset palvelut valtakunnallinen" -> kurssintiedot(arvosana = "9")
          )
          val (_, yh) = findRowsWithColumnsByTitle("YH v Yhteiskuntaoppi", kurssit)
          verifyOppijanRow(aikuisOpiskelija, expectedaikuisOpiskelijaYhKurssitRow, yh, addOpiskeluoikeudenOid = false)
        }
      }
    }
  }

  private def buildReport(
    organisaatioOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    raportinTyyppi: AikuistenPerusopetusRaporttiType
  ) = {
    AikuistenPerusopetusRaportti(repository, raportinTyyppi).build(organisaatioOid, alku, loppu, osasuoritustenAikarajaus = false)
    // TODO: Myös osasuoritustenAikarajaus=true?
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

  private def lisääPäätasonSuorituksia(oppija: LaajatOppijaHenkilöTiedot, päätasonSuoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus]) = {
    val oo = getOpiskeluoikeus(oppija.oid, OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo).asInstanceOf[AikuistenPerusopetuksenOpiskeluoikeus]
    putOppija(Oppija(oppija, List(oo.copy(suoritukset = päätasonSuoritukset ::: oo.suoritukset)))) {
      verifyResponseStatusOk()
      loadRaportointikantaFixtures
    }
  }

  private def kurssintiedot(arvosana: String, laajuus: String = "1.0") =
    s"Arvosana $arvosana, Laajuus $laajuus"
}
