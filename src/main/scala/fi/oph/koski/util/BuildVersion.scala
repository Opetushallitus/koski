package fi.oph.koski.util

import java.io.InputStream
import java.util.Properties
import scala.util.Using

case class BuildVersion(version: Option[String], vcsRevision: Option[String], buildDate: Option[String])

object BuildVersion {
  def read(): Option[BuildVersion] = read(getClass.getResourceAsStream("/buildversion.txt"))

  def read(stream: => InputStream): Option[BuildVersion] =
    Option(stream).map { input =>
      Using.resource(input) { resource =>
        val properties = new Properties()
        properties.load(resource)
        BuildVersion(
          version = Option(properties.getProperty("version")),
          vcsRevision = Option(properties.getProperty("vcsRevision")),
          buildDate = Option(properties.getProperty("buildDate"))
        )
      }
    }
}
