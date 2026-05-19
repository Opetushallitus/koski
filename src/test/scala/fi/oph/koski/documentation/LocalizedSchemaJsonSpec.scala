package fi.oph.koski.documentation

import fi.oph.koski.schema.{Finnish, LocalizedString}
import fi.oph.scalaschema.annotation.Description
import fi.oph.scalaschema.{ClassSchema, SchemaFactory, SchemaToJson}
import org.json4s._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

@Description("Sample top-level class for LocalizedSchemaJsonSpec")
case class LocalizedSchemaJsonSpecSampleClass(
  @Description("Field description for foo") foo: String,
  bar: Option[String] = None
)

class LocalizedSchemaJsonSpec extends AnyFreeSpec with Matchers {

  private lazy val schema: ClassSchema =
    SchemaFactory().createSchema(classOf[LocalizedSchemaJsonSpecSampleClass]).asInstanceOf[ClassSchema]

  private lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema)

  private def loc(fi: String, sv: String = "", en: String = ""): LocalizedString =
    Finnish(fi, Option(sv).filter(_.nonEmpty), Option(en).filter(_.nonEmpty))

  "Attaches translation fields where localization keys resolve" in {
    val localizations = Map(
      "description:Sample top-level class for LocalizedSchemaJsonSpec" -> loc("Esimerkki", "Exempel", "Sample"),
      "description:Field description for foo" -> loc("Suomi-foo-kuvaus", "", "English foo desc")
    )
    val helper = new LocalizedSchemaJson(() => localizations)

    val result = helper.translated("sample", schemaJson, schema)

    (result \ "translation" \ "fi") shouldBe JArray(List(JString("Esimerkki")))
    (result \ "translation" \ "sv") shouldBe JArray(List(JString("Exempel")))
    (result \ "translation" \ "en") shouldBe JArray(List(JString("Sample")))

    (result \ "properties" \ "foo" \ "translation" \ "fi") shouldBe JArray(List(JString("Suomi-foo-kuvaus")))
    (result \ "properties" \ "foo" \ "translation" \ "en") shouldBe JArray(List(JString("English foo desc")))
    (result \ "properties" \ "foo" \ "translation" \ "sv") shouldBe JNothing
  }

  "Returns the cached JValue when the localization snapshot reference is unchanged" in {
    val localizations = Map("description:Sample top-level class for LocalizedSchemaJsonSpec" -> loc("X", "Y", "Z"))
    val helper = new LocalizedSchemaJson(() => localizations)

    val first = helper.translated("sample", schemaJson, schema)
    val second = helper.translated("sample", schemaJson, schema)

    second should be theSameInstanceAs first
  }

  "Recomputes when the localization snapshot reference changes" in {
    var localizations: Map[String, LocalizedString] = Map("description:Sample top-level class for LocalizedSchemaJsonSpec" -> loc("A"))
    val helper = new LocalizedSchemaJson(() => localizations)

    val first = helper.translated("sample", schemaJson, schema)
    localizations = Map("description:Sample top-level class for LocalizedSchemaJsonSpec" -> loc("B"))
    val second = helper.translated("sample", schemaJson, schema)

    (first \ "translation" \ "fi") shouldBe JArray(List(JString("A")))
    (second \ "translation" \ "fi") shouldBe JArray(List(JString("B")))
  }

  "Omits the translation field when no keys resolve" in {
    val helper = new LocalizedSchemaJson(() => Map.empty)
    val result = helper.translated("sample", schemaJson, schema)

    (result \ "translation") shouldBe JNothing
    (result \ "properties" \ "foo" \ "translation") shouldBe JNothing
    (result \ "properties" \ "bar" \ "translation") shouldBe JNothing
  }
}
