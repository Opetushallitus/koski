package fi.oph.tor

import fi.oph.tor.jettylauncher.SharedJetty
import org.scalatest.{Matchers, FreeSpec}
import scala.sys.process._

class TorMochaSpec extends FreeSpec with Matchers {
  "Mocha-testit" - {
    SharedJetty.start
    val pb = Seq("web/node_modules/mocha-phantomjs/bin/mocha-phantomjs", "-R", "spec", SharedJetty.baseUrl + "/test/runner.html")
    val res = pb.!
    res should equal (0)
  }
}