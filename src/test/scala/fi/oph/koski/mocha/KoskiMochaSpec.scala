package fi.oph.koski.mocha

import java.io.{InputStream, OutputStream}

import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.log.Logging
import org.scalatest.{FreeSpec, Matchers}

import scala.sys.process._

class KoskiMochaSpec extends FreeSpec with Matchers with Logging {
  "Mocha tests" in {
    SharedJetty.start
    log("running mocha-phantom")
    val command = Seq("scripts/mocha-phantom-test.sh", SharedJetty.baseUrl + "/test/runner.html")

    val io = new ProcessIO(_.close, pipeTo(System.out), pipeTo(System.err))

    val res: Process = command.run(io)
    if (res.exitValue() != 0) {
      log("Mocha tests failed")
    }
    res.exitValue() should equal (0)
  }

  private def log(x: String) = logger.info(x)

  private def pipeTo(dest: OutputStream)(is: InputStream): Unit = {
    Iterator.continually (is.read).takeWhile(_ != -1).foreach(dest.write)
    is.close
  }

}