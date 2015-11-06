package fi.oph.tor.schema.generic

import org.json4s.JsonAST._
import org.reflections.Reflections

import scala.reflect.runtime.{universe => ru}

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
case class ClassType(fullClassName: String, properties: List[Property], override val metadata: List[Metadata]) extends SchemaType with TypeWithClassName {
  def getPropertyValue(property: Property, target: AnyRef): AnyRef = {
    target.getClass.getMethod(property.key).invoke(target)
  }
}
case class ClassTypeRef(fullClassName: String) extends SchemaType with TypeWithClassName
case class OneOf(types: List[TypeWithClassName]) extends SchemaType {
  def matchType(obj: AnyRef): SchemaType = {
    types.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}
trait TypeWithClassName extends SchemaType {
  def fullClassName: String
}

case class Property(key: String, tyep: SchemaType, metadata: List[Metadata])

trait Metadata
trait MetadataSupport {
  val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]]
  def appendMetadata(obj: JObject, metadata: Metadata): JObject
}

class ScalaJsonSchema(metadatasSupported: MetadataSupport*) {
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

  def metadataForSymbol(symbol: ru.Symbol): List[Metadata] = {
    symbol.annotations.flatMap { annotation =>
      metadatasSupported.flatMap { metadataSupport =>
        val f: PartialFunction[(String, List[String]), List[Metadata]] = metadataSupport.extractMetadata orElse { case _ => Nil }

        f(annotation.tree.tpe.toString, annotation.tree.children.tail.map(_.toString.replaceAll("\"$|^\"", "").replace("\\\"", "\"").replace("\\'", "'")))
      }
    }
  }

  private def createClassSchema(tpe: ru.Type, previousTypes: collection.mutable.Set[String]): SchemaType = {
    val className: String = tpe.typeSymbol.fullName
    if (previousTypes.contains(className)) {
      ClassTypeRef(className)
    } else {
      previousTypes.add(className)

      val params = tpe.typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.head
      val propertiesList: List[Property] = params.map{ paramSymbol =>
        val term = paramSymbol.asTerm
        val termType = createSchema(term.typeSignature, previousTypes)
        val termName: String = term.name.decoded.trim
        Property(termName, termType, metadataForSymbol(term))
      }.toList.sortBy(_.key)

      ClassType(className, propertiesList, metadataForSymbol(tpe.typeSymbol))
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

  def createSchema(className: String): SchemaType = {
    createSchema(reflect.runtime.currentMirror.classSymbol(Class.forName(className)).toType)
  }

  def createSchemaForObject(obj: AnyRef): SchemaType = createSchema(obj.getClass.getName)

  def findImplementations(tpe: ru.Type, previousTypes: collection.mutable.Set[String]): List[TypeWithClassName] = {
    import collection.JavaConverters._
    import reflect.runtime.currentMirror

    val javaClass: Class[_] = Class.forName(tpe.typeSymbol.asClass.fullName)
    val reflections = new Reflections(javaClass.getPackage.getName)

    val implementationClasses = reflections.getSubTypesOf(javaClass).asScala

    implementationClasses.toList.map { klass =>
      createSchema(currentMirror.classSymbol(klass).toType, previousTypes).asInstanceOf[TypeWithClassName]
    }
  }

  def appendMetadata(obj: JObject, metadata: List[Metadata]): JObject = {
    metadata.foldLeft(obj) { case (obj: JObject, metadata) =>
      metadatasSupported.foldLeft(obj) { case (obj, metadataSupport) =>
        metadataSupport.appendMetadata(obj, metadata)
      }
    }
  }

  private def toJsonProperties(properties: List[Property]): JValue = {
    JObject(properties
      .map { property =>
        (property.key, appendMetadata(toJsonSchema(property.tyep).asInstanceOf[JObject], property.metadata))
      }
    )
  }
  private def toRequiredProperties(properties: List[Property]): Option[(String, JValue)] = {
    val requiredProperties = properties.toList.filter(!_.tyep.isInstanceOf[OptionalType])
    requiredProperties match {
      case Nil => None
      case _ => Some("required", JArray(requiredProperties.map{property => JString(property.key)}))
    }
  }

  def descriptionJson(description: Option[String]): Option[(String, JValue)] = description.map(("description" -> JString(_)))

  def toJsonSchema(t: SchemaType): JValue = t match {
    case DateType() => JObject(("type" -> JString("string")), ("format" -> JString("date")))
    case StringType() => JObject(("type" -> JString("string")))
    case BooleanType() => JObject(("type") -> JString("boolean"))
    case NumberType() => JObject(("type") -> JString("number"))
    case ListType(x) => JObject(("type") -> JString("array"), (("items" -> toJsonSchema(x))))
    case OptionalType(x) => toJsonSchema(x)
    case ClassTypeRef(fullClassName: String) => JObject(("$ref") -> toUri(fullClassName))
    case ClassType(fullClassName, properties, metadata) => appendMetadata(JObject(List(("type" -> JString("object")), ("properties" -> toJsonProperties(properties)), ("id" -> toUri(fullClassName))) ++ toRequiredProperties(properties).toList), metadata)
    case OneOf(types) => JObject(("oneOf" -> JArray(types.map(toJsonSchema(_)))))
  }

  def toUri(fullClassName: String) = JString("#" + fullClassName.replace(".", "_"))
}