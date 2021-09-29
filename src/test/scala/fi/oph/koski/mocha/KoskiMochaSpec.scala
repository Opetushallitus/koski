package fi.oph.koski.mocha

import fi.oph.koski.SharedJetty
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

class KoskiMochaSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  "Mocha tests" taggedAs(MochaTag) in {
    SharedJetty.start()
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", SharedJetty.baseUrl + "/test/runner.html"))
  }
}

object MochaTag extends Tag("mocha")
