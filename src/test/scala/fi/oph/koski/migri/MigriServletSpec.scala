package fi.oph.koski.migri

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.MockUsers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MigriServletSpec extends AnyFreeSpec with KoskiHttpSpec with HttpSpecification with Matchers {
  "MigriServlet" - {
    "Palauttaa json-objektin" in {
      post(
        uri = "api/luovutuspalvelu/migri/valinta/oid",
        body = "{\"oids\": [\"1.2.246.562.24.51986460849\"]}",
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatusOk()
        response.body should equal("{\"oids\":[\"1.2.246.562.24.51986460849\"],\"username\":\"Lasse\",\"password\":\"Lasse\"}")
      }
    }
  }
}
