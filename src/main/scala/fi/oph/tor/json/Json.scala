package fi.oph.tor.json

import java.io.File
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import fi.oph.tor.eperusteet.RakenneOsaSerializer
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema._
import org.json4s
import org.json4s.JsonAST.{JInt, JNull, JString}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s._

object GenericJsonFormats {
  val genericFormats: Formats =  new DefaultFormats {
    override def dateFormatter = {
      val format = super.dateFormatter
      format.setTimeZone(DefaultFormats.UTC)
      format
    }
  } ++ JodaTimeSerializers.all
}

object Json {
  // Find out why SchemaBasedTraitSerializer breaks current serialization
  implicit val jsonFormats = GenericJsonFormats.genericFormats + LocalDateSerializer + RakenneOsaSerializer ++ Deserializers.deserializers

  def write(x: AnyRef, pretty: Boolean = false): String = {
    if (pretty) {
      writePretty(x)
    } else {
      Serialization.write(x);
    }
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

  def maskSensitiveInformation(parsedJson: JValue): JValue = {
    val maskedJson = parsedJson.mapField {
      case ("hetu", JString(_)) => ("hetu", JString("******-****"))
      case x => x
    }
    maskedJson
  }
}

object LocalDateSerializer extends CustomSerializer[LocalDate](format => (
  {
    case JString(s) => ContextualExtractor.tryExtract(LocalDate.parse(s))(HttpStatus.badRequest("Virheellinen päivämäärä: " + s))
    case JInt(i) => ContextualExtractor.tryExtract(LocalDateTime.ofInstant(Instant.ofEpochMilli(i.longValue()), ZoneId.of("UTC")).toLocalDate())(HttpStatus.badRequest("Virheellinen päivämäärä: " + i))
    case JNull => null
  },
  {
    case d: LocalDate => JString(d.toString)
  }
  )
)
