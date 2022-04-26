package fi.oph.koski.raportit

import fi.oph.koski.api.OpiskeluoikeusTestMethodsLukio2015
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019RaportitRepository, Lukio2019Raportti}
import fi.oph.koski.raportointikanta.{RaportointikantaTestMethods}
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class Lukio2019RaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with DirtiesFixtures with OpiskeluoikeusTestMethodsLukio2015 {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val opiskelijaSuppea = KoskiSpecificMockOppijat.teija

    val oppimäärä = defaultOpiskeluoikeus.copy(
      suoritukset = List(Lukio2019RaaportitTestData.oppimääränSuoritus),
    )

    putOppija(Oppija(opiskelijaSuppea, List(oppimäärä))) {
      verifyResponseStatusOk()
    }
    reloadRaportointikanta
  }

  private lazy val today = LocalDate.now
  private lazy val repository = Lukio2019RaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val lukioRaportti = Lukio2019Raportti(repository, t)

  "Lukion suoritustietoraportti" - {

    "Raportti näyttää oikealta" - {
      lazy val sheets = buildLukio2019raportti(jyväskylänNormaalikoulu, date(2000, 1, 1), date(2001, 1, 1), osasuoritustenAikarajaus = false)
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
          "Maksuttomuus",
          "Maksullisuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Yhteislaajuus (kaikki opintopisteet)",
          "Yhteislaajuus (suoritetut opintopisteet)",
          "Yhteislaajuus (hylätyllä arvosanalla suoritetut opintopisteet)",
          "Yhteislaajuus (tunnustetut opintopisteet)",
          "Yhteislaajuus (eri vuonna korotetut opintopisteet)",
          "AI Suomen kieli ja kirjallisuus valtakunnallinen",
          "ITT Tanssi ja liike paikallinen"
        ))
      }
      "Sarakkeiden järjestys oppiaineen kursseja käsittelevällä välilehdellä" in {
        val historia = sheets.find(_.title == "AI v Suomen kieli ja kirjallisuus")
        historia shouldBe defined
        historia.get.columnSettings.map(_.title) should equal(Seq(
          "Oppijan oid",
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "Toimipiste",
          "Opetussuunnitelma",
          "Suorituksen tyyppi",
          "ÄI1 Tekstien tulkinta ja kirjoittaminen valtakunnallinen",
          "ÄI2 Kieli- ja tekstitietoisuus valtakunnallinen",
          "ÄI3 Vuorovaikutus 1 valtakunnallinen"
        ))
      }
      "Oppiaine tason välilehti" - {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.head
        "On ensimmäinen" in {
          title should equal("Oppiaineet ja lisätiedot")
        }
        "Oppiaineiden suoritus" in {
          verifyOppijanRows(KoskiSpecificMockOppijat.teija,
            Seq(
              teijanRow
            ),
            oppiaineetRowsWithColumns
          )
        }
      }
    }
  }

  private def buildLukio2019raportti(organisaatioOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, osasuoritustenAikarajaus: Boolean) = {
    lukioRaportti.buildRaportti(organisaatioOid, alku, loppu, osasuoritustenAikarajaus)
  }

  private def zipRowsWithColumTitles(sheet: DynamicDataSheet) = {
    sheet.rows.map(_.zip(sheet.columnSettings)).map(_.map { case (data, column) => column.title -> data }.toMap)
  }

  private def verifyOppijanRows(oppija: LaajatOppijaHenkilöTiedot, expected: Seq[Map[String, Any]], all: Seq[Map[String, Any]]) = {
    val opiskeluoikeudenOid = lastOpiskeluoikeus(oppija.oid).oid
    opiskeluoikeudenOid shouldBe defined
    val found = findByOid(oppija.oid, all)
    found.length should equal(expected.length)
    found.toSet should equal(expected.map(_ + ("Opiskeluoikeuden oid" -> opiskeluoikeudenOid.get)).toSet)
  }

  private def findByOid(oid: String, maps: Seq[Map[String, Any]]) = maps.filter(_.get("Oppijan oid").exists(_ == oid))

  private def verifyNoDuplicates(strs: Seq[String]) = strs.toSet.size should equal(strs.size)

  lazy val teijanRow = Map(
    "Opiskeluoikeuden oid" -> "",
    "Oppilaitoksen nimi" -> "Jyväskylän normaalikoulu",
    "Lähdejärjestelmä" -> None,
    "Koulutustoimija" -> "Jyväskylän yliopisto",
    "Toimipiste" -> "Jyväskylän normaalikoulu",
    "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
    "Päivitetty" -> today,
    "Yksilöity" -> true,
    "Oppijan oid" -> KoskiSpecificMockOppijat.teija.oid,
    "Opiskeluoikeuden alkamispäivä" -> Some(date(2000, 1, 1)),
    "Opiskeluoikeuden viimeisin tila" -> Some("lasna"),
    "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
    "Opetussuunnitelma" -> Some("Lukio suoritetaan nuorten opetussuunnitelman mukaan"),
    "Suorituksen tyyppi" -> "lukionoppimaara",
    "Suorituksen tila" -> "kesken",
    "Suorituksen vahvistuspäivä" -> None,
    "Oppimäärä suoritettu" -> false,
    "Läsnäolopäiviä aikajakson aikana" -> 367,
    "Rahoitukset" -> "1",
    "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
    "Ryhmä" -> None,
    "Pidennetty päättymispäivä" -> false,
    "Ulkomainen vaihto-opiskelija" -> false,
    "Ulkomaanjaksot" -> None,
    "Erityisen koulutustehtävän tehtävät" -> None,
    "Erityisen koulutustehtävän jaksot" -> None,
    "Sisäoppilaitosmainen majoitus" -> None,
    "Maksuttomuus" -> None,
    "Maksullisuus" -> None,
    "Oikeutta maksuttomuuteen pidennetty" -> None,
    "Hetu" -> KoskiSpecificMockOppijat.teija.hetu,
    "Sukunimi" -> KoskiSpecificMockOppijat.teija.sukunimi,
    "Etunimet" -> KoskiSpecificMockOppijat.teija.etunimet,
    "AI Suomen kieli ja kirjallisuus valtakunnallinen" -> "Arvosana 9, 6.0 kurssia",
    "ITT Tanssi ja liike paikallinen" -> "Arvosana 8, 2.0 kurssia",
    "Yhteislaajuus (kaikki opintopisteet)" -> 8.0,
    "Yhteislaajuus (suoritetut opintopisteet)" -> 4.0,
    "Yhteislaajuus (tunnustetut opintopisteet)" -> 4.0,
    "Yhteislaajuus (hylätyllä arvosanalla suoritetut opintopisteet)" -> 0,
    "Yhteislaajuus (eri vuonna korotetut opintopisteet)" -> 2.0,
  )
}
