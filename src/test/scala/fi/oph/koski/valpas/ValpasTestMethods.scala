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

    ValpasMockUsers.mockUsersEnabled = true
    fixtureCreator.resetFixtures(fixtureCreator.valpasFixtureState)
  }
}
