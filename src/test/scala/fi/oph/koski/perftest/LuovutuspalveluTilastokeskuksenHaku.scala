package fi.oph.koski.perftest

import fi.oph.koski.koskiuser.MockUsers

import java.time.LocalDate

object LuovutuspalveluTilastokeskuksenHaku extends App {
  PerfTestRunner.executeTest(LuovutuspalveluTilastokeskuksenHakuScenario)
}

object LuovutuspalveluTilastokeskuksenHakuScenario extends PerfTestScenario {
  override def defaultUser = MockUsers.tilastokeskusKäyttäjä
  val alkupäivä = LocalDate.now.minusMonths(1).toString
  def operation(x: Int) = List(Operation(uri = s"api/luovutuspalvelu/haku&opiskeluoikeusAlkanutViimeistään=${alkupäivä}"))
}
