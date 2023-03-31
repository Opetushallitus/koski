package fi.oph.koski.ytr

import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.scalaschema.annotation.{Discriminator, EnumValue, SyntheticProperty}

import java.time.ZonedDateTime

trait YtrCertificateResponse {
  @Discriminator
  def status: String
}

case class YtrCertificateNotStarted(
  @EnumValue("NOT_STARTED")
  status: String = "NOT_STARTED",
) extends YtrCertificateResponse

case class YtrCertificateInProgress(
  @EnumValue("IN_PROGRESS")
  status: String = "IN_PROGRESS",
  requestedTime: ZonedDateTime,
) extends YtrCertificateResponse

case class YtrCertificateCompleted(
  @EnumValue("COMPLETED")
  status: String = "COMPLETED",
  requestedTime: ZonedDateTime,
  completionTime: ZonedDateTime,
  @RedundantData // Piilotetaan S3-url loppukäyttäjiltä
  certificateUrl: String,
) extends YtrCertificateResponse

trait YtrCertificateError extends YtrCertificateResponse {
  @SyntheticProperty
  @EnumValue("ERROR")
  def status: String = "ERROR"
  @Discriminator
  def errorReason: String
}

case class YtrCertificateBlocked(
  @EnumValue("NOT_ALLOWED_BLOCKED")
  errorReason: String = "NOT_ALLOWED_BLOCKED"
) extends YtrCertificateError

case class YtrCertificateOldExamination(
  @EnumValue("NOT_ALLOWED_OLD_EXAMINATION")
  errorReason: String = "NOT_ALLOWED_OLD_EXAMINATION"
) extends YtrCertificateError

case class YtrCertificateTimeout(
  requestedTime: ZonedDateTime,
  @EnumValue("TIMEOUT")
  errorReason: String = "TIMEOUT"
) extends YtrCertificateError

case class YtrCertificateInternalError(
  requestedTime: ZonedDateTime,
  @EnumValue("INTERNAL_ERROR")
  errorReason: String = "INTERNAL_ERROR"
) extends YtrCertificateError

case class YtrCertificateServiceUnavailable(
  @EnumValue("SERVICE_UNAVAILABLE")
  errorReason: String = "SERVICE_UNAVAILABLE"
) extends YtrCertificateError
