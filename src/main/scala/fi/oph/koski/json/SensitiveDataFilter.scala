package fi.oph.koski.json

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.{KoskiSchema, Oppija, TäydellisetHenkilötiedot}
import fi.oph.scalaschema.{ClassSchema, Metadata, Property, SerializationContext}
import org.json4s.JsonAST.JObject
import org.json4s.{JArray, JValue}

import scala.collection.immutable

case class SensitiveDataFilter(user: KoskiSession) {
  private implicit val u = user
  val sensitiveDataAllowed = user.hasRole("LUOTTAMUKSELLINEN")

  def filterSensitiveData(s: ClassSchema, p: Property) = if (sensitiveHidden(p.metadata)) Nil else List(p)

  def serializationContext = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)

  def rowSerializer: ((TäydellisetHenkilötiedot, immutable.Seq[OpiskeluoikeusRow])) => JValue = {
    def ser(tuple: (TäydellisetHenkilötiedot, immutable.Seq[OpiskeluoikeusRow])) = (tuple, sensitiveDataAllowed) match {
      case ((henkilö, rivit), true) =>
        JObject("henkilö" -> JsonSerializer.serialize(henkilö), "opiskeluoikeudet" -> JArray(rivit.toList.map(_.toOpiskeluoikeusData)))
      case ((henkilö, rivit), false) =>
        JsonSerializer.serialize(Oppija(henkilö, rivit.map(_.toOpiskeluoikeus)))
    }
    ser
  }

  def sensitiveHidden(metadata: List[Metadata]): Boolean = metadata.exists {
    case SensitiveData() => !sensitiveDataAllowed
    case _ => false
  }
}
