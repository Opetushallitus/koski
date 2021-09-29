package fi.oph.koski.virta

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RemoteVirtaClientSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val client = RemoteVirtaClient(VirtaConfig.fromConfig(KoskiApplicationForTests.config))

  "RemoteVirtaClient" - {
    "Ei hae mitään tyhjillä hakuehdoilla" in {
      client.opintotiedotMassahaku(Nil) should equal(None)
    }
  }
}
