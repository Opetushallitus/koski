package fi.oph.koski.json

import java.io.InputStream
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import com.github.fge.jsonpatch.diff.JsonDiff
import fi.oph.koski.eperusteet.RakenneOsaSerializer
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.util.Files
import org.json4s
import org.json4s.JsonAST.{JInt, JNull, JString}
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.{JsonMethods, Serialization}

import scala.util.Try

object GenericJsonFormats {
  val genericFormats: Formats =  new DefaultFormats {
    override def dateFormatter = {
      val format = super.dateFormatter
      format.setTimeZone(DefaultFormats.UTC)
      format
    }

    override val strictOptionParsing: Boolean = true
  } ++ JodaTimeSerializers.all
}

object Json extends Logging {
  implicit val jsonFormats = GenericJsonFormats.genericFormats + LocalDateSerializer + LocalDateTimeSerializer + RakenneOsaSerializer

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

  def read[A](json: InputStream)(implicit mf : scala.reflect.Manifest[A]) : A = {
    Serialization.read(json)
  }

  def parse(json: String) = JsonMethods.parse(json)

  def tryParse(json: String): Try[JValue] = Try(parse(json))

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

object LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format => (
  {
    case JString(s) => ContextualExtractor.tryExtract(LocalDateTime.parse(s))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + s))
    case JInt(i) => ContextualExtractor.tryExtract(LocalDateTime.ofInstant(Instant.ofEpochMilli(i.longValue()), ZoneId.of("UTC")))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + i))
    case JNull => ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: null"))
  },
  {
    case d: LocalDateTime => JString(d.toString)
  }
  )
)