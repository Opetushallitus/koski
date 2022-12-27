package fi.oph.koski.e2e

import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.freespec.AnyFreeSpec


class KoskiFrontSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  // Oletuksena vain yksi shardi, jotta kaikki testit menevät kerralla ajoon
  def shardIndex: String = scala.util.Properties.envOrElse("PLAYWRIGHT_SHARD_INDEX", "1")
  def shardTotal: String = scala.util.Properties.envOrElse("PLAYWRIGHT_SHARD_TOTAL", "1")
  "Koski integration tests" in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    runTestCommand("koski-integration-tests", Seq(
      "scripts/koski-integration-test.sh",
      sharedJetty.hostUrl,
      shardIndex,
      shardTotal
    ))
  }
}
