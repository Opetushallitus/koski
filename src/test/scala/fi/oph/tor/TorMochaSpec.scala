package fi.oph.tor

import fi.oph.tor.jettylauncher.SharedJetty
import org.scalatest.{Matchers, FreeSpec}
import scala.sys.process._

class TorMochaSpec extends FreeSpec with Matchers {
  "Mocha-testit" - {
    SharedJetty.start
    val command = Seq("scripts/mocha-phantom-test.sh", SharedJetty.baseUrl + "/test/runner.html")
    val res = command.!
    res should equal (0)
  }
}