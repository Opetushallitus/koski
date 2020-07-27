package fi.oph.koski.mocha

import fi.oph.koski.api.SharedJetty

import org.scalatest.{Tag}

class KoskiMochaSpec extends KoskiCommandLineSpec {
  "Mocha tests" taggedAs(MochaTag) in {
    SharedJetty.start
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", SharedJetty.baseUrl + "/test/runner.html"))
  }
}

object MochaTag extends Tag("mocha")
