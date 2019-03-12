package fi.oph.koski.raportointikanta

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester

trait RaportointikantaTestMethods extends HttpTester with LocalJettyHttpSpecification {

  def loadRaportointikantaFixtures[A] = {
    authGet("api/raportointikanta/clear") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/opiskeluoikeudet") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/henkilot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/organisaatiot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/koodistot") { verifyResponseStatusOk() }
  }
}
