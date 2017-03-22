package fi.oph.koski.schema

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.json.{GenericJsonFormats, Json}
import fi.oph.koski.schema.AnyOfDeserialization.{CriteriaCollection, DiscriminatorCriterion, extractAnyOf}
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._
import org.json4s._

case class DeserializationContext(rootSchema: SchemaWithClassName, path: String = "", customDeserializers: List[CustomDeserializer] = Nil, validate: Boolean = true, criteriaCache: collection.mutable.Map[String, CriteriaCollection] = collection.mutable.Map.empty) {
  def hasSerializerFor(schema: SchemaWithClassName) = customSerializerFor(schema).isDefined
  def customSerializerFor(schema: SchemaWithClassName) = customDeserializers.find(_.isApplicable(schema))
  def ifValidating(errors: => List[ValidationError]) = if (validate) { errors } else { Nil }
}

trait CustomDeserializer {
  def isApplicable(schema: SchemaWithClassName): Boolean
  def extract(json: JValue, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], Any]
}

sealed trait ValidationRuleViolation
case class MissingProperty(errorType: String = "missingProperty") extends ValidationRuleViolation
case class UnwantedProperty(errorType: String = "unwantedProperty") extends ValidationRuleViolation
case class UnexpectedType(expectedType: String, errorType: String = "unexpectedType") extends ValidationRuleViolation
case class DateFormatMismatch(expectedFormat: String = "yyyy-MM-dd", errorType: String = "dateFormatMismatch") extends ValidationRuleViolation
case class EmptyString(errorType: String = "emptyString") extends ValidationRuleViolation
case class RegExMismatch(regex: String, errorType: String = "regularExpressionMismatch") extends ValidationRuleViolation
case class EnumValueMismatch(allowedValues: List[Any], errorType: String = "enumValueMismatch") extends ValidationRuleViolation
case class NotAnyOf(allowedAlternatives: List[(String, List[String])], errorType: String = "notAnyOf") extends ValidationRuleViolation
case class SmallerThanMinimumValue(minimumValue: Number, errorType: String = "smallerThanMinimumValue") extends ValidationRuleViolation
case class GreaterThanMaximumValue(maximumValue: Number, errorType: String = "greaterThanMaximumValue") extends ValidationRuleViolation
case class SmallerThanOrEqualToExclusiveMinimumValue(exclusiveMinimumValue: Number, errorType: String = "smallerThanOrEqualToExclusiveMinimumValue") extends ValidationRuleViolation
case class GreaterThanOrEqualToExclusiveMaximumValue(exclusiveMaximumValue: Number, errorType: String = "greaterThanOrEqualToExclusiveMaximumValue") extends ValidationRuleViolation
case class LessThanMinimumNumberOfItems(minimumItems: Int, errorType: String = "lessThanMinimumNumberOfItems") extends ValidationRuleViolation
case class MoreThanMaximumNumberOfItems(maximumItems: Int, errorType: String = "moreThanMaximumNumberOfItems") extends ValidationRuleViolation
case class OtherViolation(message: String, errorType: String = "otherViolation") extends ValidationRuleViolation

case class ValidationError(path: String, value: JValue, error: ValidationRuleViolation)

case class DeserializationException(path: String, message: String, cause: Exception) extends RuntimeException(s"Deserialization error at ${path} ${message}: ${cause.getMessage}", cause)
case class TooManyMatchingCasesException(path: String, cases: List[(SchemaWithClassName, CriteriaCollection)], json: JValue) extends RuntimeException(s"Deserialization error at ${path}: more than 1 matching case: \n${cases.map {
  case (schema, criteria) => s"${schema.simpleName} (${criteria.criteria.mkString(", ")})"
}.mkString("\n")}\n\ndata=${Json.writePretty(json)}")
case class SchemaNotFoundException(path: String, className: String) extends RuntimeException(s"Deserialization error at ${path}: schema not found for class ${className}")

case class Discriminator() extends RepresentationalMetadata
case class IgnoreInAnyOfDeserialization() extends RepresentationalMetadata

object SchemaBasedJsonDeserializer {
  private implicit val formats = GenericJsonFormats.genericFormats
  private val noErrors = Nil : List[ValidationError]

  def extract[T](json: JValue)(implicit context: DeserializationContext, mf: ClassManifest[T]): Either[List[ValidationError], T] = {
    val schema = context.rootSchema.getSchema(mf.runtimeClass.getName).get // TODO: check for Option.get in this file
    extract(json, schema, Nil).right.map(_.asInstanceOf[T]) // TODO: delegate to below
  }

  def extract[T](json: String)(implicit context: DeserializationContext, mf: ClassManifest[T]): Either[List[ValidationError], T] = {
    extract(Json.read[JValue](json))
  }


  def extract(json: JValue, klass: Class[_])(implicit context: DeserializationContext): Either[List[ValidationError], AnyRef] = {
    context.rootSchema.getSchema(klass.getName) match {
      case Some(schema) => extract(json, schema, Nil).right.map(_.asInstanceOf[AnyRef])
      case None => throw new SchemaNotFoundException(context.path, klass.getName)
    }
  }

  def extract(json: JValue, schema: Schema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], Any] = {
    schema match {
      case os: OptionalSchema => extractOptional(json, os, metadata)
      case _ => extractRequired(json) { schema match {
        case ls: ListSchema => extractList(json, ls, metadata)
        case ss: StringSchema => extractString(json, ss, metadata)
        case ns: NumberSchema => extractNumber(json, ns, metadata)
        case bs: BooleanSchema => extractBoolean(json, bs, metadata)
        case ds: DateSchema => extractDate(json, ds, metadata)
        case cs: SchemaWithClassName =>
          json match {
            case _: JObject =>
              (context.customSerializerFor(cs), schema) match {
                case (Some(serializer), cs: SchemaWithClassName) => serializer.extract(json, cs, metadata)
                case (_, cs: ClassRefSchema) => extract(json, resolveSchema(cs), metadata)
                case (_, cs: ClassSchema) => extractObject(json, cs, metadata)
                case (_, as: AnyOfSchema) => extractAnyOf(json, as, metadata)
              }
            case _ =>
              Left(List(ValidationError(context.path, json, UnexpectedType("object"))))
          }
      }}
    }
  }

  protected [schema] def resolveSchema(cs: ClassRefSchema)(implicit context: DeserializationContext) = {
    context.rootSchema.getSchema(cs.fullClassName).getOrElse(throw new SchemaNotFoundException(context.path, cs.fullClassName))
  }

  def extractList(json: JValue, as: ListSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], Any] = json match {
    case JArray(values) =>
      val valueResults: List[Either[List[ValidationError], Any]] = values.zipWithIndex.map {
        case (itemJson, index) =>
          extract(itemJson, as.itemSchema, metadata)(context.copy(path = subPath(context.path, index.toString)))
      }

      val metadataValidationErrors: List[ValidationError] = context.ifValidating((as.metadata ++ metadata).collect {
        case MinItems(minItems) if values.length < minItems => ValidationError(context.path, json, LessThanMinimumNumberOfItems(minItems))
        case MaxItems(maxItems) if values.length > maxItems => ValidationError(context.path, json, MoreThanMaximumNumberOfItems(maxItems))
      })

      val errors: List[ValidationError] = valueResults.collect { case Left(errors) => errors }.flatten ++ metadataValidationErrors

      errors match {
        case Nil =>
          Right(valueResults.map(_.right.get))
        case _ => Left(errors)
      }
    case _ => Left(List(ValidationError(context.path, json, UnexpectedType("array"))))
  }

  def extractOptional(json: JValue, as: OptionalSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], AnyRef] = json match {
    case JNothing => Right(None)
    case _ => extract(json, as.itemSchema, metadata).right.map(value => Some(value))
  }

  private def extractObject(json: JValue, schema: ClassSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], AnyRef] = {
    json match {
      case JObject(values) =>
        val propertyResults: List[Either[List[ValidationError], Any]] = schema.properties.map { property =>
          val subContext = context.copy(path = subPath(context.path, property.key))
          val jsonValue = json \ property.key
          extract(jsonValue, property.schema, property.metadata)(subContext)
        }
        val unwantedProperties = values
          .filterNot(pair => schema.properties.find(_.key == pair._1).isDefined)
          .map(pair => ValidationError(subPath(context.path, pair._1), pair._2, UnwantedProperty()))
        val errors: List[ValidationError] = propertyResults.collect { case Left(errors) => errors }.flatten ++ unwantedProperties
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
              case e: Exception => throw new DeserializationException(context.path, s"instantiating ${schema.fullClassName} with ${constructorParams}", e)
            }

          case _ => Left(errors)
        }
      case _ => Left(List(ValidationError(context.path, json, UnexpectedType("object"))))
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

  private def extractNumber(json: JValue, schema: NumberSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], Number] = {
    val extractionResult: Either[List[ValidationError], Number] = json match {
      // NOTE: Number types don't necessarily match correctly and because of type erasure, an Option[Int] may end up containing a Double.
      case JDouble(num: Double) => Right(num)
      case JInt(num: BigInt) => Right(num.intValue)
      case JDecimal(num: BigDecimal) => Right(num.doubleValue)
      case JLong(num) => Right(num.toLong)
      case _ => Left(List(ValidationError(context.path, json, UnexpectedType("number"))))
    }
    extractionResult.right.flatMap { number: Number =>
      context.ifValidating(((metadata ++ schema.metadata).collect {
        case MinValue(minValue) if number.doubleValue < minValue => ValidationError(context.path, json, SmallerThanMinimumValue(minValue))
        case MaxValue(maxValue) if number.doubleValue > maxValue => ValidationError(context.path, json, GreaterThanMaximumValue(maxValue))
        case MinValueExclusive(minValue) if number.doubleValue <= minValue => ValidationError(context.path, json, SmallerThanOrEqualToExclusiveMinimumValue(minValue))
        case MaxValueExclusive(maxValue) if number.doubleValue >= maxValue => ValidationError(context.path, json, GreaterThanOrEqualToExclusiveMaximumValue(maxValue))
      })) ++ {
        verifyEnumValue(schema.enumValues, number).left.getOrElse(Nil)
      } match {
        case Nil => Right(number)
        case errors => Left(errors)
      }
    }
  }

  private def extractBoolean(json: JValue, schema: BooleanSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], Boolean] = json match {
    case JBool(b) =>
      verifyEnumValue[Boolean](schema.enumValues, b)
    case _ =>
      Left(List(ValidationError(context.path, json, UnexpectedType("boolean"))))
  }

  private def extractDate(json: JValue, schema: DateSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], LocalDate] = json match {
    case JString(dateString) => try {
        verifyEnumValue(schema.enumValues, LocalDate.parse(dateString))
      } catch {
        case e: DateTimeParseException => Left(List(ValidationError(context.path, json, DateFormatMismatch())))
      }
    case _ => Left(List(ValidationError(context.path, json, DateFormatMismatch())))
  }

  private def extractRequired[T](json: JValue)(doExtract: => Either[List[ValidationError], T])(implicit context: DeserializationContext) = json match {
    case JNothing =>
      Left(List(ValidationError(context.path, json, MissingProperty())))
    case _ =>
      doExtract
  }

  private def extractString(json: JValue, schema: StringSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], String] = json match {
    case JString(s) =>
      val stringValue = json.extract[String]
      stringValue match {
        case "" if context.validate => Left(List(ValidationError(context.path, json, EmptyString())))
        case _ =>
          val errors = context.ifValidating((schema.metadata ++ metadata).collect {
            case RegularExpression(r) if !stringValue.matches(r) => ValidationError(context.path, json, RegExMismatch(r))
          }) ++ {
            verifyEnumValue(schema.enumValues, stringValue).left.getOrElse(Nil)
          }
          errors match {
            case Nil => Right(stringValue)
            case _ => Left(errors)
          }
      }
    case _ => Left(List(ValidationError(context.path, json, UnexpectedType("string"))))
  }

  private def verifyEnumValue[T <: Any](enumValues: Option[List[Any]], actualValue: T)(implicit context: DeserializationContext): Either[List[ValidationError], T] = {
    enumValues match {
      case Some(values) if !values.contains(actualValue) => Left(List(ValidationError(context.path, toJValue(actualValue), EnumValueMismatch(values))))
      case _ => Right(actualValue)
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
  private def criteriaForSchema(schema: SchemaWithClassName)(implicit context: DeserializationContext) = context.criteriaCache.synchronized {
    context.criteriaCache.getOrElseUpdate(schema.fullClassName, {
      discriminatorCriteria(schema, KeyPath.root)
    })
  }

  def extractAnyOf(json: JValue, as: AnyOfSchema, metadata: List[Metadata])(implicit context: DeserializationContext): Either[List[ValidationError], Any] = {
    val mapping: List[(SchemaWithClassName, CriteriaCollection)] = as.alternatives.filterNot(ignoredAlternative).map { schema =>
      (schema, criteriaForSchema(schema))
    }.sortBy(-_._2.weight)

    val matchingSchemas = mapping.collect {
      case (schema, criteria) if criteria.matches(json) =>
        (schema, criteria)
    }

    /*
    // Just debugging
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
        val allowedAlternatives: List[(String, List[String])] = mapping.map { case (schema, criteria) => (schema.simpleName, criteria.apply(json)) }
        Left(List(ValidationError(context.path, json, NotAnyOf(allowedAlternatives))))
      case _ =>
        val maxWeight = matchingSchemas.head._2.weight
        val schemasWithMaximumNumberOfMatchingCriteria = matchingSchemas.filter { case (_, criteria) => criteria.weight == maxWeight }
        schemasWithMaximumNumberOfMatchingCriteria match {
          case List((schema, criteria)) =>
            SchemaBasedJsonDeserializer.extract(json, schema, metadata)
          case _ =>
            throw new TooManyMatchingCasesException(context.path, schemasWithMaximumNumberOfMatchingCriteria, json)
        }
    }
  }

  private def ignoredAlternative(schema: SchemaWithClassName) = {
    schema.metadata.contains(IgnoreInAnyOfDeserialization())
  }

  private def discriminatorCriteria(schema: SchemaWithClassName, keyPath: KeyPath)(implicit context: DeserializationContext): CriteriaCollection = schema match {
    case s: ClassRefSchema =>
      discriminatorCriteria(SchemaBasedJsonDeserializer.resolveSchema(s), keyPath)
    case s: ClassSchema =>
      val discriminatorProps: List[Property] = s.properties.filter(_.metadata.contains(Discriminator()))
      discriminatorProps match {
        case Nil =>
          CriteriaCollection(NoOtherPropertiesThan(keyPath, s.properties.map(_.key)) :: (s.properties.flatMap(propertyMatchers(keyPath, _))))
        case props =>
          CriteriaCollection(props.flatMap(propertyMatchers(keyPath, _)))
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
          discriminatorCriteria(s, propertyPath).criteria
        }
      case s =>
        List(PropertyExists(propertyPath))
    }
  }.distinct


  case class KeyPath(path: List[String]) {
    def apply(value: JValue) = path.foldLeft(value) { case (v, pathElem) => v \ pathElem }
    def plusSpace = path match {
      case Nil => ""
      case more => toString + " "
    }
    override def toString = path.mkString(".")
    def concat(pathElem: String) = KeyPath(path ++ List(pathElem))
  }
  object KeyPath {
    val root = KeyPath(List())
  }

  trait DiscriminatorCriterion {
    def keyPath: KeyPath
    def apply(value: JValue)(implicit context: DeserializationContext): List[String]
    def description: String
    def withKeyPath(s: String) = keyPath match {
      case KeyPath(Nil) => s
      case _ => s"""property "${keyPath}" ${s}"""
    }
    def weight: Int
    override def toString = withKeyPath(description)
  }

  case class CriteriaCollection(criteria: List[DiscriminatorCriterion]) {
    lazy val weight = criteria.map(_.weight).sum
    def apply(json: JValue)(implicit context: DeserializationContext) = criteria.flatMap(c => c.apply(json))
    def matches(json: JValue)(implicit context: DeserializationContext) = apply(json).isEmpty
  }

  case class PropertyExists(val keyPath: KeyPath) extends DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String] = keyPath(value) match {
      case JNothing => List(toString)
      case _ => Nil
    }
    def description = "exists"
    def weight = 100
  }

  case class PropertyEnumValues(val keyPath: KeyPath, schema: Schema, enumValues: List[Any]) extends DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String] = PropertyExists(keyPath)(value) match {
      case Nil =>
        val actualValue = SchemaBasedJsonDeserializer.extract(keyPath(value), schema, Nil)
        actualValue match {
          case Right(actualValue) =>
            Nil
          case Left(errors) =>
            List(toString)
        }
      case errors => errors
    }

    def description = enumValues match {
      case List(singleValue) => s"= $singleValue"
      case _ => s"in [${enumValues.mkString(", ")}]"
    }

    def weight = 10000
  }

  case class NoOtherPropertiesThan(keyPath: KeyPath, keys: List[String]) extends DiscriminatorCriterion {
    def apply(value: JValue)(implicit context: DeserializationContext): List[String] = PropertyExists(keyPath)(value) match {
      case Nil =>
        keyPath(value) match {
          case JObject(values) =>
            values.toList.map(_._1).filterNot(keys.contains(_)) match {
              case Nil => Nil
              case unwanted => List(withKeyPath(s"allowed properties [${keys.mkString(", ")}] do not contain [${unwanted.mkString(", ")}]")) // TODO: unified list formatting
            }
          case _ =>
            List(withKeyPath("object expected"))
        }
      case errors => errors
    }
    def description = s"no other properties than [${keys.mkString(", ")}]"
    def weight = 1
  }

}



