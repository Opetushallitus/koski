package fi.oph.koski.mocha

import java.io.{InputStream, OutputStream}

import fi.oph.koski.log.Logging
import org.scalatest.{FreeSpec, Matchers}

import scala.sys.process._

trait KoskiCommandLineSpec extends FreeSpec with Matchers with Logging {
  def runTestCommand(command: Seq[String]): Unit = {
    val io = new ProcessIO(_.close, pipeTo(System.out), pipeTo(System.err))

    val res: Process = command.run(io)
    if (res.exitValue() != 0) {
      logger.info("JS unit tests failed")
    }
    res.exitValue() should equal (0)
  }

  private def pipeTo(dest: OutputStream)(is: InputStream): Unit = {
    Iterator.continually (is.read).takeWhile(_ != -1).foreach(dest.write)
    is.close()
  }
}
