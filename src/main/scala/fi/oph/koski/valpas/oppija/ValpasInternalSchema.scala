package fi.oph.koski.valpas.oppija

import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.valpas.rouhinta.{HeturouhinnanTulos, KuntarouhinnanTulos}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object ValpasInternalSchema {
  lazy val laajaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[OppijaHakutilanteillaLaajatTiedot]).asInstanceOf[ClassSchema])
  lazy val suppeaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[OppijaHakutilanteillaSuppeatTiedot]).asInstanceOf[ClassSchema])
  lazy val kuntaSuppeaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[OppijaKuntailmoituksillaSuppeatTiedot]).asInstanceOf[ClassSchema])
  lazy val heturouhintaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[HeturouhinnanTulos]).asInstanceOf[ClassSchema])
  lazy val kuntarouhintaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[KuntarouhinnanTulos]).asInstanceOf[ClassSchema])
}
