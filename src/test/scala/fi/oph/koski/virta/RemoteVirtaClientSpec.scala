package fi.oph.koski.virta

import fi.oph.koski.KoskiApplicationForTests
import org.scalatest.{FreeSpec, Matchers}

class RemoteVirtaClientSpec extends FreeSpec with Matchers {
  private val client = RemoteVirtaClient(VirtaConfig.fromConfig(KoskiApplicationForTests.config))

  "RemoteVirtaClient" - {
    "Ei hae mitään tyhjillä hakuehdoilla" in {
      client.opintotiedotMassahaku(Nil) should equal(None)
    }
  }
}
