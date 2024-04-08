package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema.Organisaatio
import fi.oph.scalaschema.annotation.{Description, Discriminator, EnumValue, Title}
import org.json4s.JValue
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.InputStream
import java.time.LocalDate
import scala.concurrent.Future

trait MassaluovutusQueryParameters {
  @Description("Massaluovutuksen tyyppi.")
  @Discriminator
  def `type`: String
  @Description("Tulosten toimitusformaatti.")
  @Discriminator
  def format: String

  def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit]

  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean
  def asJson: JValue = JsonSerializer.serializeWithRoot(this)
  def fillAndValidate(implicit user: KoskiSpecificSession): Either[HttpStatus, MassaluovutusQueryParameters] = Right(this)
}
