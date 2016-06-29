package fi.oph.koski.integrationtest

import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.json.Json
import fi.oph.koski.schema.{OidHenkilö, Oppija, TäydellisetHenkilötiedot}
import org.scalatest.{FreeSpec, Matchers}

class OppijaIntegrationTest extends FreeSpec with Matchers with KoskidevHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val testOid = "1.2.246.562.24.51633620848"

  "Oppijan henkilötiedot, kansalaisuus ja äidinkieli" taggedAs(KoskiDevEnvironment) in {
    putOpiskeluOikeus(defaultOpiskeluoikeus, OidHenkilö(testOid), authHeaders) {
      verifyResponseStatus(200)
    }
    get("api/oppija/" + testOid, headers = authHeaders) {
      verifyResponseStatus(200)
      val oppija = Json.read[Oppija](response.body)
      val henkilö = oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot]
      henkilö.oid should equal(testOid)
      (henkilö.kansalaisuus.get)(0).koodiarvo should equal("246")
      henkilö.äidinkieli.get.koodiarvo should equal("FI")
    }
  }
}