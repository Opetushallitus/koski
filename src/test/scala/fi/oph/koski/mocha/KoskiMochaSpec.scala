package fi.oph.koski.mocha

import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.log.Logging
import org.scalatest.{FreeSpec, Matchers}

import scala.sys.process._

class KoskiMochaSpec extends FreeSpec with Matchers with Logging {
  "Mocha tests" in {
    SharedJetty.start
    log("running mocha-phantom")
    val command = Seq("scripts/mocha-phantom-test.sh", SharedJetty.baseUrl + "/test/runner.html")
    val res = command.!
    if (res != 0) {
      log("Mocha test result written to web/target/test-report.xml")
    }
    res should equal (0)
  }

  private def log(x: String) = logger.info(x)
}