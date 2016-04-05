package fi.oph.tor.schema.generic

import org.json4s.JsonAST
import org.json4s.JsonAST.{JNothing, JObject, JString}

trait Metadata

trait ObjectWithMetadata[T <: ObjectWithMetadata[T]] {
  def metadata: List[Metadata]
  def replaceMetadata(newMetadata: List[Metadata]): ObjectWithMetadata[T]
  def appendMetadata(newMetadata: List[Metadata]): ObjectWithMetadata[T] = replaceMetadata(metadata ++ newMetadata)
}

trait MetadataSupport extends AnnotationSupport with JsonMetadataSupport

trait AnnotationSupport {
  def apply(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_]

  def applyAnnotations(annotationClass: String, params: List[String], x: ObjectWithMetadata[_], schemaFactory: SchemaFactory) = if (annotationClass == myAnnotationClass.getName) {
    apply(x, params, schemaFactory)
  } else {
    x
  }

  def myAnnotationClass: Class[_]
}

trait JsonMetadataSupport {
  def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata): JObject
  def appendToDescription(obj: JObject, newDescription: String): JsonAST.JObject = {
    val description = obj.\("description") match {
      case JString(s) => s + "\n" + newDescription
      case JNothing => newDescription
    }
    obj.merge(JObject("description" -> JString(description)))
  }

  def addEnumValue(value: String, p: Property): Property = {
    val newSchema = p.schema match {
      case StringSchema(enumValues) =>
        StringSchema(Some(enumValues.toList.flatten ++ List(value)))
      case x: Any => throw new RuntimeException("Unexpected schema: " + x)

    }
    p.copy(schema = newSchema)
  }
}
