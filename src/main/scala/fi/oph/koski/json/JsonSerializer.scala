package fi.oph.koski.json

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.KoskiSchema.schemaFactory
import fi.oph.koski.schema.{KoskiSchema, RequiresRole}
import fi.oph.scalaschema.SchemaPropertyProcessor.SchemaPropertyProcessor
import fi.oph.scalaschema._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.universe.TypeTag

object JsonSerializer {
  private def filterSensitiveData(s: ClassSchema, p: Property)(implicit user: KoskiSession) = if (sensitiveHidden(p.metadata)) Nil else List(p)
  def serializationContext(implicit user: KoskiSession) = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)

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
    Serializer.serialize(obj, serializationContext)
  }

  def serialize(obj: Any, schema: Schema)(implicit user: KoskiSession): JValue = {
    Serializer.serialize(obj, schema, serializationContext)
  }

  def parse[T: TypeTag](j: String, ignoreExtras: Boolean = false): T = {
    extract(JsonMethods.parse(j))
  }
  def extract[T: TypeTag](j: JValue, ignoreExtras: Boolean = false): T = {
    implicit val c = ExtractionContext(schemaFactory).copy(ignoreUnexpectedProperties = ignoreExtras)
    SchemaValidatingExtractor.extract(j) match {
      case Right(x) => x
      case Left(error) =>
        throw new RuntimeException(s"Validation error while de-serializing as ${implicitly[TypeTag[T]].tpe.toString}: " + error)
    }
  }

  def sensitiveHidden(metadata: List[Metadata])(implicit user: KoskiSession): Boolean = metadata.exists {
    case RequiresRole(role) => !user.hasRole(role)
    case _ => false
  }
}

