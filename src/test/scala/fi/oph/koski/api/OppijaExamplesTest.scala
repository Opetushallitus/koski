package fi.oph.koski.api

import fi.oph.koski.documentation.Examples.examples
import fi.oph.common.json.JsonSerializer
import fi.oph.common.log.Logging
import org.scalatest.{FreeSpec, Matchers}

class OppijaExamplesTest extends FreeSpec with Matchers with LocalJettyHttpSpecification with Logging {
  "API examples" - {
    examples.foreach { example =>
      "POST " + example.name in {
        val body = JsonSerializer.writeWithRoot(example.data).getBytes("utf-8")
        put("api/oppija", body = body, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatusOk(example.statusCode)
          logger.info(example.name + ": OK")
        }
      }
    }
  }
}
