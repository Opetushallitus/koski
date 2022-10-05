package fi.oph.koski.schema

import java.time.LocalDate
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import fi.oph.koski.TestEnvironment
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.Examples
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import fi.oph.scalaschema.extraction.{EmptyString, RegExMismatch, ValidationError}
import org.json4s.JsonAST.JString
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.reflect.runtime.universe.TypeTag

class KoskiOppijaExamplesValidationSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val validator: JsonValidator = JsonSchemaFactory.byDefault.getValidator
  private lazy val schema: JsonNode =  JsonLoader.fromString(KoskiSchema.schemaJsonString)

  "Validation with JSON Schema" - {
    Examples.oppijaExamples.foreach { example =>
      example.name in {
        import scala.collection.JavaConverters._
        val json = JsonLoader.fromString(JsonSerializer.writeWithRoot(example.data))
        val report = validator.validate(schema, json)
        assert(report.isSuccess, "Example \"" + example.name + "\" failed to validate: \n\n" + report.asScala.filter(m => m.getLogLevel.toString == "error").mkString("\n"))
      }
    }
  }

  "Validation with SchemaBasedJsonDeserializer" - {
    "Henkilö" in {
      val oid = "1.2.246.562.24.12345678901"
      testDeserialization(OidHenkilö(oid))
      testDeserialization[Henkilö](OidHenkilö(oid))
      testDeserialization(OidHenkilö(""), Left(List(ValidationError("oid",JString(""),EmptyString()))))
      testDeserialization(OidHenkilö("123"), Left(List(ValidationError("oid",JString("123"),RegExMismatch("""^1\.2\.246\.562\.24\.\d{11}$""")))))
      testDeserialization[Henkilö](TäydellisetHenkilötiedot(oid, Some("123456-7890"), Some(LocalDate.of(1977, 2, 2)), "etu", "kutsu", "suku", Some(Koodistokoodiviite("fi", "kieli")), Some(List(Koodistokoodiviite("fi", "maatjavaltiot2")))))
    }
    "Suoritus" in {
      testDeserialization[Suoritus](pakollinenTutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", None, k3, 15))
      testDeserialization[Suoritus](ympäristöalanPerustutkintoValmis())
    }
    "Järjestämismuoto" in {
      testDeserialization[Järjestämismuoto](järjestämismuotoOppisopimus)
    }
    "LocalizedString" in {
      testDeserialization[LocalizedString](LocalizedString.finnish("Moi"))
      testDeserialization[LocalizedString](LocalizedString.swedish("Hej"))
      testDeserialization[LocalizedString](LocalizedString.english("Hello"))
      testDeserialization[LocalizedString](English("Hello"))
      testDeserialization[LocalizedString](Finnish("Moi", Some("Hej"), Some("Hi")))
    }
    "Examples" - {
      Examples.oppijaExamples.foreach { example =>
        example.name in {
          testDeserialization(example.data)
        }
      }
    }
  }

  private def testDeserialization[T : TypeTag](obj: T) {
    deserialize(obj) match {
      case Right(x) =>
        x should equal(obj)
      case Left(errors) =>
        System.err.println(JsonSerializer.writeWithRoot(errors.toList, pretty = true))
        fail("Deserialization of " + obj + " failed")
    }
    testDeserialization(obj, (Right(obj)))
  }

  private def testDeserialization[T : TypeTag](obj: T, expected: Either[List[ValidationError], T]) {
    deserialize(obj) should equal(expected)
  }

  def deserialize[T : TypeTag](obj: T): Either[List[ValidationError], T] = {
    implicit val context: ExtractionContext = strictDeserialization
    SchemaValidatingExtractor.extract(JsonSerializer.serializeWithRoot(obj))
  }
}
