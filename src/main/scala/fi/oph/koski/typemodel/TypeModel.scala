package fi.oph.koski.typemodel

import fi.oph.koski.typemodel.DataTypes.DataType
import fi.oph.scalaschema.Metadata
import fi.oph.scalaschema.annotation.{DefaultValue, MaxValue, MinValue}

object DataTypes extends Enumeration {
  type DataType = Value

  val Optional: DataType = Value("optional")
  val String: DataType = Value("string")
  val Boolean: DataType = Value("boolean")
  val Date: DataType = Value("date")
  val Number: DataType = Value("number")
  val Array: DataType = Value("array")
  val Object: DataType = Value("object")
  val Record: DataType = Value("record")
  val Enum: DataType = Value("enum")
  val Ref: DataType = Value("ref")
  val Any: DataType = Value("any")
  val Union: DataType = Value("union")
  val Literal: DataType = Value("literal")
}

trait TypeModel {
  def `type`: DataTypes.DataType
  def addMetadata(metadata: Metadata): TypeModel = this
  def withMetadata(metadata: List[Metadata]): TypeModel =
    metadata.foldLeft(this)((self, meta) => self.addMetadata(meta))
}

trait TypeModelWithClassName extends TypeModel {
  def fullClassName: String
  def packageName: String = fullClassName.splitAt(fullClassName.lastIndexOf("."))._1
  def className: String = fullClassName.splitAt(fullClassName.lastIndexOf(".") + 1)._2
}

// Primary types

case class StringType() extends TypeModel {
  def `type`: DataType = DataTypes.String
}

case class LiteralType(literal: String) extends TypeModel {
  def `type`: DataType = DataTypes.Literal
}

case class DateType() extends TypeModel {
  def `type`: DataType = DataTypes.Date
}

case class BooleanType() extends TypeModel {
  def `type`: DataType = DataTypes.Boolean
}

case class NumberType(
  default: Option[Number] = None,
  decimals: Option[Int] = None,
  min: Option[Double] = None,
  max: Option[Double] = None,
)extends TypeModel {
  def `type`: DataType = DataTypes.Number

  override def addMetadata(metadata: Metadata): TypeModel = metadata match {
    case DefaultValue(value) if value.isInstanceOf[Number] => this.copy(default = Some(value.asInstanceOf[Number]))
    case MinValue(value) => this.copy(min = Some(value))
    case MaxValue(value) => this.copy(max = Some(value))
    case _ => this
  }
}

// Container types

case class OptionalType(
  item: TypeModel
) extends TypeModel {
  def `type`: DataType = DataTypes.Optional
}
case class ArrayType(
  items: TypeModel
) extends TypeModel {
  def `type`: DataType = DataTypes.Array
}

case class RecordType(
  items: TypeModel,
) extends TypeModel {
  def `type`: DataType = DataTypes.Record
}

case class ObjectType(
  fullClassName: String,
  properties: Map[String, TypeModel],
) extends TypeModelWithClassName {
  def `type`: DataType = DataTypes.Object

  def makeOptional(keys: Seq[String]): ObjectType =
    this.copy(
      properties = properties.map {
        case (key, model) if keys.contains(key) => (key, OptionalType(model))
        case p: Any => p
      }
    )
}

// Reference types

case class ClassRef(
  fullClassName: String
) extends TypeModelWithClassName {
  def `type`: DataType = DataTypes.Ref
  def resolve(types: Seq[TypeModelWithClassName]): Option[TypeModelWithClassName] =
    types.find(_.fullClassName == fullClassName)
}

// Composition types

case class UnionType(
  fullClassName: String,
  anyOf: List[TypeModel],
) extends TypeModelWithClassName {
  def `type`: DataType = DataTypes.Union
}

case class EnumType[T](
  childType: DataTypes.DataType,
  enumValues: List[T],
) extends TypeModel {
  def `type`: DataType = DataTypes.Enum
}

// Undefined types

case class AnyType(
  `case`: String,
) extends TypeModel {
  def `type`: DataType = DataTypes.Any
}
