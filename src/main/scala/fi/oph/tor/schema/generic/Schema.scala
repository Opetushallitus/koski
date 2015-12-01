package fi.oph.tor.schema.generic

sealed trait Schema {
  def metadata: List[Metadata] = Nil
  def fieldSchema(fieldName: String): Option[Schema] = None
  def mapTo(containedSchema: Schema) = containedSchema
}

case class OptionalSchema(itemSchema: Schema) extends Schema {
  override def mapTo(containedSchema: Schema) = OptionalSchema(itemSchema.mapTo(containedSchema))
}
case class ListSchema(itemSchema: Schema) extends Schema {
  override def mapTo(containedSchema: Schema) = ListSchema(itemSchema.mapTo(containedSchema))
}
case class DateSchema(enumValues: Option[List[Any]] = None) extends Schema
case class StringSchema(enumValues: Option[List[Any]] = None) extends Schema
case class BooleanSchema(enumValues: Option[List[Any]] = None) extends Schema
case class NumberSchema(enumValues: Option[List[Any]] = None) extends Schema
case class ClassSchema(fullClassName: String, properties: List[Property], override val metadata: List[Metadata], definitions: List[SchemaWithClassName] = Nil) extends Schema with SchemaWithClassName with ObjectWithMetadata[ClassSchema] {
  override def getSchema(className: String): Option[SchemaWithClassName] = {
    if (className == this.fullClassName) {
      Some(this)
    } else {
      definitions.find(_.fullClassName == className)
    }
  }

  def getPropertyValue(property: Property, target: AnyRef): AnyRef = {
    target.getClass.getMethod(property.key).invoke(target)
  }
  def replaceMetadata(metadata: List[Metadata]) = copy(metadata = metadata)
}
case class ClassRefSchema(fullClassName: String, override val metadata: List[Metadata]) extends Schema with SchemaWithClassName with ObjectWithMetadata[ClassRefSchema] {
  def replaceMetadata(metadata: List[Metadata]) = copy(metadata = metadata)
}
case class OneOfSchema(alternatives: List[SchemaWithClassName], fullClassName: String) extends SchemaWithClassName {
}
trait SchemaWithClassName extends Schema {
  def fullClassName: String
  def simpleName: String = {
    fullClassName.split("\\.").toList.last.toLowerCase
  }
  def getSchema(className: String): Option[SchemaWithClassName] = if (className == fullClassName) {
    Some(this)
  } else {
    None
  }
}

case class Property(key: String, tyep: Schema, metadata: List[Metadata]) extends ObjectWithMetadata[Property] {
  def replaceMetadata(metadata: List[Metadata]) = copy(metadata = metadata)
}