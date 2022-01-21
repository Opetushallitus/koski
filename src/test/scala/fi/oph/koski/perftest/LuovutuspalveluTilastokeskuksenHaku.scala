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
  val alkupäivä = LocalDate.now.minusMonths(1).toString
  def operation(x: Int) = {
    List(Operation(uri = s"api/luovutuspalvelu/haku?v=1&opiskeluoikeusAlkanutAikaisintaan=${alkupäivä}", headers = authHeaders(tilastokeskusUser)))
  }
}
