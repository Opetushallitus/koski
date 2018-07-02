package fi.oph.koski.json

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.{Henkilö, KoskiSchema, Oppija}
import fi.oph.scalaschema.{ClassSchema, Metadata, Property, SerializationContext}
import org.json4s.JsonAST.JObject
import org.json4s.{JArray, JValue}

import scala.collection.immutable

case class SensitiveDataFilter(user: KoskiSession) {
  private implicit val u = user
  val sensitiveDataAllowed = user.hasRole("LUOTTAMUKSELLINEN")

  def filterSensitiveData(s: ClassSchema, p: Property) = if (sensitiveHidden(p.metadata)) Nil else List(p)

  def serializationContext = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)

  def rowSerializer: ((Henkilö, immutable.Seq[OpiskeluoikeusRow])) => JValue = {
    def ser(tuple: (Henkilö, immutable.Seq[OpiskeluoikeusRow])) = {
      JsonSerializer.serialize(Oppija(tuple._1, tuple._2.map(_.toOpiskeluoikeus)))
    }
    ser
  }

  def sensitiveHidden(metadata: List[Metadata]): Boolean = metadata.exists {
    case SensitiveData() => !sensitiveDataAllowed
    case _ => false
  }
}
