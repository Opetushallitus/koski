package fi.oph.koski.schema

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.scalaschema.SchemaPropertyProcessor.SchemaPropertyProcessor
import fi.oph.scalaschema._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.{universe => ru}

object JsonSerializer {
  def writeWithRoot[T: ru.TypeTag](x: T, pretty: Boolean = false): String = {
    implicit val u = KoskiSession.systemUser
    write(x, pretty)
  }

  def serializeWithRoot[T: ru.TypeTag](obj: T): JValue = {
    implicit val u = KoskiSession.systemUser
    serialize(obj)
  }

  def write[T: ru.TypeTag](x: T, pretty: Boolean = false)(implicit user: KoskiSession): String = {
    if (pretty) {
      JsonMethods.pretty(serialize(x))
    } else {
      JsonMethods.compact(serialize(x))
    }
  }

  def serialize[T: ru.TypeTag](obj: T)(implicit user: KoskiSession): JValue = {
    val filterSensitiveData: SchemaPropertyProcessor = (s: ClassSchema, p: Property) => if (sensitiveHidden(p.metadata)) Nil else List(p)
    val context = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)
    Serializer.serialize(obj, context)
  }

  def sensitiveHidden(metadata: List[Metadata])(implicit user: KoskiSession): Boolean = metadata.exists {
    case RequiresRole(role) => !user.hasRole(role)
    case _ => false
  }
}

