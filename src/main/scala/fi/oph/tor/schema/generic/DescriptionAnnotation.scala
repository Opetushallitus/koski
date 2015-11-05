package fi.oph.tor.schema.generic

import org.json4s.JsonAST.{JObject, JString}

import scala.annotation.StaticAnnotation

object DescriptionAnnotation extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[DescriptionAnnotation].getName) =>
      List(DescriptionAnnotation(params.mkString(" ")))
  }

  override def appendMetadata(obj: JObject, metadata: Metadata) = metadata match {
    case DescriptionAnnotation(desc) => obj.merge(JObject("description" -> JString(desc)))
    case _ => obj
  }
}

case class DescriptionAnnotation(text: String) extends StaticAnnotation with Metadata