package fi.oph.koski.schema

import fi.oph.koski.TestEnvironment
import fi.oph.koski.schema.annotation.HiddenFromAiemmatOpiskeluoikeudet
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.{JNothing, JString, JValue}
import org.json4s.JsonAST.JBool
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class HiddenFromAiemmatOpiskeluoikeudetSchemaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(
    KoskiSchema.createSchema(classOf[HiddenFromAiemmatOpiskeluoikeudetTestClass]).asInstanceOf[ClassSchema]
  )

  "@HiddenFromAiemmatOpiskeluoikeudet" - {
    "merkitsee kentän skeema-JSONiin, jotta rajapinnan käyttäjä näkee, mitä aiemmatOpiskeluoikeudet-kysely ei palauta" in {
      (schemaJson \ "properties" \ "piilotettu" \ "hiddenFromAiemmatOpiskeluoikeudet") should be(JBool(true))
      (schemaJson \ "properties" \ "piilotettu" \ "description") should matchPattern { case JString(d) if d.contains("Ei palauteta aiemmatOpiskeluoikeudet-massaluovutuskyselyssä") => }
    }

    "ei merkitse muita kenttiä" in {
      (schemaJson \ "properties" \ "julkinen" \ "hiddenFromAiemmatOpiskeluoikeudet") should be(JNothing)
    }
  }
}

case class HiddenFromAiemmatOpiskeluoikeudetTestClass(
  @HiddenFromAiemmatOpiskeluoikeudet
  piilotettu: String,
  julkinen: String,
)
