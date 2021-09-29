package fi.oph.koski.mocha

import org.scalatest.freespec.AnyFreeSpec

class KoskiJsUnitSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  "JS unit tests" in {
    runTestCommand("js-unit-tests", Seq("make", "js-unit-test"))
  }
}
