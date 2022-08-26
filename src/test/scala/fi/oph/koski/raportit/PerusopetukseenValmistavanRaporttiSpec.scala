package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class PerusopetukseenValmistavanRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods
  with OpiskeluoikeusTestMethodsPerusopetus with BeforeAndAfterAll {

  private lazy val repository = PerusopetukseenValmistavanRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  private lazy val valmistavanRaportti = PerusopetukseenValmistavanRaportti(repository, t)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }


  private val defaultExpectedValmistavaRow: PerusopetukseenValmistavanRaporttiRow = PerusopetukseenValmistavanRaporttiRow(
    opiskeluoikeusOid = "1.2.246.562.15.72335373733",
    lähdejärjestelmä = None,
    lähdejärjestelmänTunniste = None,
    koulutustoimijaNimi = "Jyväskylän yliopisto",
    oppilaitosNimi = "Jyväskylän normaalikoulu",
    toimipisteenNimi = "Jyväskylän normaalikoulu",
    aikaleima = LocalDate.now(),
    yksiloity = true,
    oppijaOid = "1.2.246.562.24.00000000007",
    hetu = Option("220109-784L"),
    sukunimi = "Koululainen",
    etunimet = "Kaisa",
    kansalaisuus = None,
    opiskeluoikeudenAlkamispäivä = Option(LocalDate.parse("2007-08-15")),
    opiskeluoikeudenTila = Option("valmistunut"),
    opiskeluoikeudenTilatAikajaksonAikana = "lasna,loma,lasna,valmistunut",
    suoritustyyppi = "perusopetukseenvalmistavaopetus",
    suorituksenTila = "valmis",
    suorituksenVahvistuspaiva = "2008-06-01",
    läsnäolopäiviäAikajaksonAikana = 274,
    rahoitukset = ""
  )

  "Valmistavan opetuksen raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/valmistavansuoritustietojentarkistus?oppilaitosOid=$jyväskylänNormaalikoulu&alku=2018-01-01&loppu=2022-01-01&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="valmistavaopetus_koski_raportti_${jyväskylänNormaalikoulu}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=valmistavansuoritustietojentarkistus&oppilaitosOid=$jyväskylänNormaalikoulu&alku=2018-01-01&loppu=2022-01-01&lang=fi")))
      }
    }

    "Raportin lataaminen toimii eri lokalisaatiolla" in {
      authGet(s"api/raportit/valmistavansuoritustietojentarkistus?oppilaitosOid=$jyväskylänNormaalikoulu&alku=2018-01-01&loppu=2022-01-01&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="valmistavaopetus_koski_raportti_${jyväskylänNormaalikoulu}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=valmistavansuoritustietojentarkistus&oppilaitosOid=$jyväskylänNormaalikoulu&alku=2018-01-01&loppu=2022-01-01&lang=sv")))
      }
    }

    "Raportti näyttää oikealta" - {
      lazy val report = valmistavanRaportti.buildRows(Seq(MockOrganisaatiot.jyväskylänNormaalikoulu), LocalDate.of(2000, 1, 1), LocalDate.of(2022, 1, 1), t)
      lazy val sheet = valmistavanRaportti.buildDataSheet(report)
      "Sarakkeiden järjestys" in {
        sheet.columnSettings.map(_.title) should equal(Seq(
          "Opiskeluoikeuden oid",
          "Lähdejärjestelmä",
          "Opiskeluoikeuden tunniste lähdejärjestelmässä",
          "Koulutustoimija",
          "Oppilaitoksen nimi",
          "Toimipiste",
          "Päivitetty",
          "Yksilöity",
          "Oppijan oid",
          "Hetu",
          "Sukunimi",
          "Etunimet",
          "Kansalaisuus",
          "Opiskeluoikeuden alkamispäivä",
          "Opiskeluoikeuden viimeisin tila",
          "Opiskeluoikeuden tilat aikajakson aikana",
          "Suorituksen tyyppi",
          "Suorituksen tila",
          "Suorituksen vahvistuspäivä",
          "Läsnäolopäiviä aikajakson aikana",
          "Rahoitukset"
        ))
      }
      "Data näyttää oikealta" in {
        sheet.rows.head should equal ( defaultExpectedValmistavaRow.copy(opiskeluoikeusOid = report.head.opiskeluoikeusOid).productIterator.toList )
      }
    }
  }
}

