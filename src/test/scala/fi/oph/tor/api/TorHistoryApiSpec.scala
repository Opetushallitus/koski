package fi.oph.tor.api

import fi.oph.tor.db.OpiskeluOikeusHistoryRow
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.schema.TorOppija
import org.scalatest.FunSpec

class TorHistoryApiSpec extends FunSpec with OpiskeluOikeusTestMethods {
  SharedJetty.start

  describe("Valideilla tiedoilla") {
    it("palautetaan HTTP 200") {
      resetFixtures {
        putOpiskeluOikeusAjax(Map()) {
          verifyResponseCode(200)
          val oppijaOid = Json.read[String](body)
          authGet("api/oppija/" + oppijaOid) {
            verifyResponseCode(200)
            val opiskeluoikeusId = Json.read[TorOppija](body).opiskeluoikeudet(0).id.get
            authGet("api/opiskeluoikeus/historia/" + opiskeluoikeusId) {
              val historia = Json.read[List[OpiskeluOikeusHistoryRow]](body)
              historia.map(_.versionumero) should equal(List(1,2)) // First one was inserted in fixtures already
            }
          }
        }
      }
    }
  }
}
