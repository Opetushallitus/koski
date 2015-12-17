package fi.oph.tor

import fi.oph.tor.jettylauncher.SharedJetty
import org.scalatest.{Matchers, FreeSpec}
import scala.sys.process._

class TorMochaSpec extends FreeSpec with Matchers {
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

  private def log(x: String) = println(s"********** mocha test: $x **********")
}