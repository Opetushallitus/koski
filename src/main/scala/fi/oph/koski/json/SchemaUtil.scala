package fi.oph.koski.json

import fi.oph.koski.editor.ClassFinder
import fi.oph.scalaschema._

object SchemaUtil {
  def getClazz(s: Schema): Option[Class[_]] = s match {
    case OptionalSchema(itemSchema) => getClazz(itemSchema)
    case ListSchema(itemSchema) => getClazz(itemSchema)
    case MapSchema(itemSchema) => getClazz(itemSchema)
    case n: SchemaWithClassName => Some(ClassFinder.forSchema(n))
    case _ => None
  }
}
