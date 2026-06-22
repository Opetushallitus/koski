package fi.oph.koski.documentation

import fi.oph.koski.schema.annotation.OksaUri
import fi.oph.koski.schema.{Finnish, LocalizedString}
import fi.oph.scalaschema.annotation.Description
import fi.oph.scalaschema.{ClassSchema, SchemaFactory, SchemaToJson}
import org.json4s._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

@Description("Sample top-level class for LocalizedSchemaJsonSpec")
case class LocalizedSchemaJsonSpecSampleClass(
  @Description("Field description for foo") foo: String,
  bar: Option[String] = None,
  @Description("Field description for oksa")
  @OksaUri("tmpOKSAID123", "term") oksa: Option[String] = None
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

    (result \ "translation" \ "fi") shouldBe JNothing
    (result \ "translation" \ "sv") shouldBe JArray(List(JString("Exempel")))
    (result \ "translation" \ "en") shouldBe JArray(List(JString("Sample")))

    (result \ "properties" \ "foo" \ "translation" \ "fi") shouldBe JNothing
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

    (first \ "description") shouldBe JString("A")
    (second \ "description") shouldBe JString("B")
  }

  "Falls back to the code @Description as translation.fi when Tolgee has no entry" in {
    val helper = new LocalizedSchemaJson(() => Map.empty)
    val result = helper.translated("sample", schemaJson, schema)

    (result \ "translation" \ "fi") shouldBe JArray(List(JString("Sample top-level class for LocalizedSchemaJsonSpec")))
    (result \ "translation" \ "sv") shouldBe JNothing
    (result \ "translation" \ "en") shouldBe JNothing

    (result \ "properties" \ "foo" \ "translation" \ "fi") shouldBe JArray(List(JString("Field description for foo")))
    (result \ "properties" \ "foo" \ "translation" \ "sv") shouldBe JNothing

    // bar has no @Description, so nothing to fall back to.
    (result \ "properties" \ "bar" \ "translation") shouldBe JNothing
  }

  "Overrides the description field with the Tolgee fi value when present" in {
    val localizations = Map(
      "description:Sample top-level class for LocalizedSchemaJsonSpec" -> loc("Korjattu luokkakuvaus"),
      "description:Field description for foo" -> loc("Korjattu foo-kuvaus", "Korrigerad foo", "Fixed foo")
    )
    val helper = new LocalizedSchemaJson(() => localizations)

    val result = helper.translated("sample", schemaJson, schema)

    (result \ "description") shouldBe JString("Korjattu luokkakuvaus")
    (result \ "properties" \ "foo" \ "description") shouldBe JString("Korjattu foo-kuvaus")
  }

  "Drops the overridden description's fi from translation but keeps sv/en" in {
    val localizations = Map(
      "description:Field description for foo" -> loc("Korjattu foo-kuvaus", "Korrigerad foo", "Fixed foo")
    )
    val helper = new LocalizedSchemaJson(() => localizations)

    val result = helper.translated("sample", schemaJson, schema)

    (result \ "properties" \ "foo" \ "description") shouldBe JString("Korjattu foo-kuvaus")
    (result \ "properties" \ "foo" \ "translation" \ "fi") shouldBe JNothing
    (result \ "properties" \ "foo" \ "translation" \ "sv") shouldBe JArray(List(JString("Korrigerad foo")))
    (result \ "properties" \ "foo" \ "translation" \ "en") shouldBe JArray(List(JString("Fixed foo")))
  }

  "Keeps the @Description text when Tolgee has no entry for the key" in {
    val helper = new LocalizedSchemaJson(() => Map.empty)
    val result = helper.translated("sample", schemaJson, schema)

    (result \ "description") shouldBe JString("Sample top-level class for LocalizedSchemaJsonSpec")
    (result \ "properties" \ "foo" \ "description") shouldBe JString("Field description for foo")
  }

  "Preserves appendToDescription suffixes (e.g. Oksa link) when overriding" in {
    val localizations = Map(
      "description:Field description for oksa" -> loc("Korjattu oksa-kuvaus")
    )
    val helper = new LocalizedSchemaJson(() => localizations)

    val result = helper.translated("sample", schemaJson, schema)
    val oksaDesc = (result \ "properties" \ "oksa" \ "description")

    oksaDesc shouldBe a [JString]
    val text = oksaDesc.asInstanceOf[JString].s
    text should startWith ("Korjattu oksa-kuvaus")
    text should include ("Oksa:")
    text should include ("tmpOKSAID123")
  }

}
