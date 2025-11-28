package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.Session
import fi.oph.scalaschema.annotation.{Description, Discriminator}
import org.json4s.JValue

trait KoskiMassaluovutusQueryParameters extends MassaluovutusQueryParameters

trait MassaluovutusQueryParameters {
  @Description("Massaluovutuksen tyyppi.")
  @Discriminator
  def `type`: String
  @Description("Tulosten toimitusformaatti.")
  @Discriminator
  def format: String

  def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit]

  def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean
  def asJson: JValue = JsonSerializer.serializeWithRoot(this)
  def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryParameters] = Right(this)
  def priority: Int = MassaluovutusQueryPriority.normal
}

trait PartitionSupport {
  def partitionFormats: Seq[String]
}

object MassaluovutusQueryPriority {
  val highest = 1
  val high = 3
  val normal = 10
  val low = 30
  val lowest = 90
}
