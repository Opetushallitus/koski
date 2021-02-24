package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification.externalJettyPort
import fi.oph.koski.api.{LocalJettyHttpSpecification, SharedJetty}
import fi.oph.koski.http.{HttpSpecification, HttpTester}
import fi.oph.koski.log.{AccessLogTester, AuditLogTester, RootLogTester}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

trait ValpasTestBase extends FreeSpec with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    val fixtureCreator = KoskiApplicationForTests.fixtureCreator

    ValpasMockUsers.mockUsersEnabled = true
    fixtureCreator.resetFixtures(fixtureCreator.valpasFixtureState)
  }

  override protected def afterAll(): Unit = {
    val fixtureCreator = KoskiApplicationForTests.fixtureCreator
    ValpasMockUsers.mockUsersEnabled = false
    fixtureCreator.resetFixtures(fixtureCreator.koskiSpecificFixtureState)
  }
}

trait ValpasHttpTestBase extends ValpasTestBase with HttpTester with HttpSpecification {
  override def baseUrl = LocalJettyHttpSpecification.baseUrl
  def defaultUser = ValpasMockUsers.valpasJklNormaalikoulu

  override def beforeAll(): Unit = {
    if (externalJettyPort.isEmpty) SharedJetty.start
    AuditLogTester.setup
    AccessLogTester.setup
    RootLogTester.setup
    super.beforeAll()
  }
}
