package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

trait MuuAmmatillinenSpecification[T <: AmmatillinenPäätasonSuoritus] extends FreeSpec with KoskiHttpSpec with MuuAmmatillinenTestMethods[T] {
  "Muu ammatillinen" - {
    "Validi opiskeluoikeus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Suoritus puuttuu" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = Nil))(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r)))
    }
  }
}
