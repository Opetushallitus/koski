package fi.oph.koski.mocha

import fi.oph.koski.jettylauncher.SharedJetty

class KoskiMochaSpec extends KoskiCommandLineSpec {
  "Mocha tests" in {
    SharedJetty.start
    logger.info("running mocha-phantom")
    runTestCommand(Seq("scripts/mocha-phantom-test.sh", SharedJetty.baseUrl + "/test/runner.html"))
  }
}