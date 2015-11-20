package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{ScalaJsonSchema, Metadata, MetadataSupport, ObjectWithMetadata}
import org.json4s.JsonAST.{JObject, JString}

import scala.annotation.StaticAnnotation

object Description extends MetadataSupport {
  override val applyAnnotations: PartialFunction[(String, List[String], ObjectWithMetadata[_], ScalaJsonSchema), ObjectWithMetadata[_]] = {
    case (annotationClass, params, schemaType: ObjectWithMetadata[_], _) if (annotationClass == classOf[Description].getName) =>
      schemaType.appendMetadata(List(Description(params.mkString(" "))))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case Description(desc) => obj.merge(JObject("description" -> JString(desc)))
    case _ => obj
  }
}

case class Description(text: String) extends StaticAnnotation with Metadata