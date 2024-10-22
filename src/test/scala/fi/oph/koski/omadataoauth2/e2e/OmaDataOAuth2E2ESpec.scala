package fi.oph.koski.omadataoauth2.e2e

import fi.oph.koski.mocha.KoskiCommandLineSpec
import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL

class OmaDataOAuth2E2ESpec extends AnyFreeSpec with KoskiCommandLineSpec {
  // Oletuksena vain yksi shardi, jotta kaikki testit menevät kerralla ajoon
  def shardIndex: String = scala.util.Properties.envOrElse("PLAYWRIGHT_SHARD_INDEX", "1")
  def shardTotal: String = scala.util.Properties.envOrElse("PLAYWRIGHT_SHARD_TOTAL", "1")

  "OmaDataOAuth2 integration tests" taggedAs(OmaDataOAuth2E2ETag) in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    runTestCommand("omadata-oauth2-e2e-tests", Seq(
      "scripts/omadata-oauth2-e2e-test.sh",
      s"http://host.docker.internal:${sharedJetty.port}",
      shardIndex,
      shardTotal
    ))
  }

  // TODO: TOR-2210: Löytyisiköhän tähän joku siistimpi keino esim. Github actionsin sisällä hakea julkinen IP?
  private def myIP(): String = {
    val whatismyip = new URL("http://checkip.amazonaws.com")
    val in:BufferedReader = new BufferedReader(new InputStreamReader(
      whatismyip.openStream()
    ))
    in.readLine()
  }
}



object OmaDataOAuth2E2ETag extends Tag("omadataoauth2e2e")
