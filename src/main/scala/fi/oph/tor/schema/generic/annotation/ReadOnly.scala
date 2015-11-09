package fi.oph.tor.schema.generic.annotation

import fi.oph.tor.schema.generic.{Metadata, MetadataSupport}
import org.json4s.JsonAST.JObject

import scala.annotation.StaticAnnotation

object ReadOnly extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[ReadOnly].getName) =>
      List(ReadOnly(params.mkString(" ")))
  }

  override def appendMetadataToJsonSchema(obj: JObject, metadata: Metadata) = metadata match {
    case ReadOnly(desc) =>
      appendToDescription(obj, "\n" + desc)
    case _ => obj
  }
}

case class ReadOnly(desc: String) extends StaticAnnotation with Metadata