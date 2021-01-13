package fi.oph.common.json

import fi.oph.koski.util.Files
import org.json4s
import org.json4s.jackson.JsonMethods.parse

import scala.reflect.runtime.universe.TypeTag

object JsonFiles {
  def readFile(filename: String): json4s.JValue = {
    readFileIfExists(filename).get
  }

  def readFileIfExists(filename: String): Option[json4s.JValue] = Files.asString(filename).map(parse(_))

  def writeFile[T : TypeTag](filename: String, json: T) = {
    Files.writeFile(filename, JsonSerializer.writeWithRoot(json, pretty = true))
  }
}
