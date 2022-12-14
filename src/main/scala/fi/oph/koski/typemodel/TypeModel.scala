package fi.oph.koski.typemodel

import fi.oph.koski.typemodel.DataTypes.DataType
import fi.oph.koski.typemodel.ObjectType.{ObjectDefaultNode, ObjectDefaultsProperty, ObjectDefaultsMap}
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

  def unambigiousDefaultValue: Option[Any] = None
}

trait TypeModelWithClassName extends TypeModel {
  def fullClassName: String
  def packageName: String = fullClassName.splitAt(fullClassName.lastIndexOf("."))._1
  def className: String = fullClassName.splitAt(fullClassName.lastIndexOf(".") + 1)._2
}

// Primary types

case class StringType(
  default: Option[String] = None,
) extends TypeModel {
  def `type`: DataType = DataTypes.String
  override def addMetadata(metadata: Metadata): TypeModel = metadata match {
    case DefaultValue(value) if value.isInstanceOf[String] => this.copy(default = Some(value.asInstanceOf[String]))
    case _ => this
  }
  override def unambigiousDefaultValue: Option[String] = default
}

case class LiteralType(literal: String) extends TypeModel {
  def `type`: DataType = DataTypes.Literal
  override def unambigiousDefaultValue: Option[String] = Some(literal)
}

case class DateType() extends TypeModel {
  def `type`: DataType = DataTypes.Date
}

case class BooleanType(
  default: Option[Boolean] = None,
) extends TypeModel {
  def `type`: DataType = DataTypes.Boolean
  override def addMetadata(metadata: Metadata): TypeModel = metadata match {
    case DefaultValue(value) if value.isInstanceOf[Boolean] => this.copy(default = Some(value.asInstanceOf[Boolean]))
    case _ => this
  }
  override def unambigiousDefaultValue: Option[Boolean] = default
}

case class NumberType(
  default: Option[Number] = None,
  decimals: Option[Int] = None,
  min: Option[Double] = None,
  max: Option[Double] = None,
) extends TypeModel {
  def `type`: DataType = DataTypes.Number

  override def addMetadata(metadata: Metadata): TypeModel = metadata match {
    case DefaultValue(value) if value.isInstanceOf[Number] => this.copy(default = Some(value.asInstanceOf[Number]))
    case MinValue(value) => this.copy(min = Some(value))
    case MaxValue(value) => this.copy(max = Some(value))
    case _ => this
  }

  override def unambigiousDefaultValue: Option[Number] = default
}

// Container types

case class OptionalType(
  item: TypeModel
) extends TypeModel {
  def `type`: DataType = DataTypes.Optional
  override def unambigiousDefaultValue: Option[Option[_]] = Some(None)
}

case class ArrayType(
  items: TypeModel
) extends TypeModel {
  def `type`: DataType = DataTypes.Array
  override def unambigiousDefaultValue: Option[List[_]] = Some(List())
}

case class RecordType(
  items: TypeModel,
) extends TypeModel {
  def `type`: DataType = DataTypes.Record
  override def unambigiousDefaultValue: Option[Any] = Some(Map())
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

  override def unambigiousDefaultValue: Option[ObjectDefaultsMap] = {
    val propDefaults = properties.mapValues {
      case o: ObjectType => o.unambigiousDefaultValue.map(values => ObjectDefaultsMap(o.className, values.properties))
      case p: TypeModel => p.unambigiousDefaultValue.map(value => ObjectDefaultsProperty(value))
      case _ => None
    }
    if (propDefaults.values.forall(_.isDefined)) {
      Some(ObjectDefaultsMap(className, propDefaults.mapValues(_.get)))
    } else {
      None
    }
  }
}

object ObjectType {
  trait ObjectDefaultNode

  case class ObjectDefaultsMap(
    className: String,
    properties: Map[String, ObjectDefaultNode]
  ) extends ObjectDefaultNode

  case class ObjectDefaultsProperty[T](
    default: T
  ) extends ObjectDefaultNode
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
  override def unambigiousDefaultValue: Option[T] =
    if (enumValues.length == 1) {
      Some(enumValues.head)
    } else {
      None
    }
}

// Undefined types

case class AnyType(
  `case`: String,
) extends TypeModel {
  def `type`: DataType = DataTypes.Any
}
