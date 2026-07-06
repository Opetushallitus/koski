package fi.oph.koski.schema

import fi.oph.koski.TestEnvironment
import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.{JNothing, JValue}
import org.json4s.JsonAST.JBool
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SensitiveDataSchemaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(
    KoskiSchema.createSchema(classOf[SensitiveDataTestClass]).asInstanceOf[ClassSchema]
  )

  "@SensitiveData" - {
    "merkitsee kentän skeema-JSONiin sensitive-lipulla, jotta skeemakatselin voi korostaa sen" in {
      (schemaJson \ "properties" \ "salainen" \ "sensitive") should be(JBool(true))
    }

    "ei lisää sensitive-lippua kenttiin joita ei ole merkitty" in {
      (schemaJson \ "properties" \ "julkinen" \ "sensitive") should be(JNothing)
    }
  }
}

case class SensitiveDataTestClass(
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  salainen: String,
  julkinen: String
)
