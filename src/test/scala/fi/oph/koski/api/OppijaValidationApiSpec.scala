package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.opiskeluoikeus.ValidationResult
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppijaValidationApiSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods with Matchers {
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
      val oo = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid)
      authGet("api/opiskeluoikeus/validate/" + oo.oid.get) {
        verifyResponseStatusOk()
        val result = JsonSerializer.parse[ValidationResult](body)
        checkValidity(result)
      }
    }

    def checkValidity(result: ValidationResult) = {
      println(result.henkilöOid)

      result.henkilöOid match {
        case KoskiSpecificMockOppijat.tunnisteenKoodiarvoPoistettu.oid => result.errors.map(_.key) should equal(List("badRequest.validation.jsonSchema"))
        case KoskiSpecificMockOppijat.montaKoulutuskoodiaAmis.oid => result.errors.map(_.key) should equal(List("badRequest.validation.jsonSchema"))
        case KoskiSpecificMockOppijat.kelaRikkinäinenOpiskeluoikeus.oid => result.errors.map(_.key) should equal(List("badRequest.validation.jsonSchema"))
        case _ => result.errors should equal(Nil)
      }

      println(result.henkilöOid + " ok")
    }

    "Forbidden for non-root users" in {
      val oo = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid)
      authGet("api/opiskeluoikeus/validate/" + oo.oid.get, user = MockUsers.kalle) {
        verifyResponseStatus(403, Nil)
      }
    }
  }
}
