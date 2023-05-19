package fi.oph.koski.typemodel

import fi.oph.koski.typemodel.JsonValue.valueToJson
import fi.oph.scalaschema.annotation.EnumValue
import org.json4s.{JObject, JValue}

object Constraints {
  def build(constraintType: TypeModel, types: Seq[TypeModel]): Constraint = {
    implicit val allTypes: Seq[TypeModelWithClassName] = types.collect { case t: TypeModelWithClassName => t }
    buildConstraint(constraintType, 30)
  }

  private def buildConstraint(tpe: TypeModel, levelsLeft: Int)(implicit allTypes: Seq[TypeModelWithClassName]): Constraint = {
    if (levelsLeft == 0) {
      AnyConstraint()
    } else {
      tpe match {
        case t: ObjectType =>
          println(s"buildConstrait $t")
          ObjectConstraint(
            default = defaultOf(t).asInstanceOf[Option[JObject]],
            properties = t.properties.mapValues(value => buildConstraint(value, levelsLeft - 1)),
            `class` = t.fullClassName,
          )
        case t: ClassRef =>
          allTypes.find(_.fullClassName == t.fullClassName)
            .map(a => buildConstraint(a, levelsLeft - 1))
            .getOrElse(AnyConstraint())
        case t: StringType =>
          StringConstraint(
            default = defaultOf(t)
          )
        case t: LiteralType =>
          LiteralConstraint(constant = t.literal)
        case _: DateType =>
          DateConstraint()
        case t: BooleanType =>
          BooleanConstraint(
            default = defaultOf(t),
          )
        case t: NumberType =>
          NumberConstraint(
            default = defaultOf(t),
            min = t.min,
            max = t.max,
            decimals = t.decimals,
          )
        case t: OptionalType =>
          OptionalConstraint(
            default = defaultOf(t),
            optional = buildConstraint(t.item, levelsLeft - 1),
          )
        case t: ArrayType =>
          ArrayConstraint(
            default = defaultOf(t),
            items = buildConstraint(t.items, levelsLeft - 1),
          )
        case t: RecordType =>
          RecordConstraint(
            default = defaultOf(t),
            items = buildConstraint(t.items, levelsLeft - 1),
          )
        case t: UnionType =>
          UnionConstraint(
            anyOf = t.anyOf
              .collect { case t: TypeModelWithClassName => t }
              .map(child => (child.fullClassName, buildConstraint(child, levelsLeft - 1)))
              .toMap,
          )
        case t: EnumType[_] =>
          t.childType match {
            case DataTypes.String =>
              StringConstraint(
                default = defaultOf(t),
                enum = Some(t.enumValues.asInstanceOf[List[String]]),
              )
            case DataTypes.Number =>
              NumberConstraint(
                default = defaultOf(t),
                enum = Some(t.enumValues.asInstanceOf[List[Double]]),
              )
            case DataTypes.Boolean =>
              BooleanConstraint(
                default = defaultOf(t),
                enum = Some(t.enumValues.asInstanceOf[List[Boolean]]),
              )
            case _ => AnyConstraint()
          }
        case _: AnyType =>
          AnyConstraint()
      }
    }
  }

  def defaultOf(t: TypeModel): Option[JValue] =
    t.unambigiousDefaultValue
      .map(default => valueToJson(default))
}

trait Constraint {
  def `type`: String
}

case class StringConstraint(
  default: Option[JValue] = None,
  enum: Option[Seq[String]] = None,
  @EnumValue("string")
  `type`: String = "string",
) extends Constraint

case class LiteralConstraint(
  constant: String,
  @EnumValue("string")
  `type`: String = "string",
) extends Constraint

case class DateConstraint(
  @EnumValue("date")
  `type`: String = "date",
) extends Constraint

case class BooleanConstraint(
  default: Option[JValue] = None,
  enum: Option[Seq[Boolean]] = None,
  @EnumValue("bool")
  `type`: String = "bool",
) extends Constraint

case class NumberConstraint(
  default: Option[JValue] = None,
  decimals: Option[Int] = None,
  min: Option[Limit] = None,
  max: Option[Limit] = None,
  enum: Option[Seq[Double]] = None,
  @EnumValue("number")
  `type`: String = "number",
) extends Constraint

case class OptionalConstraint(
  default: Option[JValue] = None,
  optional: Constraint,
  @EnumValue("optional")
  `type`: String = "optional",
) extends Constraint

case class ArrayConstraint(
  default: Option[JValue] = None,
  minItems: Option[Int] = None,
  maxItems: Option[Int] = None,
  items: Constraint,
  @EnumValue("array")
  `type`: String = "array",
) extends Constraint

case class RecordConstraint(
  default: Option[JValue] = None,
  items: Constraint,
  @EnumValue("object")
  `type`: String = "object",
) extends Constraint

case class ObjectConstraint(
  default: Option[JObject] = None,
  `class`: String,
  properties: Map[String, Constraint],
  @EnumValue("object")
  `type`: String = "object"
) extends Constraint

case class UnionConstraint(
  anyOf: Map[String, Constraint],
  @EnumValue("union")
  `type`: String = "union",
) extends Constraint

case class AnyConstraint(
  @EnumValue("any")
  `type`: String = "any",
) extends Constraint
