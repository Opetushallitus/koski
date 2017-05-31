package fi.oph.koski.editor

import fi.oph.koski.json.Json
import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema._

object EditorLocalizationTool extends App {
  val keys = allSchemas(EditorSchema.schema)(KoskiSchema.schemaFactory, collection.mutable.Set.empty[String]).collect { case s: ClassSchema => s.properties.map(ObjectModelBuilder.propertyTitle) }.flatten.toSet
  val keyValueMap = keys.zip(keys).toMap
  println(Json.write(keyValueMap))

  def allSchemas(schema: Schema)(implicit factory: SchemaFactory, classesCovered: collection.mutable.Set[String]): List[Schema] = schema match {
    case s: ClassRefSchema => allSchemas(s.resolve(factory))
    case s: SchemaWithClassName if classesCovered.contains(s.fullClassName) =>
      Nil
    case s: SchemaWithClassName =>
      println(s.fullClassName)
      classesCovered.add(s.fullClassName)
      s match {
        case s: ClassSchema =>
          s :: s.properties.map(_.schema).flatMap(allSchemas)
        case s: AnyOfSchema => s :: s.alternatives.flatMap(allSchemas)
      }
    case s: OptionalSchema => s :: allSchemas(s.itemSchema)
    case s: ListSchema => s :: allSchemas(s.itemSchema)
    case s: ElementSchema => List(s)
  }
}
