package fi.oph.koski.raportointikanta

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot

trait RaportointikantaTestMethods extends HttpTester with LocalJettyHttpSpecification {
  def loadRaportointikantaFixtures[A] = {
    authGet("api/raportointikanta/clear") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/opiskeluoikeudet") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/henkilot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/organisaatiot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/koodistot") { verifyResponseStatusOk() }
  }

  def verifyRaportinLataaminen(apiUrl: String, expectedRaporttiNimi: String): Unit = {
    val queryString1 = s"oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31"
    val queryString2 = "password=dummy&downloadToken=test123"
    authGet(s"$apiUrl?$queryString1&$queryString2") {
      verifyResponseStatusOk()
      val ENCRYPTED_XLSX_PREFIX = Array(0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1).map(_.toByte)
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=$expectedRaporttiNimi&$queryString1")))
    }
  }
}
