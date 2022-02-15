package fi.oph.koski.json

import java.io.InputStream
import fi.oph.koski.log.Logging
import fi.oph.koski.util.ClasspathResource
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.io.Source.fromInputStream

object JsonResources extends Logging {
  def readResource(resourcename: String): JValue = readResourceIfExists(resourcename).getOrElse(throw new RuntimeException(s"Resource $resourcename not found"))

  def readResourceIfExists(resourcename: String): Option[JValue] =
    ClasspathResource.resourceSerializer(resourcename)((is: InputStream) => JsonMethods.parse(fromInputStream(is).mkString))
}
