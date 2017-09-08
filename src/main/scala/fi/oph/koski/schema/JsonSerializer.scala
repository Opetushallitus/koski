package fi.oph.koski.schema

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.scalaschema._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.{universe => ru}

class JsonSerializer(implicit user: KoskiSession) {
  def write[T: ru.TypeTag](x: T, pretty: Boolean = false): String = {
    if (pretty) {
      JsonMethods.pretty(serialize(x))
    } else {
      JsonMethods.compact(serialize(x))
    }
  }

  def serialize[T: ru.TypeTag](obj: T): JValue = {
    val context = SerializationContext(KoskiSchema.schemaFactory)
    Serializer.serialize(obj, context)
  }
}

object JsonSerializer {
  implicit private val root = KoskiSession.systemUser
  private val serializer = new JsonSerializer()
  def writeWithRoot[T: ru.TypeTag](x: T, pretty: Boolean = false): String = serializer.write(x, pretty)
  def serializeWithRoot[T: ru.TypeTag](obj: T): JValue = serializer.serialize(obj)
}

