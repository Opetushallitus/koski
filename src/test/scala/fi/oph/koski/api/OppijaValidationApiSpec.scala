package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.opiskeluoikeus.ValidationResult
import org.scalatest.{FreeSpec, Matchers}

class OppijaValidationApiSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers {
  override def defaultUser = MockUsers.paakayttaja

  "Validation of stored data using the validation API" - {
    "Validate all - fast" in {
      resetFixtures
      authGet("api/opiskeluoikeus/validate") {
        verifyResponseStatusOk()
        val results = JsonSerializer.parse[List[ValidationResult]](body)
        results.length should be >= 0
        results.foreach(checkValidity)
      }
    }
    "Validate all - with person and history data" in {
      resetFixtures
      authGet("api/opiskeluoikeus/validate?henkilö=true&history=true") {
        verifyResponseStatusOk()
        val results = JsonSerializer.parse[List[ValidationResult]](body)
        results.length should be >= 0
        results.foreach(checkValidity)
      }
    }
    "Validate single" in {
      val oo = lastOpiskeluoikeus(MockOppijat.eero.oid)
      authGet("api/opiskeluoikeus/validate/" + oo.oid.get) {
        verifyResponseStatusOk()
        val result = JsonSerializer.parse[ValidationResult](body)
        checkValidity(result)
      }
    }

    def checkValidity(result: ValidationResult) = {
      println(result.henkilöOid)
      result.errors should equal(Nil)
      println(result.henkilöOid + " ok")
    }

    "Forbiddon for non-root users" in {
      val oo = lastOpiskeluoikeus(MockOppijat.eero.oid)
      authGet("api/opiskeluoikeus/validate/" + oo.oid.get, user = MockUsers.kalle) {
        verifyResponseStatus(403, Nil)
      }
    }
  }
}
