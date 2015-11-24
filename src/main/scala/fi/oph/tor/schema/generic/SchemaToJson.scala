package fi.oph.tor.schema.generic

import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s.Extraction
import org.json4s.JsonAST._

object SchemaToJson {
  def toJsonSchema(t: Schema)(implicit ms: List[JsonMetadataSupport]): JValue = t match {
    case DateSchema(enumValues) => JObject(List("type" -> JString("string"), "format" -> JString("date")) ++ toEnumValueProperty(enumValues))
    case StringSchema(enumValues) => simpleObjectToJson("string", enumValues)
    case BooleanSchema(enumValues) => simpleObjectToJson("boolean", enumValues)
    case NumberSchema(enumValues) => simpleObjectToJson("number", enumValues)
    case ListSchema(x) => JObject(("type") -> JString("array"), (("items" -> toJsonSchema(x))))
    case OptionalSchema(x) => toJsonSchema(x)
    case t: ClassRefSchema => appendMetadata(
      JObject(
        ("$ref" -> JString("#/definitions/" + t.simpleName))
      ),
      t.metadata
    )
    case t: ClassSchema => appendMetadata(
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
    case OneOfSchema(alternatives, _) => JObject(("oneOf" -> JArray(alternatives.map(toJsonSchema(_)))))
  }

  def simpleObjectToJson(tyep: String, enumValues: Option[List[Any]]) = {
    JObject(List("type" -> JString(tyep)) ++ toEnumValueProperty(enumValues))
  }

  def toEnumValueProperty(enumValues: Option[List[Any]]): Option[(String, JValue)] = {
    implicit val formats = GenericJsonFormats.genericFormats
    enumValues.map(enumValues => ("enum", Extraction.decompose(enumValues)))
  }

  private def toJsonProperties(properties: List[Property])(implicit ms: List[JsonMetadataSupport]): JValue = {
    JObject(properties
      .map { property =>
      val propertyMetadata: JObject = appendMetadata(toJsonSchema(property.tyep).asInstanceOf[JObject], property.metadata)
      (property.key, propertyMetadata)
    }
    )
  }
  private def toRequiredProperties(properties: List[Property]): Option[(String, JValue)] = {
    val requiredProperties = properties.toList.filter(!_.tyep.isInstanceOf[OptionalSchema])
    requiredProperties match {
      case Nil => None
      case _ => Some("required", JArray(requiredProperties.map{property => JString(property.key)}))
    }
  }

  private def toDefinitionProperty(definitions: List[SchemaWithClassName])(implicit ms: List[JsonMetadataSupport]): Option[(String, JValue)] = definitions.flatMap {
    case x: ClassSchema => List(x)
    case _ => Nil
  } match {
    case Nil => None
    case xs =>
      Some("definitions", JObject(definitions.map(definition => (definition.simpleName, toJsonSchema(definition)))))
  }

  private def appendMetadata(obj: JObject, metadata: List[Metadata])(implicit ms: List[JsonMetadataSupport]): JObject = {
    metadata.foldLeft(obj) { case (obj: JObject, metadata) =>
      ms.foldLeft(obj) { case (obj, metadataSupport) =>
        metadataSupport.appendMetadataToJsonSchema(obj, metadata)
      }
    }
  }
}
