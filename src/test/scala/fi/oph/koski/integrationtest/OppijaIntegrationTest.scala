package fi.oph.koski.integrationtest

import fi.oph.koski.json.Json
import fi.oph.koski.schema.{Oppija, TaydellisetHenkilötiedot}
import org.scalatest.{FreeSpec, Matchers, Tag}

class OppijaIntegrationTest extends FreeSpec with Matchers with KoskidevHttpSpecification {
  val testOid = "1.2.246.562.24.51633620848"

  "Oppijan henkilötiedot, kansalaisuus ja äidinkieli" taggedAs(KoskiDevEnvironment) in {
    get("api/oppija/" + testOid, headers = authHeaders) {
      verifyResponseStatus(200)
      val oppija = Json.read[Oppija](response.body)
      val henkilö = oppija.henkilö.asInstanceOf[TaydellisetHenkilötiedot]
      henkilö.oid should equal(testOid)
      (henkilö.kansalaisuus.get)(0).koodiarvo should equal("246")
      henkilö.äidinkieli.get.koodiarvo should equal("FI")
    }
  }
}