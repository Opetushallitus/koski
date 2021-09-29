package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.Examples.examples
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppijaExamplesTest extends AnyFreeSpec with Matchers with KoskiHttpSpec with Logging {
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
