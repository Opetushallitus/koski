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
  val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], ScalaJsonSchema), ObjectWithMetadata[_]]
}

trait JsonMetadataSupport {
  def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata): JObject
  def appendToDescription(obj: JObject, koodisto: String): JsonAST.JObject = {
    val description = obj.\("description") match {
      case JString(s) => s
      case JNothing => ""
    }
    obj.merge(JObject("description" -> JString(description + koodisto)))
  }
}
