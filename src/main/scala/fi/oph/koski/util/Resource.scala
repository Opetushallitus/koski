package fi.oph.koski.util

import java.io.InputStream

import fi.oph.koski.log.Logging
import fi.oph.koski.util.Resource.ResourceSerializer

object Resource {
  type ResourceSerializer[T] = (InputStream => T) => Option[T]
}

trait Resource extends Logging {
  def resourceSerializer[T](resourceName: String): ResourceSerializer[T] = serializer =>
    inputStream(resourceName).map { is =>
      try {
        serializer(is)
      } finally {
        is.close()
      }
    }

  private def inputStream(resourceName: String) = try {
    getInputStream(resourceName)
  } catch {
    case e: Exception =>
      logger.error(s"Loading resource $resourceName failed")
      throw e
  }

  def getInputStream(resourceName: String): Option[InputStream]
  def exists(resourceName: String): Boolean
}

