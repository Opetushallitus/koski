package fi.oph.tor.schema.generic

sealed trait SchemaType {
  def metadata: List[Metadata] = Nil
  def fieldSchema(fieldName: String): Option[SchemaType] = None
}

case class OptionalType(itemType: SchemaType) extends SchemaType
case class ListType(itemType: SchemaType) extends SchemaType
case class DateType() extends SchemaType
case class StringType() extends SchemaType
case class BooleanType() extends SchemaType
case class NumberType() extends SchemaType
case class ClassType(fullClassName: String, properties: List[Property], override val metadata: List[Metadata], definitions: List[ClassType] = Nil) extends SchemaType with TypeWithClassName {
  def getPropertyValue(property: Property, target: AnyRef): AnyRef = {
    target.getClass.getMethod(property.key).invoke(target)
  }
}
case class ClassTypeRef(fullClassName: String, override val metadata: List[Metadata]) extends SchemaType with TypeWithClassName
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

case class Property(key: String, tyep: SchemaType, metadata: List[Metadata])