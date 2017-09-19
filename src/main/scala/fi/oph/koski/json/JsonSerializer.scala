package fi.oph.koski.json

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{KoskiSchema, RequiresRole}
import fi.oph.scalaschema.SchemaPropertyProcessor.SchemaPropertyProcessor
import fi.oph.scalaschema._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.universe.TypeTag

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
    val filterSensitiveData: SchemaPropertyProcessor = (s: ClassSchema, p: Property) => if (sensitiveHidden(p.metadata)) Nil else List(p)
    val context = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)
    Serializer.serialize(obj, context)
  }

  def serialize(obj: Any, schema: Schema)(implicit user: KoskiSession): JValue = {
    val filterSensitiveData: SchemaPropertyProcessor = (s: ClassSchema, p: Property) => if (sensitiveHidden(p.metadata)) Nil else List(p)
    val context = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)
    Serializer.serialize(obj, schema, context)
  }

  def parse[T: TypeTag](j: String, ignoreExtras: Boolean = false, validating: Boolean = true): T = {
    extract(JsonMethods.parse(j))
  }
  def extract[T: TypeTag](j: JValue, ignoreExtras: Boolean = false, validating: Boolean = true): T = {
    implicit val c = KoskiSchema.deserializationContext.copy(ignoreUnexpectedProperties = ignoreExtras, validate = validating)
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

