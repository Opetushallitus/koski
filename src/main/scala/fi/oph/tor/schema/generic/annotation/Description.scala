package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport}
import org.json4s.JsonAST.{JObject, JString}

import scala.annotation.StaticAnnotation

object Description extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[Description].getName) =>
      List(Description(params.mkString(" ")))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case Description(desc) => obj.merge(JObject("description" -> JString(desc)))
    case _ => obj
  }
}

case class Description(text: String) extends StaticAnnotation with Metadata