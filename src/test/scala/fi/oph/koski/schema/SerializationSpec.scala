package fi.oph.koski.schema

import fi.oph.koski.documentation.{AmmatillinenExampleData, Examples}
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import org.json4s.MappingException
import org.scalatest.{FreeSpec, Matchers}

private[schema] case class Application(user: Option[User])

private[schema] case class User(firstName: String, lastName: String)

class SerializationSpec extends FreeSpec with Matchers with Logging {
  import KoskiSchema.deserializationContext
  "Serialization / deserialization" - {
    "Optional field" - {
      // TODO: use schema-based
      "Success case" in {
        Json.read[Application]("""{"user": {"firstName": "John", "lastName": "Doe"}}""")
      }
      "When deserialization fails -> throw exception" in {
        intercept[MappingException] {
          Json.read[Application]("""{"user": {"firstName": "John"}}""")
        }
      }
    }
    "Tunnustaminen" in {
      val jsonString = Json.write(AmmatillinenExampleData.tunnustettu)
      val tunnustettu = SchemaBasedJsonDeserializer.extract[OsaamisenTunnustaminen](jsonString).right.get
      tunnustettu should(equal(AmmatillinenExampleData.tunnustettu))
    }

    "Examples" - {
      Examples.examples.foreach { example =>
        example.name in {
          val jsonString = Json.write(example.data)
          val oppija = SchemaBasedJsonDeserializer.extract[Oppija](jsonString).right.get
          oppija should(equal(example.data))
          logger.info(example.name + " ok")
        }
      }
    }
    "LocalizedString" - {
      "Serialized/deserializes cleanly" in {
        val string: LocalizedString = LocalizedString.finnish("rölli")
        string.values.foreach { x: AnyRef => {} } // <- force lazy val to evaluate
        val jsonString = Json.write(string)
        jsonString should equal("""{"fi":"rölli"}""")
      }
    }

    "Suoritukset" - {
      Examples.examples.foreach { e =>
        (e.name + " serialisoituu") in {
          val kaikkiSuoritukset: Seq[Suoritus] = e.data.opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.rekursiivisetOsasuoritukset))

          kaikkiSuoritukset.foreach { s =>
            val jsonString = Json.write(s)
            val suoritus = SchemaBasedJsonDeserializer.extract[Suoritus](jsonString).right.get
            suoritus should (equal(s))
          }
        }
      }
    }
  }
}
