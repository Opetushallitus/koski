package fi.oph.tor.json

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import fi.oph.tor.eperusteet.RakenneOsaSerializer
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s.JsonAST.{JInt, JNull, JString}
import org.json4s.jackson.Serialization
import org.json4s.{CustomSerializer, Extraction, JValue}

object Json {
  implicit val jsonFormats = GenericJsonFormats.genericFormats + new LocalDateSerializer + new RakenneOsaSerializer

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
