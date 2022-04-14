package fi.oph.koski.mocha

import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

class KoskiMochaSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  "Mocha tests" taggedAs(MochaTag) in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", sharedJetty.baseUrl + "/test/runner.html"))
  }
}

object MochaTag extends Tag("mocha")
