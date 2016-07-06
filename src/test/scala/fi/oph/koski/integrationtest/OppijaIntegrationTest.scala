package fi.oph.koski.integrationtest

import fi.oph.koski.api.{OpiskeluoikeusTestMethodsAmmatillinen, SearchTestMethods}
import fi.oph.koski.json.Json
import fi.oph.koski.schema.{OidHenkilö, Oppija, TäydellisetHenkilötiedot, YlioppilastutkinnonOpiskeluoikeus}
import org.scalatest.{FreeSpec, Matchers}

// This test is run against the Koski application deployed in the KoskiDev test environment.
class OppijaIntegrationTest extends FreeSpec with Matchers with KoskidevHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods {
  val testOid = "1.2.246.562.24.51633620848"

  "Oppijan henkilötiedot, kansalaisuus ja äidinkieli" taggedAs(KoskiDevEnvironment) in {
    // This makes sure that our server is running, can authenticate a user, can insert data into the database and
    // return results, i.e. is up and running.

    putOpiskeluOikeus(defaultOpiskeluoikeus, OidHenkilö(testOid)) {
      verifyResponseStatus(200)
    }
    authGet("api/oppija/" + testOid) {
      verifyResponseStatus(200)
      val oppija = Json.read[Oppija](response.body)
      val henkilö = oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot]
      henkilö.oid should equal(testOid)
      (henkilö.kansalaisuus.get)(0).koodiarvo should equal("246")
      henkilö.äidinkieli.get.koodiarvo should equal("FI")

      oppija.opiskeluoikeudet.length should be >= 1
    }
  }
}