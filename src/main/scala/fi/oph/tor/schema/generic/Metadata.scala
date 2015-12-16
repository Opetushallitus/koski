package fi.oph.tor.schema.generic

import org.json4s.JsonAST
import org.json4s.JsonAST.{JNothing, JString, JObject}

trait Metadata

trait ObjectWithMetadata[T <: ObjectWithMetadata[T]] {
  def metadata: List[Metadata]
  def replaceMetadata(newMetadata: List[Metadata]): ObjectWithMetadata[T]
  def appendMetadata(newMetadata: List[Metadata]): ObjectWithMetadata[T] = replaceMetadata(metadata ++ newMetadata)
}

trait MetadataSupport extends AnnotationSupport with JsonMetadataSupport {

}

trait AnnotationSupport {
  val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], SchemaFactory), ObjectWithMetadata[_]]
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
      case x => throw new RuntimeException("Unexpected schema: " + x)

    }
    p.copy(schema = newSchema)
  }
}
