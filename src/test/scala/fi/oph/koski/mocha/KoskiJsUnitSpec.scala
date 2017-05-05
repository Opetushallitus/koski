package fi.oph.koski.mocha

class KoskiJsUnitSpec extends KoskiCommandLineSpec {
  "JS unit tests" in {
    runTestCommand("js-unit-tests", Seq("make", "js-unit-test"))
  }
}