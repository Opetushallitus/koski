package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.{Logging}
import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.scalaschema.annotation.{Discriminator, EnumValue, SyntheticProperty}

import java.io.OutputStream
import java.time.ZonedDateTime

class TodistusService(application: KoskiApplication) extends Logging {
  private val resultRepository = new TodistusResultRepository(application.config)

  def currentStatus(req: TodistusOidRequest): Either[HttpStatus, CertificateResponse] = {
    // TODO: TOR-2400: Toteuta
    Left(KoskiErrorCategory.notImplemented())
  }

  def initiateGenerating(req: TodistusOidRequest): Either[HttpStatus, Unit] = {
    // TODO: TOR-2400: Toteuta
    Left(KoskiErrorCategory.notImplemented())
  }

  def downloadCertificate(req: CertificateCompleted, output: OutputStream): Unit = {
    // TODO: TOR-2400: Hae tiedosto, tai ehkä ennemmin presigned s3 URL?
    // resultRepository.fooBar...
  }
}

trait CertificateResponse {
  @Discriminator
  def status: String
}

// TODO: TOR-2400: Tarvittavat lisätilat monivaiheista luontia, debuggausta yms. varten

case class CertificateNotStarted(
  @EnumValue("NOT_STARTED")
  status: String = "NOT_STARTED",
) extends CertificateResponse

case class CertificateInProgress(
  @EnumValue("IN_PROGRESS")
  status: String = "IN_PROGRESS",
  requestedTime: ZonedDateTime,
) extends CertificateResponse

case class CertificateCompleted(
  @EnumValue("COMPLETED")
  status: String = "COMPLETED",
  requestedTime: ZonedDateTime,
  completionTime: ZonedDateTime,
  @RedundantData // Piilotetaan S3-url loppukäyttäjiltä
  certificateUrl: String,
) extends CertificateResponse

trait CertificateError extends CertificateResponse {
  @SyntheticProperty
  @EnumValue("ERROR")
  def status: String = "ERROR"
  @Discriminator
  def errorReason: String
}
