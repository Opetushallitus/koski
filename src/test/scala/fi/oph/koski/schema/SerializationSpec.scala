package fi.oph.koski.schema

import fi.oph.koski.documentation.{AmmatillinenExampleData, Examples}
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import org.json4s.MappingException
import org.scalatest.{FunSpec, Matchers}

private[schema] case class Application(user: Option[User])

private[schema] case class User(firstName: String, lastName: String)

class SerializationSpec extends FunSpec with Matchers with Logging {
  describe("Serialization / deserialization") {
    describe("Optional field") {
      it("Success case") {
        Json.read[Application]("""{"user": {"firstName": "John", "lastName": "Doe"}}""")
      }
      it("When deserialization fails -> throw exception") {
        intercept[MappingException] {
          Json.read[Application]("""{"user": {"firstName": "John"}}""")
        }
      }
    }
    it("Tunnustaminen") {
      val jsonString = Json.write(AmmatillinenExampleData.tunnustettu)
      val tunnustettu = Json.read[OsaamisenTunnustaminen](jsonString)
      tunnustettu should(equal(AmmatillinenExampleData.tunnustettu))
    }

    describe("Examples") {
      Examples.examples.foreach { example =>
        it(example.name) {
          val jsonString = Json.write(example.data)
          val oppija = Json.read[Oppija](jsonString)
          oppija should(equal(example.data))
          logger.info(example.name + " ok")
        }
      }
    }
    describe("LocalizedString") {
      it("Serialized/deserializes cleanly") {
        val string: LocalizedString = LocalizedString.finnish("rölli")
        string.values.foreach { x: AnyRef => {} } // <- force lazy val to evaluate
        val jsonString = Json.write(string)
        jsonString should equal("""{"fi":"rölli"}""")
      }
    }

    describe("Suoritukset") {
      Examples.examples.foreach { e =>
        it(e.name + " serialisoituu") {
          val kaikkiSuoritukset: Seq[Suoritus] = e.data.opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.rekursiivisetOsasuoritukset))

          kaikkiSuoritukset.foreach { s =>
            val jsonString = Json.write(s)
            val suoritus = Json.read[Suoritus](jsonString)
            suoritus should (equal(s))
          }
        }
      }
    }
  }
}
