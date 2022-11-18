package fi.oph.koski.e2e

import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.freespec.AnyFreeSpec

class KoskiFrontSpec extends AnyFreeSpec with KoskiCommandLineSpec {

  "Koski integration tests" in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    runTestCommand("koski-integration-tests", Seq(
      "scripts/koski-integration-test.sh",
      sharedJetty.hostUrl
    ))
  }
}
