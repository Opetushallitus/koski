package fi.oph.koski.json

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.KoskiSchema.schemaFactory
import fi.oph.koski.schema.{KoskiSchema, Oppija, RequiresRole, TäydellisetHenkilötiedot}
import fi.oph.scalaschema._
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JValue}

import scala.collection.immutable
import scala.reflect.runtime.universe.TypeTag

/**
  * JSON (de)serialization using scala-schema mechanisms
  */
object JsonSerializer {
  def writeWithRoot[T: TypeTag](x: T, pretty: Boolean = false): String = {
    implicit val u = KoskiSession.systemUser
    write(x, pretty)
  }

  def serializeWithRoot[T: TypeTag](obj: T): JValue = serializeWithUser(KoskiSession.systemUser)(obj)

  def serializeWithUser[T: TypeTag](user: KoskiSession)(obj: T): JValue = {
    implicit val u = user
    serialize(obj)
  }

  def write[T: TypeTag](x: T, pretty: Boolean = false)(implicit user: KoskiSession): String = {
    if (pretty) {
      JsonMethods.pretty(serialize(x))
    } else {
      JsonMethods.compact(serialize(x))
    }
  }

  def serialize[T: TypeTag](obj: T)(implicit user: KoskiSession): JValue = {
    Serializer.serialize(obj, SensitiveDataFilter.serializationContext)
  }

  def serialize(obj: Any, schema: Schema)(implicit user: KoskiSession): JValue = {
    Serializer.serialize(obj, schema, SensitiveDataFilter.serializationContext)
  }

  def parse[T: TypeTag](j: String, ignoreExtras: Boolean = false): T = {
    extract(JsonMethods.parse(j), ignoreExtras)
  }
  def extract[T: TypeTag](j: JValue, ignoreExtras: Boolean = false): T = {
    implicit val c = ExtractionContext(schemaFactory).copy(ignoreUnexpectedProperties = ignoreExtras)
    SchemaValidatingExtractor.extract(j) match {
      case Right(x) => x
      case Left(error) =>
        throw new RuntimeException(s"Validation error while de-serializing as ${implicitly[TypeTag[T]].tpe.toString}: " + error)
    }
  }
}

object SensitiveDataFilter {
  def filterSensitiveData(s: ClassSchema, p: Property)(implicit user: KoskiSession) = if (sensitiveHidden(p.metadata)) Nil else List(p)

  def serializationContext(implicit user: KoskiSession) = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)

  def rowSerializer(implicit user: KoskiSession): ((TäydellisetHenkilötiedot, immutable.Seq[OpiskeluoikeusRow])) => JValue = {
    val shortcut = user.hasRole("LUOTTAMUKSELLINEN")
    def ser(tuple: (TäydellisetHenkilötiedot, immutable.Seq[OpiskeluoikeusRow])) = (tuple, shortcut) match {
      case ((henkilö, rivit), true) =>
        JObject("henkilö" -> JsonSerializer.serialize(henkilö), "opiskeluoikeudet" -> JArray(rivit.toList.map(_.toOpiskeluoikeusData)))
      case ((henkilö, rivit), false) =>
        JsonSerializer.serialize(Oppija(henkilö, rivit.map(_.toOpiskeluoikeus)))
    }
    ser
  }

  def sensitiveHidden(metadata: List[Metadata])(implicit user: KoskiSession): Boolean = metadata.exists {
    case RequiresRole(role) => !user.hasRole(role)
    case _ => false
  }

}

