package fi.oph.koski.json

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import com.github.fge.jsonpatch.diff.JsonDiff
import fi.oph.koski.eperusteet.RakenneOsaSerializer
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.util.Files
import org.json4s
import org.json4s.JsonAST.{JInt, JNull, JString}
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.{JsonMethods, Serialization}

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
    readFileIfExists(filename).get
  }

  def readFileIfExists(filename: String): Option[json4s.JValue] = Files.asString(filename).map(parse(_))

  def writeFile(filename: String, json: AnyRef) = {
    import java.nio.charset.StandardCharsets
    import java.nio.file.{Files, Paths}

    Files.write(Paths.get(filename), writePretty(json).getBytes(StandardCharsets.UTF_8))
  }

  def maskSensitiveInformation(parsedJson: JValue): JValue = {
    val maskedJson = parsedJson.mapField {
      case ("hetu", JString(_)) => ("hetu", JString("******-****"))
      case field: (String, JsonAST.JValue) => field
    }
    maskedJson
  }

  def jsonDiff(oldValue: JValue, newValue: JValue): JArray = {
    JsonMethods.fromJsonNode(JsonDiff.asJson(JsonMethods.asJsonNode(oldValue), JsonMethods.asJsonNode(newValue))).asInstanceOf[JArray]
  }

  def jsonDiff(oldValue: AnyRef, newValue: AnyRef): JArray = {
    jsonDiff(toJValue(oldValue), toJValue(newValue))
  }
}

object LocalDateSerializer extends CustomSerializer[LocalDate](format => (
  {
    case JString(s) => ContextualExtractor.tryExtract(LocalDate.parse(s))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + s))
    case JInt(i) => ContextualExtractor.tryExtract(LocalDateTime.ofInstant(Instant.ofEpochMilli(i.longValue()), ZoneId.of("UTC")).toLocalDate())(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + i))
    case JNull => ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: null"))
  },
  {
    case d: LocalDate => JString(d.toString)
  }
  )
)
