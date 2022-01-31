package fi.oph.koski.perftest

import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.perftest.LocalYtrFetchKoesuoritusScenario.{Headers, defaultUser, kansalainenAuthHeaders}

import java.time.LocalDate

object LuovutuspalveluTilastokeskuksenHaku extends App {
  PerfTestRunner.executeTest(LuovutuspalveluTilastokeskuksenHakuScenario)
}

object LuovutuspalveluTilastokeskuksenHakuScenario extends PerfTestScenario {
  def tilastokeskusUser = new UserWithPassword {
    override def username = requiredEnv("TILASTOKESKUS_USER")
    override def password = requiredEnv("TILASTOKESKUS_PASS")
  }
  override def defaultUser = tilastokeskusUser
  override def maximumExpectedDurationMs = 10000
  def operation(x: Int) = {
    List(Operation(uri = s"api/luovutuspalvelu/haku?v=1&opiskeluoikeusAlkanutAikaisintaan=2021-01-01&opiskeluoikeusAlkanutViimeistään=2021-12-31")) // headers = authHeaders(tilastokeskusUser) ?
  }
}
