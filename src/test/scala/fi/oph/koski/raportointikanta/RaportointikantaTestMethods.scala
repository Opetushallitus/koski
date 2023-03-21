package fi.oph.koski.raportointikanta

import fi.oph.koski.db.RaportointiDatabaseConfig
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.time.LocalDate


trait RaportointikantaTestMethods extends KoskiHttpSpec {
  implicit val formats = DefaultFormats

  val ENCRYPTED_XLSX_PREFIX: Array[Byte] = Array(0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1).map(_.toByte)

  lazy val mainRaportointiDb: RaportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  lazy val tempRaportointiDb: RaportointiDatabase = new RaportointiDatabase(
    new RaportointiDatabaseConfig(KoskiApplicationForTests.config, schema = Temp)
  )

  def reloadRaportointikanta(): Unit = {
    authGet("api/test/raportointikanta/load?force=true&fullReload=true") { verifyResponseStatusOk() }
    Wait.until(loadComplete)
  }

  def updateRaportointikanta(): Unit = {
    authGet("api/test/raportointikanta/load?force=true") { verifyResponseStatusOk() }
    Wait.until(loadComplete)
  }

  def verifyRaportinLataaminen(apiUrl: String, expectedRaporttiNimi: String, expectedFileNamePrefix: String, lang: String = "fi"): Unit = {
    val queryString1 = s"oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&lang=$lang"
    val queryString2 = "password=dummy&downloadToken=test123"
    authGet(s"$apiUrl?$queryString1&$queryString2") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="${expectedFileNamePrefix}_${MockOrganisaatiot.stadinAmmattiopisto}_20160101-20161231.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=$expectedRaporttiNimi&$queryString1")))
    }
  }

  def verifyPerusopetukseVuosiluokkaRaportinLataaminen(queryString: String, apiUrl: String, expectedRaporttiNimi: String, expectedFileNamePrefix: String, vuosiluokka: String = "9", paiva: LocalDate = LocalDate.of(2016, 1, 1)): Unit = {
    val password = "password=dummy&downloadToken=test123"
    authGet(s"$apiUrl?$queryString&$password") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="${expectedFileNamePrefix}_${MockOrganisaatiot.jyväskylänNormaalikoulu}_${vuosiluokka}_${paiva.toString}.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=$expectedRaporttiNimi&$queryString")))
    }
  }

  def loadComplete = authGet("api/raportointikanta/status") {
    val isComplete = (JsonMethods.parse(body) \ "public" \ "isComplete").extract[Boolean]
    val isLoading = (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
    isComplete && !isLoading
  }
}
