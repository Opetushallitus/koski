package fi.oph.koski.json

import fi.oph.koski.log.Logging
import org.json4s
import org.json4s.StreamInput
import org.json4s.jackson.JsonMethods

object JsonResources extends Logging {
  def readResource(resourcename: String): json4s.JValue = readResourceIfExists(resourcename).getOrElse(throw new RuntimeException(s"Resource $resourcename not found"))

  def readResourceIfExists(resourcename: String): Option[json4s.JValue] = {
    try {
      Option(getClass().getResource(resourcename)).map { r =>
        val resource = r.openConnection()
        resource.setUseCaches(false) // To avoid a random "stream closed exception" caused by JRE bug (probably this: https://bugs.openjdk.java.net/browse/JDK-8155607)
      val is = resource.getInputStream()
        try {
          JsonMethods.parse(StreamInput(is))
        } finally {
          is.close()
        }
      }
    } catch {
      case e: Exception =>
        logger.error("Load resource " + resourcename + " failed")
        throw e
    }
  }
}
