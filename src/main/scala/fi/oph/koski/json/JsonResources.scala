package fi.oph.koski.json

import java.io.InputStream

import fi.oph.koski.log.Logging
import fi.oph.koski.util.ClasspathResources
import org.json4s
import org.json4s.StreamInput
import org.json4s.jackson.JsonMethods

object JsonResources extends Logging {
  def readResource(resourcename: String): json4s.JValue = readResourceIfExists(resourcename).getOrElse(throw new RuntimeException(s"Resource $resourcename not found"))

  def readResourceIfExists(resourcename: String): Option[json4s.JValue] =
    ClasspathResources.readResourceIfExists(resourcename, (is: InputStream) => JsonMethods.parse(StreamInput(is)))
}
