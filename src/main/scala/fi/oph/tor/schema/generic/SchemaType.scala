package fi.oph.tor.schema.generic

sealed trait SchemaType {
  def metadata: List[Metadata] = Nil
  def fieldSchema(fieldName: String): Option[SchemaType] = None
  def mapTo(containedType: SchemaType) = containedType
}

case class OptionalType(itemType: SchemaType) extends SchemaType {
  override def mapTo(containedType: SchemaType) = OptionalType(itemType.mapTo(containedType))
}
case class ListType(itemType: SchemaType) extends SchemaType {
  override def mapTo(containedType: SchemaType) = ListType(itemType.mapTo(containedType))
}
case class DateType(enumValues: Option[List[Any]] = None) extends SchemaType
case class StringType(enumValues: Option[List[Any]] = None) extends SchemaType
case class BooleanType(enumValues: Option[List[Any]] = None) extends SchemaType
case class NumberType(enumValues: Option[List[Any]] = None) extends SchemaType
case class ClassType(fullClassName: String, properties: List[Property], override val metadata: List[Metadata], definitions: List[ClassType] = Nil) extends SchemaType with TypeWithClassName with ObjectWithMetadata[ClassType] {
  def getPropertyValue(property: Property, target: AnyRef): AnyRef = {
    target.getClass.getMethod(property.key).invoke(target)
  }
  def replaceMetadata(metadata: List[Metadata]) = copy(metadata = metadata)
}
case class ClassTypeRef(fullClassName: String, override val metadata: List[Metadata]) extends SchemaType with TypeWithClassName with ObjectWithMetadata[ClassTypeRef] {
  def replaceMetadata(metadata: List[Metadata]) = copy(metadata = metadata)
}
case class OneOf(types: List[TypeWithClassName]) extends SchemaType {
  def matchType(obj: AnyRef): SchemaType = {
    types.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}
trait TypeWithClassName extends SchemaType {
  def fullClassName: String
  def simpleName: String = {
    fullClassName.split("\\.").toList.last.toLowerCase
  }
}

case class Property(key: String, tyep: SchemaType, metadata: List[Metadata]) extends ObjectWithMetadata[Property] {
  def replaceMetadata(metadata: List[Metadata]) = copy(metadata = metadata)
}