package fi.oph.koski.schema

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.json.{GenericJsonFormats, Json}
import fi.oph.koski.localization.{English, Finnish, LocalizedString, Swedish}
import fi.oph.koski.schema.AnyOfDeserialization.DiscriminatorCriterion
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.RegularExpression
import org.json4s._

case class DeserializationContext(rootSchema: SchemaWithClassName, path: String = "", customDeserializers: List[(String, CustomDeserializer)] = Nil) {
  def hasSerializerFor(schema: SchemaWithClassName) = customSerializerFor(schema).isDefined
  def customSerializerFor(schema: SchemaWithClassName) = customDeserializers.find(_._1 == schema.fullClassName).map(_._2)
}

trait CustomDeserializer {
  def extract(json: JValue, schema: Schema)(implicit context: DeserializationContext): Either[List[DeserializationError], Any]
}

sealed trait ValidationRuleViolation
case class UnexpectedType(expectedType: String, errorType: String = "unexpectedType") extends ValidationRuleViolation
case class EmptyString(errorType: String = "emptyString") extends ValidationRuleViolation
case class RegExMismatch(regex: String, errorType: String = "regularExpressionMismatch") extends ValidationRuleViolation
case class EnumValueMismatch(allowedValues: List[Any], errorType: String = "enumValueMismatch") extends ValidationRuleViolation
case class NotAnyOf(allowedAlternatives: List[(String, List[String])], errorType: String = "notAnyOf") extends ValidationRuleViolation
case class OtherViolation(message: String, errorType: String = "otherViolation") extends ValidationRuleViolation

case class DeserializationError(path: String, value: JValue, error: ValidationRuleViolation)

case class DeserializationException(path: String, message: String, cause: Exception) extends RuntimeException(s"Deserialization error at ${path} ${message}: ${cause.getMessage}", cause)
case class TooManyMatchingCasesException(path: String, cases: List[(SchemaWithClassName, List[DiscriminatorCriterion])]) extends RuntimeException(s"Deserialization error at ${path}: more than 1 matching case: ${cases.map {
  case (schema, criteria) => s"${schema.simpleName} (${criteria.mkString(", ")})"
}.mkString("\n")}")
case class SchemaNotFoundException(path: String, className: String) extends RuntimeException(s"Deserialization error at ${path}: schema not found for class ${className}")

case class Discriminator() extends RepresentationalMetadata
trait IgnoreInAnyOfDeserialization {}

object SchemaBasedJsonDeserializer {
  private implicit val formats = GenericJsonFormats.genericFormats
  private val noErrors = Nil : List[DeserializationError]


  def extract(json: JValue, schema: Schema)(implicit context: DeserializationContext): Either[List[DeserializationError], Any] = {
    schema match {
      case ss: StringSchema => extractString(json, ss)
      case ns: NumberSchema => extractNumber(json, ns)
      case bs: BooleanSchema => extractBoolean(json, bs)
      case ds: DateSchema => extractDate(json, ds)
      case cs: SchemaWithClassName if context.hasSerializerFor(cs) => context.customSerializerFor(cs).get.extract(json, schema)
      case cs: ClassRefSchema => extract(json, resolveSchema(cs))
      case cs: ClassSchema => extractObject(json, cs)
      case as: AnyOfSchema => AnyOfDeserialization.extractAnyOf(json, as)
      case os: OptionalSchema => extractOptional(json, os)
      case ls: ListSchema => extractList(json, ls)
    }
  }

  def extractAs(json: JValue, klass: Class[_])(implicit context: DeserializationContext): Either[List[DeserializationError], Any] = {
    extract(json, context.rootSchema.getSchema(klass.getName).get)
  }

  protected [schema] def resolveSchema(cs: ClassRefSchema)(implicit context: DeserializationContext) = {
    context.rootSchema.getSchema(cs.fullClassName).getOrElse(throw new SchemaNotFoundException(context.path, cs.fullClassName))
  }

  def extractList(json: JValue, as: ListSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], Any] = json match {
      // TODO: maxValues, minValues

    case JArray(values) =>
      val valueResults: List[Either[List[DeserializationError], Any]] = values.zipWithIndex.map {
        case (itemJson, index) =>
          extract(itemJson, as.itemSchema)(context.copy(path = subPath(context.path, index.toString)))
      }
      val errors: List[DeserializationError] = valueResults.collect { case Left(errors) => errors }.flatten
      errors match {
        case Nil =>
          Right(valueResults.map(_.right.get))
        case _ => Left(errors)
      }
    case _ => Left(List(DeserializationError(context.path, json, UnexpectedType("array"))))
  }

  def extractOptional(json: JValue, as: OptionalSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], AnyRef] = json match {
    case JNothing => Right(None)
    case _ => extract(json, as.itemSchema).right.map(value => Some(value))
  }

  private def extractObject(json: JValue, schema: ClassSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], AnyRef] = {
    json match {
      case JObject(values) =>
        val propertyResults: List[Either[List[DeserializationError], Any]] = schema.properties.map { property =>
          val subContext = context.copy(path = subPath(context.path, property.key))
          val jsonValue = json \ property.key
          extract(jsonValue, property.schema)(subContext)
        }
        // TODO: fail on extra properties
        val errors: List[DeserializationError] = propertyResults.collect { case Left(errors) => errors }.flatten
        errors match {
          case Nil =>
            val constructor = Class.forName(schema.fullClassName).getConstructors.apply(0)
            val constructorParamsAndTypes: List[(Object, Class[_])] = propertyResults.map(_.right.get).asInstanceOf[List[Object]].zip(constructor.getParameterTypes)
            val constructorParams = constructorParamsAndTypes.map {
              case (number: Number, klass) =>
                convertNumber(number, klass)
              case (value, klass) => value
            }.asInstanceOf[List[Object]]

            try {
              Right(constructor.newInstance(constructorParams: _*).asInstanceOf[AnyRef])
            } catch {
              // TODO: number conversions
              case e: Exception => throw new DeserializationException(context.path, s"instantiating ${schema.fullClassName} with ${constructorParams}", e)
            }

          case _ => Left(errors)
        }
      case _ => Left(List(DeserializationError(context.path, json, UnexpectedType("object"))))
    }
  }

  private def convertNumber(number: Number, klass: Class[_]) =  {
    if (klass == classOf[Int] || klass == classOf[Integer]) {
      number.intValue()
    } else if (klass == classOf[Float]) {
      number.floatValue()
    } else if (klass == classOf[Double]) {
      number.doubleValue()
    } else {
      //println(s"Number conversion from ${number.getClass.getName} to ${klass.getName}")
      number
    }
  }

  private def subPath(path: String, elem: String) = path match {
    case "" => elem
    case _ => path + "." + elem
  }

  private def extractNumber(json: JValue, schema: NumberSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], Number] = json match {
    // TODO: maxValue, minValue, enumValues
    case JDouble(num) => Right(num)
    case JInt(num) => Right(num)
    case JDecimal(num) => Right(num)
    case JLong(num) => Right(num)
    case _ => Left(List(DeserializationError(context.path, json, UnexpectedType("number"))))
  }

  private def extractBoolean(json: JValue, schema: BooleanSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], Boolean] = json match {
    // TODO: enumValues
    case JBool(b) => Right(b)
    case _ => Left(List(DeserializationError(context.path, json, UnexpectedType("boolean"))))
  }

  private def extractDate(json: JValue, schema: DateSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], LocalDate] = json match {
    // TODO: enumValues
    case JString(dateString) => try {
        Right(LocalDate.parse(dateString))
      } catch {
        case e: DateTimeParseException => Left(List(DeserializationError(context.path, json, UnexpectedType("string/date"))))
      }
    case _ => Left(List(DeserializationError(context.path, json, UnexpectedType("string/date"))))
  }


  private def extractString(json: JValue, schema: StringSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], String] = json match {
    case JString(s) =>
      val stringValue = json.extract[String]
      stringValue match {
        case "" => Left(List(DeserializationError(context.path, json, EmptyString())))
        case _ =>
          val errors = schema.metadata.flatMap {
            case RegularExpression(r) if !stringValue.matches(r) =>
              List(DeserializationError(context.path, json, RegExMismatch(r)))
            case (_) =>
              Nil
          } ++ {
            verifyEnumValue(schema.enumValues, stringValue)
          }
          errors match {
            case Nil => Right(stringValue)
            case _ => Left(errors)
          }
      }
    case _ => Left(List(DeserializationError(context.path, json, UnexpectedType("string"))))
  }

  private def verifyEnumValue[T <: Any](enumValues: Option[List[T]], actualValue: T)(implicit context: DeserializationContext) = {
    enumValues match {
      case Some(values) if !values.contains(actualValue) => List(DeserializationError(context.path, toJValue(actualValue), EnumValueMismatch(values)))
      case _ => Nil
    }
  }

  protected [schema] def toJValue(t: Any): JValue = t match {
    case t : AnyRef => Json.toJValue(t)
    case _ => JDouble(t.asInstanceOf[Number].doubleValue())
  }

  protected [schema] def writeJson(t: Any) = t match {
    case t : AnyRef => Json.write(t)
    case _ => t.toString
  }
}

object AnyOfDeserialization {
  def extractAnyOf(json: JValue, as: AnyOfSchema)(implicit context: DeserializationContext): Either[List[DeserializationError], Any] = {
    val mapping: List[(SchemaWithClassName, List[DiscriminatorCriterion])] = as.alternatives.filterNot(ignoredAlternative).map { schema =>
      (schema, discriminatorCriteria(schema, KeyPath.root))
    }.sortBy(-_._2.length)

    val matchingSchemas = mapping.collect {
      case (schema, criteria) if criteria.filter(criterion => criterion.apply(json).nonEmpty).isEmpty => (schema, criteria)
    }

    /*
    mapping.foreach {
      case (schema, criteria) => criteria.foreach { criterion =>
        val errors = criterion.apply(json)
        if (errors.nonEmpty) {
          println(context.path + ": " + schema.simpleName + ": " + errors)
        }
      }
    }
    */

    matchingSchemas match {
      case Nil =>
        val allowedAlternatives: List[(String, List[String])] = mapping.map { case (schema, criteria) => (schema.simpleName, criteria.flatMap(c => c.apply(json))) }
        Left(List(DeserializationError(context.path, json, NotAnyOf(allowedAlternatives))))
      case _ =>
        val maxCriteria = matchingSchemas.head._2.length
        val schemasWithMaximumNumberOfMatchingCriteria = matchingSchemas.filter { case (_, criteria) => criteria.length == maxCriteria }
        schemasWithMaximumNumberOfMatchingCriteria match {
          case List((schema, criteria)) => SchemaBasedJsonDeserializer.extract(json, schema)
          case _ => throw new TooManyMatchingCasesException(context.path, schemasWithMaximumNumberOfMatchingCriteria)
        }
    }
  }

  private def ignoredAlternative(schema: SchemaWithClassName) = classOf[IgnoreInAnyOfDeserialization].isAssignableFrom(Class.forName(schema.fullClassName))

  private def discriminatorCriteria(schema: SchemaWithClassName, keyPath: KeyPath)(implicit context: DeserializationContext): List[DiscriminatorCriterion] = schema match {
    case s: ClassRefSchema => discriminatorCriteria(SchemaBasedJsonDeserializer.resolveSchema(s), keyPath)
    case s: ClassSchema =>
      val discriminatorProps: List[Property] = s.properties.filter(_.metadata.contains(Discriminator()))
      discriminatorProps match {
        case Nil =>
          NoOtherPropertiesThan(keyPath, s.properties.map(_.key)) :: (s.properties.flatMap(propertyMatchers(keyPath, _)))
        case props =>
          props.flatMap(propertyMatchers(keyPath, _))
      }
  }

  private def propertyMatchers(keyPath:KeyPath, property: Property)(implicit context: DeserializationContext): List[DiscriminatorCriterion] = {
    val propertyPath = keyPath.concat(property.key)
    property.schema match {
      case s: OptionalSchema if !property.metadata.contains(Discriminator()) => Nil // Optional attribute are required only when marked with @Discriminator
      case s: StringSchema if s.enumValues.isDefined =>  List(PropertyEnumValues(propertyPath, s, s.enumValues.get))
      case s: NumberSchema if s.enumValues.isDefined =>  List(PropertyEnumValues(propertyPath, s, s.enumValues.get))
      case s: BooleanSchema if s.enumValues.isDefined => List(PropertyEnumValues(propertyPath, s, s.enumValues.get))
      case s: DateSchema if s.enumValues.isDefined =>    List(PropertyEnumValues(propertyPath, s, s.enumValues.get))
      case s: ClassRefSchema => propertyMatchers(keyPath, property.copy(schema = SchemaBasedJsonDeserializer.resolveSchema(s)))
      case s: ClassSchema =>
        List(PropertyExists(propertyPath)) ++ s.properties.flatMap { nestedProperty =>
          discriminatorCriteria(s, propertyPath)
        }
      case s =>
        List(PropertyExists(propertyPath))
    }
  }.distinct


  case class KeyPath(path: List[String]) {
    def apply(value: JValue) = path.foldLeft(value) { case (v, pathElem) => v \ pathElem }
    override def toString = path.mkString(".")
    def concat(pathElem: String) = KeyPath(path ++ List(pathElem))
  }
  object KeyPath {
    val root = KeyPath(List())
  }

  trait DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String]
  }
  case class PropertyExists(keyPath: KeyPath) extends DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String] = keyPath(value) match {
      case JNothing => List(keyPath + " required")
      case _ => Nil
    }
    override def toString = keyPath + " exists"
  }

  case class PropertyEnumValues(keyPath: KeyPath, schema: Schema, enumValues: List[Any]) extends DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String] = PropertyExists(keyPath)(value) match {
      case Nil =>
        val actualValue = SchemaBasedJsonDeserializer.extract(keyPath(value), schema)
        actualValue match {
          case Right(actualValue) =>
            Nil
          case Left(errors) =>
            List(toString)
        }
      case errors => errors
    }

    override def toString = enumValues match {
      case List(singleValue) => s"${keyPath} = $singleValue"
      case _ => s"${keyPath} in [${enumValues.mkString(", ")}]"
    }
  }

  case class NoOtherPropertiesThan(keyPath: KeyPath, keys: List[String]) extends DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String] = PropertyExists(keyPath)(value) match {
      case Nil =>
        keyPath(value) match {
          case JObject(values) =>
            values.toList.map(_._1).filterNot(keys.contains(_)) match {
              case Nil => Nil
              case more => List(s"${keyPath} allowed properties [${keys.mkString(", ")}] do not contain ${more}")
            }
          case _ =>
            List(s"$keyPath expected object")
        }
      case errors => errors
    }
    override def toString = s"no other properties than [${keys.mkString(", ")}]"
  }

}

object CustomDeserializers {
  object LocalizedStringDeserializer extends CustomDeserializer {
    def extract(json: JValue, schema: Schema)(implicit context: DeserializationContext): Either[List[DeserializationError], Any] = json match {
      case json: JObject if json.values.contains("fi") => SchemaBasedJsonDeserializer.extractAs(json, classOf[Finnish])
      case json: JObject if json.values.contains("en") => SchemaBasedJsonDeserializer.extractAs(json, classOf[English])
      case json: JObject if json.values.contains("sv") => SchemaBasedJsonDeserializer.extractAs(json, classOf[Swedish])
      case _ => Left(List(DeserializationError(context.path, json, OtherViolation("One of fi, sv, en expected"))))
    }
  }
  val serializers = List((classOf[LocalizedString].getName, LocalizedStringDeserializer))
}