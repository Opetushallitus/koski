package fi.oph.koski.valpasfront.jest

import fi.oph.koski.mocha.KoskiCommandLineSpec
import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

abstract class ValpasFrontSpecBase extends AnyFreeSpec with KoskiCommandLineSpec {
  def chunkNumber: Int
  def numberOfChunks: Int

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    runTestCommand("valpas-front", Seq(
      "scripts/valpas-front-test.sh",
      sharedJetty.hostUrl,
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
  def numberOfChunks = 6
}

class ValpasFrontSpec2 extends ValpasFrontSpecBase {
  def chunkNumber = 2
  def numberOfChunks = 6
}

class ValpasFrontSpec3 extends ValpasFrontSpecBase {
  def chunkNumber = 3
  def numberOfChunks = 6
}

class ValpasFrontSpec4 extends ValpasFrontSpecBase {
  def chunkNumber = 4
  def numberOfChunks = 6
}

class ValpasFrontSpec5 extends ValpasFrontSpecBase {
  def chunkNumber = 5
  def numberOfChunks = 6
}

class ValpasFrontSpec6 extends ValpasFrontSpecBase {
  def chunkNumber = 6
  def numberOfChunks = 6
}

object ValpasFrontTag extends Tag("valpasfront")
