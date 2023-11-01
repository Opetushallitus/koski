package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.MuuAmmatillinenTestMethods
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

trait MuuAmmatillinenSpecification[T <: AmmatillinenPäätasonSuoritus] extends AnyFreeSpec with KoskiHttpSpec with MuuAmmatillinenTestMethods[T] {
  "Muu ammatillinen" - {
    "Validi opiskeluoikeus" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Suoritus puuttuu" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = Nil))(verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lessThanMinimumNumberOfItems.*".r)))
    }
  }
}
