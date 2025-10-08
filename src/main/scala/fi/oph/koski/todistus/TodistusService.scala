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
  def state: String
}

// TODO: TOR-2400: Tarvittavat lisätilat monivaiheista luontia, debuggausta yms. varten

case class CertificateQueued(
  @EnumValue("QUEUED")
  state: String = "QUEUED",
) extends CertificateResponse

case class CertificateCompleted(
  @EnumValue("COMPLETED")
  state: String = "COMPLETED",
  requestedTime: ZonedDateTime,
  completionTime: ZonedDateTime,
  @RedundantData // Piilotetaan S3-url loppukäyttäjiltä
  rawUrl: String,
  @RedundantData // Piilotetaan S3-url loppukäyttäjiltä
  signedUrl: String,
) extends CertificateResponse

trait CertificateError extends CertificateResponse {
  @SyntheticProperty
  @EnumValue("ERROR")
  def state: String = "ERROR"
  @Discriminator
  def error: String
}
