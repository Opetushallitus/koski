package fi.oph.koski.mocha

import fi.oph.koski.api.SharedJetty

class KoskiMochaSpec extends KoskiCommandLineSpec {
  "Mocha tests" in {
    SharedJetty.start
    runTestCommand("mocha-chrome", Seq("scripts/mocha-chrome-test.sh", SharedJetty.baseUrl + "/test/runner.html"))
  }
}
