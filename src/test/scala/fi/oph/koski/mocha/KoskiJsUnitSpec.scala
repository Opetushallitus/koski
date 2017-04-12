package fi.oph.koski.mocha

import java.io.{InputStream, OutputStream}

import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.log.Logging
import org.scalatest.{FreeSpec, Matchers}

import scala.sys.process._

class KoskiJsUnitSpec extends KoskiCommandLineSpec {
  "JS unit tests" in {
    logger.info("running js-unit-tests")
    runTestCommand(Seq("make", "js-unit-test"))
  }
}