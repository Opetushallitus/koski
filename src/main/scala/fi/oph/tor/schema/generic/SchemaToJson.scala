package fi.oph.tor.schema.generic

import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s.Extraction
import org.json4s.JsonAST._

import scala.reflect.runtime.{universe => ru}
object SchemaToJson {
  def toJsonSchema(t: SchemaType)(implicit s: ScalaJsonSchema): JValue = t match {
    case DateType(enumValues) => JObject(List("type" -> JString("string"), "format" -> JString("date")) ++ toEnumValueProperty(enumValues))
    case StringType(enumValues) => simpleObjectToJson("string", enumValues)
    case BooleanType(enumValues) => simpleObjectToJson("boolean", enumValues)
    case NumberType(enumValues) => simpleObjectToJson("number", enumValues)
    case ListType(x) => JObject(("type") -> JString("array"), (("items" -> toJsonSchema(x))))
    case OptionalType(x) => toJsonSchema(x)
    case t: ClassTypeRef => appendMetadata(
      JObject(
        ("$ref" -> JString("#/definitions/" + t.simpleName))
      ),
      t.metadata
    )
    case t: ClassType => appendMetadata(
      JObject(List(
        ("type" -> JString("object")),
        ("properties" -> toJsonProperties(t.properties)),
        ("id" -> JString("#" + t.simpleName)),
        ("additionalProperties" -> JBool(false))
      ) ++ toRequiredProperties(t.properties).toList
        ++ toDefinitionProperty(t.definitions).toList
      ),
      t.metadata
    )
    case OneOf(types) => JObject(("oneOf" -> JArray(types.map(toJsonSchema(_)))))
  }

  def simpleObjectToJson(tyep: String, enumValues: Option[List[Any]]) = {
    JObject(List("type" -> JString(tyep)) ++ toEnumValueProperty(enumValues))
  }

  def toEnumValueProperty(enumValues: Option[List[Any]]): Option[(String, JValue)] = {
    implicit val formats = GenericJsonFormats.genericFormats
    enumValues.map(enumValues => ("enum", Extraction.decompose(enumValues)))
  }

  private def toJsonProperties(properties: List[Property])(implicit s: ScalaJsonSchema): JValue = {
    JObject(properties
      .map { property =>
      val propertyMetadata: JObject = appendMetadata(toJsonSchema(property.tyep).asInstanceOf[JObject], property.metadata)
      (property.key, propertyMetadata)
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

  private def toDefinitionProperty(definitions: List[ClassType])(implicit s: ScalaJsonSchema): Option[(String, JValue)] = definitions match {
    case Nil => None
    case xs =>
      Some("definitions", JObject(definitions.map(definition => (definition.simpleName, toJsonSchema(definition)))))
  }

  private def appendMetadata(obj: JObject, metadata: List[Metadata])(implicit s: ScalaJsonSchema): JObject = {
    metadata.foldLeft(obj) { case (obj: JObject, metadata) =>
      s.metadatasSupported.foldLeft(obj) { case (obj, metadataSupport) =>
        metadataSupport.appendMetadataToJsonSchema(obj, metadata)
      }
    }
  }
}
