package fi.oph.koski.valpas

import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers, ValpasSession}
import fi.oph.koski.{KoskiApplicationForTests, LocalJettyHttpSpec}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

trait ValpasTestBase extends AnyFreeSpec with LocalJettyHttpSpec with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FixtureUtil.resetMockData(KoskiApplicationForTests)
  }

  override protected def afterAll(): Unit = {
    FixtureUtil.clearMockData(KoskiApplicationForTests)
    super.afterAll()
  }

  override def defaultUser: ValpasMockUser = ValpasMockUsers.valpasJklNormaalikoulu

  protected def session(user: ValpasMockUser): ValpasSession = user.toValpasSession(KoskiApplicationForTests.käyttöoikeusRepository)

  protected val defaultSession: ValpasSession = session(defaultUser)

  override def kansalainenLoginHeaders(hetu: String): List[(String, String)] = {
    get("cas/valpas/oppija", headers = List("hetu" -> hetu, "security" -> "mock")) {
      verifyResponseStatusOk(302)
      val cookie = response.headers("Set-Cookie").find(x => x.startsWith("koskiOppija")).get
      List("Cookie" -> cookie)
    }
  }
}
