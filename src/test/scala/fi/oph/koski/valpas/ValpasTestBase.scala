package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

trait ValpasTestBase extends FreeSpec with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    val fixtureCreator = KoskiApplicationForTests.fixtureCreator

    ValpasMockUsers.mockUsersEnabled = true
    fixtureCreator.resetFixtures(fixtureCreator.valpasFixtureState)
  }
}

trait ValpasHttpTestBase extends HttpTester with LocalJettyHttpSpecification;
