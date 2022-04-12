package fi.oph.koski.api

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.config.Environment
import fi.oph.koski.config.KoskiApplication.defaultConfig
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec

import scala.io.Source

class OppijaValidationNonMockEnvSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  defaultConfig.withValue("env", fromAnyRef(Environment.UnitTest)).withValue(
    "opintopolku.virkailija.url", fromAnyRef("https://virkailija.testiopintopolku.fi")
  )

  "Kentät cleanForTesting ja ignoreKoskiValidator" - {
    "Vaikka kentät määritelty, muu kuin mock-ympäristö ei ota vastaan dataa validoimatta" in {
      val json = JsonMethods.parse(Source.fromFile("src/test/resources/rikkinäinen_opiskeluoikeus.json").mkString)
      putOppija(json, headers = authHeaders() ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }
  }
}
