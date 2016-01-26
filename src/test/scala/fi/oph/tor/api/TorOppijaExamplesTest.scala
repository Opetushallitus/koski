package fi.oph.tor.api

import fi.oph.tor.json.Json
import fi.oph.tor.schema.TorOppijaExamples
import org.scalatest.{FreeSpec, Matchers}

class TorOppijaExamplesTest extends FreeSpec with Matchers with HttpSpecification {
  "API examples" - {
    TorOppijaExamples.examples.foreach { example =>
      "POST " + example.name in {
        val body = Json.write(example.data).getBytes("utf-8")
        put("api/oppija", body = body, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatus()
          println(example.name + ": OK")
        }
      }
    }
  }
}
