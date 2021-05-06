package fi.oph.koski.valpas

import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import fi.oph.koski.{KoskiApplicationForTests, LocalJettyHttpSpec}
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

trait ValpasTestBase extends FreeSpec with LocalJettyHttpSpec with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FixtureUtil.resetMockData(KoskiApplicationForTests)
  }

  override protected def afterAll(): Unit = {
    FixtureUtil.clearMockData(KoskiApplicationForTests)
    super.afterAll()
  }

  override def defaultUser: ValpasMockUser = ValpasMockUsers.valpasJklNormaalikoulu
}
