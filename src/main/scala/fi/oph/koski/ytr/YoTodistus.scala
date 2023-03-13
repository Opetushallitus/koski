package fi.oph.koski.ytr

import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.scalaschema.annotation.{Discriminator, EnumValue, SyntheticProperty}

import java.time.LocalDateTime

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
  requestedTime: LocalDateTime,
) extends YtrCertificateResponse

case class YtrCertificateCompleted(
  @EnumValue("COMPLETED")
  status: String = "COMPLETED",
  requestedTime: LocalDateTime,
  completedTime: LocalDateTime,
  @RedundantData // Piilotetaan S3-url loppykäyttäjiltä
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
  requestedTime: LocalDateTime,
  @EnumValue("NOT_ALLOWED_BLOCKED")
  errorReason: String = "NOT_ALLOWED_BLOCKED"
) extends YtrCertificateError

case class YtrCertificateOldExamination(
  requestedTime: LocalDateTime,
  @EnumValue("NOT_ALLOWED_OLD_EXAMINATION")
  errorReason: String = "NOT_ALLOWED_OLD_EXAMINATION"
) extends YtrCertificateError

case class YtrCertificateTimeout(
  requestedTime: LocalDateTime,
  @EnumValue("TIMEOUT")
  errorReason: String = "TIMEOUT"
) extends YtrCertificateError

case class YtrCertificateInternalError(
  requestedTime: LocalDateTime,
  @EnumValue("INTERNAL_ERROR")
  errorReason: String = "INTERNAL_ERROR"
) extends YtrCertificateError

case class YtrCertificateServiceUnavailable(
  requestedTime: LocalDateTime,
  @EnumValue("SERVICE_UNAVAILABLE")
  errorReason: String = "SERVICE_UNAVAILABLE"
) extends YtrCertificateError
