package fi.oph.koski.perftest

import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}

import java.time.LocalDate

object LuovutuspalveluTilastokeskuksenHaku extends App {
  PerfTestRunner.executeTest(LuovutuspalveluTilastokeskuksenHakuScenario)
}

object LuovutuspalveluTilastokeskuksenHakuScenario extends PerfTestScenario {
  override def defaultUser = new UserWithPassword {
    override def username = requiredEnv("TILASTOKESKUS_USER")
    override def password = requiredEnv("TILASTOKESKUS_PASS")
  }
  val alkupäivä = LocalDate.now.minusMonths(1).toString
  def operation(x: Int) = List(Operation(uri = s"api/luovutuspalvelu/haku&opiskeluoikeusAlkanutViimeistään=${alkupäivä}"))
}
