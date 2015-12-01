package fi.oph.tor.json

import java.io.File
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import fi.oph.tor.eperusteet.RakenneOsaSerializer
import fi.oph.tor.schema._
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s
import org.json4s.JsonAST.{JInt, JNull, JString}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.{CustomSerializer, Extraction, JValue}

object Json {
  // Find out why SchemaBasedTraitSerializer breaks current serialization
  implicit val jsonFormats = GenericJsonFormats.genericFormats + new LocalDateSerializer +
    new RakenneOsaSerializer ++ Deserializers.deserializers

  def write(x: AnyRef): String = {
    Serialization.write(x);
  }

  def writePretty(x: AnyRef): String = {
    Serialization.writePretty(x);
  }

  def read[A](json: String)(implicit mf : scala.reflect.Manifest[A]) : A = {
    Serialization.read(json)
  }

  def toJValue(x: AnyRef): JValue = {
    Extraction.decompose(x)
  }

  def fromJValue[A](x: JValue)(implicit mf : scala.reflect.Manifest[A]): A = {
    x.extract[A]
  }


  def readFile(filename: String): json4s.JValue = {
    parse(scala.io.Source.fromFile(filename).mkString)
  }

  def readFileIfExists(filename: String): Option[json4s.JValue] = {
    if (new File(filename).exists()) {
      Some(Json.readFile(filename))
    } else {
      None
    }
  }

  def writeFile(filename: String, json: AnyRef) = {
    import java.nio.charset.StandardCharsets
    import java.nio.file.{Files, Paths}

    Files.write(Paths.get(filename), writePretty(json).getBytes(StandardCharsets.UTF_8))
  }
}

class LocalDateSerializer extends CustomSerializer[LocalDate](format => (
  {
    case JString(s) => LocalDate.parse(s)
    case JInt(i) => LocalDateTime.ofInstant(Instant.ofEpochMilli(i.longValue()), ZoneId.of("UTC")).toLocalDate()
    case JNull => null
  },
  {
    case d: LocalDate => JString(d.toString)
  }
  )
)
