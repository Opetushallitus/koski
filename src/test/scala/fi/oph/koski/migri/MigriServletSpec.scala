package fi.oph.koski.migri

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.MockUsers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.util.matching.Regex

class MigriServletSpec extends AnyFreeSpec with KoskiHttpSpec with HttpSpecification with Matchers {
  "MigriServlet" - {
    "Palauttaa json-objektin hetulla" in {
      post(
        uri = "api/luovutuspalvelu/migri/hetu",
        body = "{\"hetu\": \"211097-402L\"}",
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatusOk()
        response.body should include ("1.2.246.562.24.00000000026")
      }
    }

    "Palauttaa json-objektin henkilo-oidilla" in {
      post(
        uri = "api/luovutuspalvelu/migri/oid",
        body = "{\"oid\": \"1.2.246.562.24.00000000026\"}",
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatusOk()
        response.body should include ("211097-402L")
      }
    }

    "Palauttaa valintatulokset json-objektin henkilo-oideilla" in {
      post(
        uri = "api/luovutuspalvelu/migri/valinta/oid",
        body = "{\"oids\": [\"1.2.246.562.24.51986460849\"]}",
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatusOk()
        response.body should equal("{\"oids\":[\"1.2.246.562.24.51986460849\"],\"username\":\"Lasse\",\"password\":\"Lasse\"}")
      }
    }

    "Palauttaa valintatulokset json-objektin hetuilla" in {
      post(
        uri = "api/luovutuspalvelu/migri/valinta/hetut",
        body = "{\"hetut\": [\"170249-378D\"]}",
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatusOk()
        response.body should equal("{\"hetus\":[\"170249-378D\"],\"username\":\"Lasse\",\"password\":\"Lasse\"}")
      }
    }
  }
}
