package fi.oph.koski.raportit

import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.raportit.tuva.TuvaSuoritustiedotRaportti
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class TuvaSuoritustiedotRaporttiSpec
  extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with DirtiesFixtures {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  "Tutkintokoulutukseen valmentavan koulutuksen suoritustietoraportti" - {

    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/tuvasuoritustietojentarkistus?oppilaitosOid=$stadinAmmattiopisto&alku=2018-01-01&loppu=2022-01-01&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="tuva_suoritustiedot_${stadinAmmattiopisto}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=tuvasuoritustietojentarkistus&oppilaitosOid=$stadinAmmattiopisto&alku=2018-01-01&loppu=2022-01-01&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/tuvasuoritustietojentarkistus?oppilaitosOid=$stadinAmmattiopisto&alku=2018-01-01&loppu=2022-01-01&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="tuva_suoritustiedot_${stadinAmmattiopisto}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=tuvasuoritustietojentarkistus&oppilaitosOid=$stadinAmmattiopisto&alku=2018-01-01&loppu=2022-01-01&lang=sv")))
      }
    }

    "Raportti näyttää oikealta" - {
      lazy val sheets = TuvaSuoritustiedotRaportti.buildRaportti(KoskiApplicationForTests.raportointiDatabase, stadinAmmattiopisto, date(2000, 1, 1), date(2022, 9, 30), t)
      lazy val eiRivejäSheets = TuvaSuoritustiedotRaportti.buildRaportti(KoskiApplicationForTests.raportointiDatabase, stadinAmmattiopisto, date(1987, 1, 1), date(1988, 9, 30), t)
      lazy val titleAndRowsWithColumns = sheets.map(s => (s.title, zipRowsWithColumTitles(s)))

      "Taulukoiden tai sarakkeiden nimiä ei päädy duplikaattina raportille" in {
        verifyNoDuplicates(sheets.map(_.title))
        sheets.map(_.columnSettings.map(_.title)).foreach(verifyNoDuplicates)
      }

      "Yksi taulukko per datasta löytyvä järjestämislupa" in {
        sheets.size shouldBe 2
        sheets.exists(_.title == "Ammatillisen järjestämislupa") shouldBe true
        sheets.exists(_.title == "Perusopetuksen järjestämislupa") shouldBe true
      }

      "Yksi taulukko per oletusjärjestämislupa kun raportin hakuparametreilla ei löydy rivejä" in {
        eiRivejäSheets.size shouldBe 3
        eiRivejäSheets.exists(_.title == "Ammatillisen järjestämislupa") shouldBe true
        eiRivejäSheets.exists(_.title == "Lukion järjestämislupa") shouldBe true
        eiRivejäSheets.exists(_.title == "Perusopetuksen järjestämislupa") shouldBe true
      }

      "Sarakkeidein järjestys ammatillisen järjestysluvan taulukossa" in {
        sheets.find(_.title == "Ammatillisen järjestämislupa").get.columnSettings.map(_.title) should equal(Seq(
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
          "Päätason suoritusten nimet",
          "Opiskeluoikeuden päättymispäivä",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Järjestämislupa",
          "Maksuttomuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Majoitus (pv)",
          "Sisäoppilaitosmainen majoitus (pv)",
          "Vaativan erityisen tuen yhteydessä järjestettävä majoitus (pv)",
          "Erityinen tuki (pv)",
          "Vaativat erityisen tuen tehtävä (pv)",
          "Ulkomaanjaksot (pv)",
          "Vaikeasti vammaisten opetus (pv)",
          "Vammainen ja avustaja (pv)",
          "Osa-aikaisuusjaksot (prosentit)",
          "Osa-aikaisuus keskimäärin (%)",
          "Vankilaopetuksessa (pv)",
          "Koulutusvienti",
          "Pidennetty päättymispäivä"
        ))
      }

      "Sarakkeidein järjestys lukion järjestysluvan taulukossa" in {
        eiRivejäSheets.find(_.title == "Lukion järjestämislupa").get.columnSettings.map(_.title) should equal(Seq(
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
          "Päätason suoritusten nimet",
          "Opiskeluoikeuden päättymispäivä",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Järjestämislupa",
          "Maksuttomuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Sisäoppilaitosmainen majoitus (pv)",
          "Ulkomaanjaksot (pv)",
          "Pidennetty päättymispäivä"
        ))
      }

      "Sarakkeidein järjestys perusopetuksen järjestysluvan taulukossa" in {
        sheets.find(_.title == "Perusopetuksen järjestämislupa").get.columnSettings.map(_.title) should equal(Seq(
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
          "Päätason suoritusten nimet",
          "Opiskeluoikeuden päättymispäivä",
          "Rahoitukset",
          "Läsnä/valmistunut-rahoitusmuodot syötetty",
          "Järjestämislupa",
          "Maksuttomuus",
          "Oikeutta maksuttomuuteen pidennetty",
          "Majoitusetu (pv)",
          "Sisäoppilaitosmainen majoitus (pv)",
          "Erityisen tuen päätökset (pv)",
          "Koulukoti (pv)",
          "Kuljetusetu (pv)",
          "Ulkomaanjaksot (pv)",
          "Muu kuin vaikeimmin kehitysvammainen (pv)",
          "Vaikeimmin kehitysvammainen (pv)",
          "Pidennetty päättymispäivä"
        ))
      }

      "Suoritustiedot näkyvät ammatillisen järjestämisluvan suorituksella" in {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.find(_._1 == "Ammatillisen järjestämislupa").get
        verifyOppijanRows(KoskiSpecificMockOppijat.tuva,
          Seq(
            Map(
              "Opiskeluoikeuden oid" -> "",
              "Lähdejärjestelmä" -> None,
              "Koulutustoimija" -> "Helsingin kaupunki",
              "Oppilaitoksen nimi" -> "Stadin ammatti- ja aikuisopisto",
              "Toimipiste" -> "Stadin ammatti- ja aikuisopisto",
              "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
              "Päivitetty" -> LocalDate.now(),
              "Yksilöity" -> true,
              "Oppijan oid" -> KoskiSpecificMockOppijat.tuva.oid,
              "Hetu" -> KoskiSpecificMockOppijat.tuva.hetu,
              "Sukunimi" -> KoskiSpecificMockOppijat.tuva.sukunimi,
              "Etunimet" -> KoskiSpecificMockOppijat.tuva.etunimet,
              "Opiskeluoikeuden alkamispäivä" -> Some(LocalDate.of(2021, 8, 1)),
              "Opiskeluoikeuden viimeisin tila" -> Some("valmistunut"),
              "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna, valmistunut",
              "Päätason suoritusten nimet" -> Some("Tutkintokoulutukseen valmentava koulutus"),
              "Opiskeluoikeuden päättymispäivä" -> Some(LocalDate.of(2021, 12, 31)),
              "Rahoitukset" -> "1, 1",
              "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
              "Järjestämislupa" -> Some("Ammatillisen koulutuksen järjestämislupa (TUVA)"),
              "Maksuttomuus" -> Some("2021-08-01 – "),
              "Oikeutta maksuttomuuteen pidennetty" -> None,
              "Majoitus (pv)" -> Some(0),
              "Sisäoppilaitosmainen majoitus (pv)" -> Some(0),
              "Vaativan erityisen tuen yhteydessä järjestettävä majoitus (pv)" -> Some(0),
              "Erityinen tuki (pv)" -> Some(0),
              "Vaativat erityisen tuen tehtävä (pv)" -> Some(0),
              "Ulkomaanjaksot (pv)" -> Some(0),
              "Vaikeasti vammaisten opetus (pv)" -> Some(0),
              "Vammainen ja avustaja (pv)" -> Some(0),
              "Osa-aikaisuusjaksot (prosentit)" -> None,
              "Osa-aikaisuus keskimäärin (%)" -> Some(100.0),
              "Vankilaopetuksessa (pv)" -> Some(0),
              "Koulutusvienti" -> Some(false),
              "Pidennetty päättymispäivä" -> Some(false)
            )
          ),
          oppiaineetRowsWithColumns
        )
      }

      "Suoritustiedot näkyvät perusopetuksen järjestämisluvan suorituksella" in {
        lazy val (title, oppiaineetRowsWithColumns) = titleAndRowsWithColumns.find(_._1 == "Perusopetuksen järjestämislupa").get
        verifyOppijanRows(KoskiSpecificMockOppijat.tuvaPerus,
          Seq(
            Map(
              "Opiskeluoikeuden oid" -> "",
              "Lähdejärjestelmä" -> None,
              "Koulutustoimija" -> "Helsingin kaupunki",
              "Oppilaitoksen nimi" -> "Stadin ammatti- ja aikuisopisto",
              "Toimipiste" -> "Stadin ammatti- ja aikuisopisto",
              "Opiskeluoikeuden tunniste lähdejärjestelmässä" -> None,
              "Päivitetty" -> LocalDate.now(),
              "Yksilöity" -> true,
              "Oppijan oid" -> KoskiSpecificMockOppijat.tuvaPerus.oid,
              "Hetu" -> KoskiSpecificMockOppijat.tuvaPerus.hetu,
              "Sukunimi" -> KoskiSpecificMockOppijat.tuvaPerus.sukunimi,
              "Etunimet" -> KoskiSpecificMockOppijat.tuvaPerus.etunimet,
              "Opiskeluoikeuden alkamispäivä" -> Some(LocalDate.of(2021, 8, 1)),
              "Opiskeluoikeuden viimeisin tila" -> Some("lasna"),
              "Opiskeluoikeuden tilat aikajakson aikana" -> "lasna",
              "Päätason suoritusten nimet" -> Some("Tutkintokoulutukseen valmentava koulutus"),
              "Opiskeluoikeuden päättymispäivä" -> None,
              "Rahoitukset" -> "1, 1",
              "Läsnä/valmistunut-rahoitusmuodot syötetty" -> true,
              "Järjestämislupa" -> Some("Perusopetuksen järjestämislupa (TUVA)"),
              "Maksuttomuus" -> Some("2021-08-01 – "),
              "Oikeutta maksuttomuuteen pidennetty" -> None,
              "Majoitusetu (pv)" -> Some(395),
              "Sisäoppilaitosmainen majoitus (pv)" -> Some(395),
              "Erityisen tuen päätökset (pv)" -> Some(426),
              "Koulukoti (pv)" -> Some(395),
              "Kuljetusetu (pv)" -> Some(395),
              "Ulkomaanjaksot (pv)" -> Some(0),
              "Muu kuin vaikeimmin kehitysvammainen (pv)" -> Some(426),
              "Vaikeimmin kehitysvammainen (pv)" -> Some(0),
              "Pidennetty päättymispäivä" -> Some(false)
            )
          ),
          oppiaineetRowsWithColumns
        )
      }
    }
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
}
