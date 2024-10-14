package fi.oph.koski.omadataoauth2

import org.scalatra.ScalatraServlet

import java.util.UUID

trait OmaDataOAuth2Support extends ScalatraServlet with OmaDataOAuth2Config {
  protected def validateClientIdRekisteröity(clientId: String): Either[ValidationError, String] = {
    // TODO: TOR-2210: esim. koodisto voisi olla parempi source kuin konffitiedosto clientien tiedoille
    if (hasConfigForClient(clientId)) {
      Right(clientId)
    } else {
      Left(ValidationError(ValidationErrorType.invalid_client_data, s"unregistered client ${clientId}"))
    }
  }
}

object ValidationError {
  def apply(clientError: ValidationErrorType, errorDescription: String): ValidationError =
    ValidationError(s"omadataoauth2-error-${UUID.randomUUID()}", clientError, errorDescription)
}

case class ValidationError(
  errorId: String,
  errorType: ValidationErrorType,
  errorDescription: String
) {
  def getClientErrorParams = s"error=${errorType.toString}&error_id=${errorId}"
  def getLoggedErrorMessage = s"${errorId}: ${errorDescription}"

  def getPostResponseServletParams: Seq[(String, String)] =
    Seq(
      ("error", errorType.toString),
      ("error_description", s"${errorId}: ${errorDescription}")
    )
}

sealed abstract class ValidationErrorType(val errorType: String)

object ValidationErrorType {
  final case object invalid_client_data extends ValidationErrorType("invalid_client_data")
  final case object invalid_request extends ValidationErrorType("invalid_request")
}
