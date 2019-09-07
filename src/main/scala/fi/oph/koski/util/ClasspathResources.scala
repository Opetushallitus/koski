package fi.oph.koski.util

import java.io.InputStream

import fi.oph.koski.log.Logging

object ClasspathResources extends Logging {
  def readResourceIfExists[T](resourcename: String, serialize: InputStream => T): Option[T] = {
    try {
      Option(getClass().getResource(resourcename)).map { r =>
        val resource = r.openConnection()
        resource.setUseCaches(false) // To avoid a random "stream closed exception" caused by JRE bug (probably this: https://bugs.openjdk.java.net/browse/JDK-8155607)
      val is = resource.getInputStream()
        try {
          serialize(is)
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
