package fi.oph.koski.schema.annotation

import fi.oph.koski.koodisto.{SynteettinenKoodiResolvingCustomDeserializer}
import fi.oph.koski.schema.annotation.SchemaClassMapper.mapClasses
import fi.oph.scalaschema._
import org.json4s.JsonAST

case class SynteettinenKoodistoUri(koodistoUri: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JsonAST.JObject) = {
    val dokumentaatio =
      SynteettinenKoodiResolvingCustomDeserializer.synteettisetKoodistot
        .find(_.koodistoUri == koodistoUri).map(_.dokumentaatio).getOrElse("Dokumentaatiota ei löydy")
    appendToDescription(obj, s"(Synteettinen koodi: ${koodistoUri}, ${dokumentaatio})")
  }

  override def applyMetadata(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory) = {
    super.applyMetadata(mapClasses(x, schemaFactory, { case s: ClassSchema =>
      s.copy(properties = s.properties.map {
        case p: Property if p.key == "koodistoUri" => addEnumValue(koodistoUri, p)
        case p: Property => p
      })
    }), schemaFactory)
  }
}
