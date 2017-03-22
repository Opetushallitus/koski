package fi.oph.koski.schema

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.Examples
import fi.oph.koski.json.Json
import fi.oph.koski.localization.{English, Finnish, LocalizedString}
import org.json4s.JsonAST.JString
import org.scalatest.{FreeSpec, Matchers}

class SchemaBasedJsonDeserializerSpec extends FreeSpec with Matchers {
  implicit val context = DeserializationContext(KoskiSchema.schema)

  "SchemaBasedJsonDeserializer" - {
    "Henkilö" in {
      val oid = "1.2.246.562.24.12345678901"
      testDeserialization(OidHenkilö(oid), classOf[OidHenkilö])
      testDeserialization(OidHenkilö(oid), classOf[Henkilö])
      testDeserialization(OidHenkilö(""), classOf[OidHenkilö], Left(List(ValidationError("oid",JString(""),EmptyString()))))
      testDeserialization(OidHenkilö("123"), classOf[OidHenkilö], Left(List(ValidationError("oid",JString("123"),RegExMismatch("""1\.2\.246\.562\.24\.\d{11}""")))))
      testDeserialization(TäydellisetHenkilötiedot(oid, "123456-7890", "etu", "kutsu", "suku", Some(Koodistokoodiviite("fi", "kieli")), Some(List(Koodistokoodiviite("fi", "maatjavaltiot2")))), classOf[Henkilö])
    }
    "Suoritus" in {
      testDeserialization(tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", k3, 15), classOf[Suoritus])
      testDeserialization(ympäristöalanPerustutkintoValmis(), classOf[Suoritus])
    }
    "Järjestämismuoto" in {
      testDeserialization(järjestämismuotoOppisopimus, classOf[Järjestämismuoto])
    }
    "LocalizedString" in {
      testDeserialization(LocalizedString.finnish("Moi"), classOf[LocalizedString])
      testDeserialization(LocalizedString.swedish("Hej"), classOf[LocalizedString])
      testDeserialization(LocalizedString.english("Hello"), classOf[LocalizedString])
      testDeserialization(English("Hello"), classOf[LocalizedString])
      testDeserialization(Finnish("Moi", Some("Hej"), Some("Hi")), classOf[LocalizedString])
    }
    "Examples" - {
      Examples.examples.foreach { example =>
        example.name in {
          testDeserialization(example.data, classOf[Oppija])
        }
      }
    }
  }

  private def testDeserialization(obj: AnyRef, klass: Class[_]) {
    deserialize(obj, klass) match {
      case Right(x) =>
        x should equal(obj)
      case Left(errors) =>
        System.err.println(Json.writePretty(errors))
        fail("Deserialization of " + obj + " failed")
    }
    testDeserialization(obj, klass, (Right(obj)))
  }

  private def testDeserialization(obj: AnyRef, klass: Class[_], expected: Either[List[ValidationError], Any]) {
    deserialize(obj, klass) should equal(expected)
  }

  def deserialize(obj: AnyRef, klass: Class[_]): Either[List[ValidationError], Any] = {
    val schema = KoskiSchema.schema.getSchema(klass.getName).get
    SchemaBasedJsonDeserializer.extract(Json.toJValue(obj), schema, Nil)
  }
}
