package fi.oph.koski.editor

import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.servlet.KoskiSpecificApiServlet
import org.json4s.jackson.Serialization
import reflect.runtime.universe.TypeTag

trait EditorApiServlet extends KoskiSpecificApiServlet {
  override def toJsonString[T: TypeTag](x: T): String =
    Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}
