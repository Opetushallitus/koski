package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.Json
import fi.oph.koski.opiskeluoikeus.ValidationResult
import org.scalatest.{FreeSpec, Matchers}

class OppijaValidationApiSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers {
  "Validation of stored data using the validation API" - {
    "Validate all - fast" in {
      resetFixtures
      authGet("api/opiskeluoikeus/validate") {
        verifyResponseStatus(200)
        val results = Json.read[List[ValidationResult]](body)
        results.length should be >= 0
        results.foreach(checkValidity)
      }
    }
    "Validate all - with person and history data" in {
      resetFixtures
      authGet("api/opiskeluoikeus/validate?henkilö=true&history=true") {
        verifyResponseStatus(200)
        val results = Json.read[List[ValidationResult]](body)
        results.length should be >= 0
        results.foreach(checkValidity)
      }
    }
    "Validate single" in {
      val oo = lastOpiskeluoikeus(MockOppijat.eero.oid)
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
