package fi.oph.koski.perftest

import java.time.{LocalDate}

object LuovutuspalveluTilastokeskuksenHaku extends App {
  PerfTestRunner.executeTest(LuovutuspalveluTilastokeskuksenHakuScenario)
}

object LuovutuspalveluTilastokeskuksenHakuScenario extends PerfTestScenario {
  val alkupäivä = LocalDate.now.minusMonths(1).toString
  def operation(x: Int) = List(Operation(uri = s"api/luovutuspalvelu/haku&opiskeluoikeusAlkanutViimeistään=${alkupäivä}"))
}
