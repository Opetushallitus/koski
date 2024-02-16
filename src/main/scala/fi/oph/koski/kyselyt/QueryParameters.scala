package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema.Organisaatio
import fi.oph.scalaschema.annotation.{Discriminator, EnumValue}
import org.json4s.JValue

import java.time.LocalDate

trait QueryParameters {
  @Discriminator
  def `type`: String

  @Discriminator
  def format: String

  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean
  def asJson: JValue = JsonSerializer.serializeWithRoot(this)
  def withDefaults(implicit user: KoskiSpecificSession): Either[HttpStatus, QueryParameters] = Right(this)
}

