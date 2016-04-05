package fi.oph.tor.schema.generic

import org.json4s.JsonAST
import org.json4s.JsonAST.{JNothing, JObject, JString}
import scala.annotation.StaticAnnotation

trait Metadata extends StaticAnnotation

trait ObjectWithMetadata[T <: ObjectWithMetadata[T]] {
  def metadata: List[Metadata]
  def replaceMetadata(newMetadata: List[Metadata]): ObjectWithMetadata[T]
  def appendMetadata(newMetadata: List[Metadata]): ObjectWithMetadata[T] = replaceMetadata(metadata ++ newMetadata)
}

trait MetadataSupport[M] extends AnnotationSupport[M] with JsonMetadataSupport[M]

trait AnnotationSupport[M] {
  def applyAnnotation(x: ObjectWithMetadata[_], params: List[String], schemaFactory: SchemaFactory): ObjectWithMetadata[_]

  def applyAnnotations(annotationClass: String, params: List[String], x: ObjectWithMetadata[_], schemaFactory: SchemaFactory) = if (annotationClass == metadataClass.getName) {
    applyAnnotation(x, params, schemaFactory)
  } else {
    x
  }

  def metadataClass: Class[M]
}

trait JsonMetadataSupport[M] {
  def appendMetadataToJsonSchema(obj: JObject, metadata: M): JObject

  def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata): JObject = if (metadataClass.isInstance(metadata)) {
    appendMetadataToJsonSchema(obj, metadata.asInstanceOf[M])
  } else {
    obj
  }
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

  def metadataClass: Class[M]
}
