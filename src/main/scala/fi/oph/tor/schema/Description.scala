package fi.oph.tor.schema

import org.json4s.JValue
import org.json4s.JsonAST.JString
import scala.annotation.StaticAnnotation

object Description extends MetadataSupport {
  override val extractMetadata: PartialFunction[(String, List[String]), List[Metadata]] = {
    case (annotationClass, params) if (annotationClass == classOf[Description].getName) =>
      List(Description(params.mkString(" ")))
  }

  override val formatMetadata: PartialFunction[Metadata, List[(String, JValue)]] = {
    case Description(desc) =>
      List("description" -> JString(desc))
  }
}

case class Description(text: String) extends StaticAnnotation with Metadata