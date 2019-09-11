package fi.oph.koski.util

import java.io.{BufferedInputStream, InputStream}
import java.nio.file.Files.{exists => fileExists, newInputStream}
import java.nio.file.Paths

class FileResource(val resourceRoot: String) extends Resource {
  override def getInputStream(resourceName: String): Option[InputStream] = try {
    Some(new BufferedInputStream(newInputStream(Paths.get(resourceRoot, resourceName))))
  } catch {
    case e: Exception =>
      logger.error(e)(s"Problem accessing $resourceRoot/$resourceName")
      None
  }

  override def exists(resourceName: String): Boolean = fileExists(Paths.get(resourceRoot, resourceName))
}
