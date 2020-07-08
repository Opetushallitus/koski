package fi.oph.koski.schema.annotation

import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.schema.annotation.SchemaClassMapper.mapClasses
import fi.oph.scalaschema._
import org.json4s.JsonAST._

/*
  Vain tietty arvo hyväksytään -annotaatio
 */
case class AllowedValue(arvo: Any) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject): JObject =
    appendToDescription(obj, "(Hyväksytty arvo: " + arvo + ")")

  override def applyMetadata(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = super.applyMetadata(x match {
    case p: Property => addEnumValue(arvo, p)
    case _ => x
  }, schemaFactory)
}
