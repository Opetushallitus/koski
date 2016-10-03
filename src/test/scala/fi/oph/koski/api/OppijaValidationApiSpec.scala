package fi.oph.koski.api

import fi.oph.koski.json.Json
import fi.oph.koski.koski.ValidationResult
import fi.oph.koski.oppija.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class OppijaValidationApiSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluOikeusTestMethods with Matchers {
  "Validation of stored data using the validation API" - {
    "Validate all" in {
      resetFixtures
      authGet("api/opiskeluoikeus/validate") {
        verifyResponseStatus(200)
        val results = Json.read[List[ValidationResult]](body)
        results.length should be >= 0
        results.foreach(checkValidity)
      }
    }
    "Validate single" in {
      val oo = lastOpiskeluOikeus(MockOppijat.eero.oid)
      authGet("api/opiskeluoikeus/validate/" + oo.id.get) {
        verifyResponseStatus(200)
        val result = Json.read[ValidationResult](body)
        checkValidity(result)
      }
    }

    def checkValidity(result: ValidationResult) = {
      println(result.henkilöOid)
      result.errors should equal(Nil)
      println(result.henkilöOid + " ok")
    }
  }
}
