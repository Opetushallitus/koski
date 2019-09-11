package fi.oph.koski.util

import java.io.InputStream
import java.net.URL
import java.nio.file.Paths

import fi.oph.koski.util.Resource.ResourceSerializer

object ClasspathResource {
  def resourceSerializer[T](resourceName: String): ResourceSerializer[T] =
    new ClasspathResource("").resourceSerializer[T](resourceName)
}

class ClasspathResource(val resourceRoot: String) extends Resource {
  override def getInputStream(resourceName: String): Option[InputStream] = getResource(resourceName).map { r =>
    val resource = r.openConnection()
    resource.setUseCaches(false) // To avoid a random "stream closed exception" caused by JRE bug (probably this: https://bugs.openjdk.java.net/browse/JDK-8155607)
    resource.getInputStream
  }

  override def exists(resourceName: String): Boolean = getResource(resourceName).isDefined

  def getResource(resourceName: String): Option[URL] =
    Option(getClass.getResource(Paths.get(resourceRoot, resourceName).toString))
}
