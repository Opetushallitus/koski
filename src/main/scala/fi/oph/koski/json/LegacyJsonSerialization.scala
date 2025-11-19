package fi.oph.koski.json

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Opiskeluoikeus, Suoritus}
import fi.oph.koski.servlet.InvalidRequestException
import org.json4s.JsonAST.{JDouble, JInt, JNull, JString}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, JValue, Serializer}

/**
  * JSON (de)serialization with json4s extraction/serialization mechanisms
  */
object LegacyJsonSerialization {
  implicit val jsonFormats: Formats = GenericJsonFormats.genericFormats + LocalDateSerializer + LocalDateTimeSerializer + BlockOpiskeluoikeusSerializer

  def toJValue(x: Map[_, _]): JValue = {
    Extraction.decompose(x)
  }

  def toJValue(x: List[_]): JValue = {
    Extraction.decompose(x)
  }
}


// Estää opiskeluoikeuksien serialisoimisen vahingosssa ilman arkaluontoisten kenttien filtteröintiä
private object BlockOpiskeluoikeusSerializer extends Serializer[Opiskeluoikeus] {
  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats) = {
    case x: Opiskeluoikeus => fail(x.getClass.getName)
    case x: Suoritus => fail(x.getClass.getName)
  }

  def fail(name: String) = throw new RuntimeException(s"$name-luokan serialisointi estetty, käytä fi.oph.scalaschema.Serializer-luokkaa")
}

private object ExtractionHelper {
  def tryExtract[T](block: => T)(status: => HttpStatus) = {
    try {
      block
    } catch {
      case e: Exception => throw new InvalidRequestException(status)
    }
  }
}


object LocalDateSerializer extends CustomSerializer[LocalDate](format => ( {
    case JString(s) => ExtractionHelper.tryExtract(LocalDate.parse(s))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + s))
    case JInt(i) => ExtractionHelper.tryExtract(LocalDateTime.ofInstant(Instant.ofEpochMilli(i.longValue), ZoneId.of("UTC")).toLocalDate())(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + i))
    case JNull => throw new InvalidRequestException(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: null"))
  }, {
    case d: LocalDate => JString(d.toString)
  })
)

object LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format => ( {
    case JString(s) => ExtractionHelper.tryExtract(LocalDateTime.parse(s))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + s))
    case JDouble(i) => ExtractionHelper.tryExtract(LocalDateTime.ofInstant(Instant.ofEpochMilli(i.toLong), ZoneId.of("UTC")))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + i))
    case JInt(i) => ExtractionHelper.tryExtract(LocalDateTime.ofInstant(Instant.ofEpochMilli(i.longValue), ZoneId.of("UTC")))(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: " + i))
    case JNull => throw new InvalidRequestException(KoskiErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: null"))
  }, {
    case d: LocalDateTime => JString(d.toString)
  })
)

object GenericJsonFormats {
  val genericFormats: Formats = new DefaultFormats {
    override def dateFormatter = {
      val format = super.dateFormatter
      format.setTimeZone(DefaultFormats.UTC)
      format
    }

    override val strictOptionParsing: Boolean = true
  }
}
