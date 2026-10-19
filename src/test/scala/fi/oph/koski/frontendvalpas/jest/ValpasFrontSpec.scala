package fi.oph.koski.frontendvalpas.jest

import fi.oph.koski.mocha.KoskiCommandLineSpec
import fi.oph.koski.util.Futures
import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil
import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

abstract class ValpasFrontSpecBase extends AnyFreeSpec with KoskiCommandLineSpec {
  def chunkNumber: Int
  def numberOfChunks: Int

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    // Ensimmäinen reset KOSKI_SPECIFIC → VALPAS on kylmällä sovelluksella moninkertaisesti hitaampi
    // kuin myöhemmät ja voi ylittää testien 45 s aikarajan, joten tehdään se tässä. Testi, joka käyttää
    // oletustarkastelupäivää ilman forcea, ei tämän jälkeen resetoi itse lainkaan.
    Futures.await(Future(FixtureUtil.resetMockData(KoskiApplicationForTests)), atMost = 5.minutes)
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
