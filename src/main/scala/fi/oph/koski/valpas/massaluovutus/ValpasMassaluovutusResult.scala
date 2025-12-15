package fi.oph.koski.valpas.massaluovutus

import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema.annotation.Title
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

@Title("Oppivelvolliset oppijat")
case class ValpasOppivelvollisetMassaluovutusResult(
  oppijat: Seq[ValpasMassaluovutusOppivelvollinenOppija]
)

object ValpasOppivelvollisetMassaluovutusResult {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValpasOppivelvollisetMassaluovutusResult]).asInstanceOf[ClassSchema])
}

@Title("Ei oppivelvollisuutta suorittavat oppijat")
case class ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult(
  oppijat: Seq[ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija]
)

object ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult]).asInstanceOf[ClassSchema])
}
