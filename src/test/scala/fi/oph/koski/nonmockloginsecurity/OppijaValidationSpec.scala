package fi.oph.koski.nonmockloginsecurity

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.config.KoskiApplication.defaultConfig
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec

import scala.io.Source

class OppijaValidationSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  override def defaultKoskiApplication = KoskiApplication(defaultConfig.withValue(
    "login.security", fromAnyRef("not-mock")
  ))

  "Kentät cleanForTesting ja ignoreKoskiValidator" - {
    "Vaikka kentät määritelty, muu kuin local-ympäristö ei ota vastaan dataa validoimatta" in {
      val json = JsonMethods.parse(Source.fromFile("src/test/resources/rikkinäinen_opiskeluoikeus.json").mkString)
      putOppija(json, headers = authHeaders() ++ jsonContent) {
        verifyResponseStatus(400,  ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*unexpectedProperty.*".r))
      }
    }
  }
}
