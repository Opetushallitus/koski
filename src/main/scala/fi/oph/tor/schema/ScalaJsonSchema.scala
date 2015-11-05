package fi.oph.tor.schema

import org.json4s.JsonAST._
import org.reflections.Reflections

import scala.annotation.StaticAnnotation
import scala.reflect.api.JavaUniverse
import scala.reflect.runtime.{universe => ru}

sealed trait SchemaType
case class OptionalType(x: SchemaType) extends SchemaType
case class ListType(x: SchemaType) extends SchemaType
case class DateType() extends SchemaType
case class StringType() extends SchemaType
case class BooleanType() extends SchemaType
case class NumberType() extends SchemaType
case class ClassType(fullClassName: String, properties: Map[String, SchemaType]) extends SchemaType
case class ClassTypeRef(fullClassName: String) extends SchemaType
case class OneOf(types: List[SchemaType]) extends SchemaType

case class Implementations(is: List[Class[_]]) extends StaticAnnotation

object ScalaJsonSchema {
  private lazy val schemaTypeForScala = Map(
    "org.joda.time.DateTime" -> DateType(),
    "java.util.Date" -> DateType(),
    "java.time.LocalDate" -> DateType(),
    "java.lang.String" -> StringType(),
    "scala.Boolean" -> BooleanType(),
    "scala.Int" -> NumberType(),
    "scala.Long" -> NumberType(),
    "scala.Double" -> NumberType()
  )

  private def createClassSchema(tpe: ru.Type, previousTypes: collection.mutable.Set[String]): SchemaType = {
    val className: String = tpe.typeSymbol.fullName
    if (previousTypes.contains(className)) {
      ClassTypeRef(className)
    } else {
      previousTypes.add(className)
      val propertiesList: List[(String, SchemaType)] = tpe.members.flatMap { member =>
        if (member.isTerm) {
          val term = member.asTerm
          if ((term.isVal || term.isVar)) {
            val termType = createSchema(term.typeSignature, previousTypes)
            val termName: String = term.name.decoded.trim
            Some(termName -> termType)
          } else {
            None
          }
        } else {
          None
        }
      }.toList.sortBy(_._1)

      ClassType(className, propertiesList.toMap)
    }
  }


  def createSchema(tpe: ru.Type, previousTypes: collection.mutable.Set[String] = collection.mutable.Set.empty): SchemaType = {
    val typeName = tpe.typeSymbol.fullName

    if (typeName == "scala.Option") {
      // Option[T] becomes the schema of T with required set to false
      OptionalType(createSchema(tpe.asInstanceOf[ru.TypeRefApi].args.head, previousTypes))
    } else if (tpe.baseClasses.exists(s => s.fullName == "scala.collection.Traversable" ||
      s.fullName == "scala.Array" ||
      s.fullName == "scala.Seq" ||
      s.fullName == "scala.List" ||
      s.fullName == "scala.Vector")) {
      // (Traversable)[T] becomes a schema with items set to the schema of T
      ListType(createSchema(tpe.asInstanceOf[ru.TypeRefApi].args.head, previousTypes))
    } else {
      schemaTypeForScala.getOrElse(typeName, {
        if (tpe.typeSymbol.isClass) {
          if (tpe.typeSymbol.isAbstract) {
            OneOf(findImplementations(tpe, previousTypes))
          } else {
            createClassSchema(tpe, previousTypes)
          }
        } else {
          throw new RuntimeException("What is this type: " + tpe)
        }
      })
    }
  }


  def findImplementations(tpe: ru.Type, previousTypes: collection.mutable.Set[String]): List[SchemaType] = {
    import collection.JavaConverters._
    import reflect.runtime.currentMirror
    val reflections = new Reflections("fi.oph.tor.schema")
    val clazz: JavaUniverse#ClassSymbol = tpe.typeSymbol.asClass
    val implementationClasses = reflections.getSubTypesOf(Class.forName(clazz.fullName)).asScala

    implementationClasses.toList.map { klass =>
      createSchema(currentMirror.classSymbol(klass).toType, previousTypes)
    }
  }

  private def toJsonProperties(properties: Map[String, SchemaType]): JValue = {
    JObject(properties
      .mapValues { tyep => toJsonSchema(tyep)}
      .toList
    )
  }
  private def toRequiredProperties(properties: Map[String, SchemaType]): Option[(String, JValue)] = {
    val requiredProperties: List[(String, SchemaType)] = properties.toList.filter(!_._2.isInstanceOf[OptionalType])
    requiredProperties match {
      case Nil => None
      case _ => Some("required", JArray(requiredProperties.map{case (key, tyep) => JString(key)}))
    }
  }

  def toJsonSchema(t: SchemaType): JValue = t match {
    case DateType() => JObject(("type" -> JString("string")), ("format" -> JString("date")))
    case StringType() => JObject(("type" -> JString("string")))
    case BooleanType() => JObject(("type") -> JString("boolean"))
    case NumberType() => JObject(("type") -> JString("number"))
    case ListType(x) => JObject(("type") -> JString("array"), (("items" -> toJsonSchema(x))))
    case OptionalType(x) => toJsonSchema(x)
    case ClassTypeRef(fullClassName: String) => JObject(("$ref") -> toUri(fullClassName))
    case ClassType(fullClassName, properties) => JObject(List(("type" -> JString("object")),("properties" -> toJsonProperties(properties)), ("id" -> toUri(fullClassName))) ++ toRequiredProperties(properties).toList)
    case OneOf(types) => JObject(("oneOf" -> JArray(types.map(toJsonSchema(_)))))
  }

  def toUri(fullClassName: String) = JString("#" + fullClassName.replace(".", "_"))
}