package fi.oph.koski.valpas.jest

import fi.oph.koski.SharedJetty
import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

abstract class ValpasFrontSpecBase extends AnyFreeSpec with KoskiCommandLineSpec {
  def chunkNumber: Int
  def numberOfChunks: Int

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    SharedJetty.start()
    runTestCommand("valpas-front", Seq(
      "scripts/valpas-front-test.sh",
      SharedJetty.hostUrl,
      chunkNumber.toString,
      numberOfChunks.toString,
    ))
  }
}

class ValpasFrontSpec extends ValpasFrontSpecBase {
  def chunkNumber = 1
  def numberOfChunks = 1
}

class ValpasFrontSpec1 extends ValpasFrontSpecBase {
  def chunkNumber = 1
  def numberOfChunks = 2
}

class ValpasFrontSpec2 extends ValpasFrontSpecBase {
  def chunkNumber = 2
  def numberOfChunks = 2
}

object ValpasFrontTag extends Tag("valpasfront")
