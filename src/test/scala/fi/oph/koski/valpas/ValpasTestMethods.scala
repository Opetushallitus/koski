package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester
import fi.oph.koski.util.Wait
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

trait ValpasTestMethods extends FreeSpec with HttpTester with LocalJettyHttpSpecification with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    val fixtureCreator = KoskiApplicationForTests.fixtureCreator
    val raportointikantaService = KoskiApplicationForTests.raportointikantaService

    ValpasMockUsers.mockUsersEnabled = true
    fixtureCreator.resetFixtures(fixtureCreator.valpasFixtureState)
    raportointikantaService.dropAndCreateSchema()
    raportointikantaService.loadRaportointikanta(true)

    Wait.until(loadComplete)
  }

  private def loadComplete = authGet("api/raportointikanta/status") {
    val isComplete = (JsonMethods.parse(body) \ "public" \ "isComplete").extract[Boolean]
    val isLoading = (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
    isComplete && !isLoading
  }
}
