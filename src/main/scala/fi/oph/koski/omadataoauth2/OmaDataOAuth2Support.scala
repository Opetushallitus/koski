package fi.oph.koski.omadataoauth2

import fi.oph.koski.omadataoauth2.ReportingType.ReportingType
import org.scalatra.ScalatraServlet

import java.net.URLEncoder
import java.util.{Base64, UUID}

trait OmaDataOAuth2Support extends ScalatraServlet with OmaDataOAuth2Config {
  def urlEncode(str: String): String = URLEncoder.encode(str, "UTF-8")

  def base64UrlEncode(str: String): String = Base64.getUrlEncoder().encodeToString(str.getBytes("UTF-8"))
  def base64UrlDecode(str: String): String = new String(Base64.getUrlDecoder().decode(str), "UTF-8")


  protected def validateQueryParams(): Either[ValidationError, ClientInfo] = {
    for {
      clientInfo <- validateQueryClientParams
      _ <- validateQueryOtherParams
    } yield clientInfo
  }

  protected def validateQueryClientParams(): Either[ValidationError, ClientInfo] = {
    for {
      clientId <- validateParamExistsOnce("client_id", ReportingType.ToResourceOwner)
      redirectUri <- validateParamExistsOnce("redirect_uri", ReportingType.ToResourceOwner)
      _ <- validateClientIdRekisteröity(clientId)
      _ <- validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId, redirectUri)
    } yield (ClientInfo(clientId, redirectUri))
  }

  protected def validateQueryOtherParams(): Either[ValidationError, Unit] = {
    // TODO: TOR-2210: tee muiden parametrien validoinnit, joiden virheistä voi/kuuluu raportoida redirect_uri:n kautta
    // clientille asti. Esim. onko S256 code challenge annettu jne.
    Right(Unit)
  }

  private def validateParamExistsOnce(paramName: String, reportingType: ReportingType): Either[ValidationError, String] = {
    multiParams(paramName).length match {
      case 0 => Left(ValidationError(ValidationErrorType.invalid_client_data, s"${paramName} puuttuu query-parametreista", reportingType))
      case 1 => Right(params(paramName))
      case _ => Left(ValidationError(ValidationErrorType.invalid_client_data, s"${paramName} määritelty useammin kuin kerran", reportingType))
    }
  }

  protected def validateClientIdRekisteröity(clientId: String): Either[ValidationError, String] = {
    // TODO: TOR-2210: esim. koodisto voisi olla parempi source kuin konffitiedosto clientien tiedoille
    if (hasConfigForClient(clientId)) {
      Right(clientId)
    } else {
      Left(ValidationError(ValidationErrorType.invalid_client_data, s"${clientId} tuntematon client_id, ei rekisteröity", ReportingType.ToResourceOwner))
    }
  }

  protected def createParamsString(params: Seq[(String, String)]): String = params.map {
    case (name, value) => s"${name}=${urlEncode(value)}"
  }.mkString("&")


  private def validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId: String, redirectUri: String): Either[ValidationError, Unit] = {
    if (hasRedirectUri(clientId, redirectUri)) {
      Right(Unit)
    } else {
      Left(ValidationError(ValidationErrorType.invalid_client_data, s"${redirectUri} ei rekisteröity clientille ${clientId}", ReportingType.ToResourceOwner))
    }
  }
}

object ValidationError {
  def apply(clientError: ValidationErrorType, loggedMessage: String, reportingType: ReportingType): ValidationError =
    ValidationError(s"omadataoauth2-error-${UUID.randomUUID()}", clientError, loggedMessage, reportingType)
}

case class ValidationError(
  errorId: String,
  errorType: ValidationErrorType,
  loggedMessage: String,
  reportingType: ReportingType
) {
  def getClientErrorParams = s"error=${errorType.toString}&error_id=${errorId}"
  def getLoggedErrorMessage = s"${errorId}: ${loggedMessage}"
}

sealed abstract class ValidationErrorType(val errorType: String)

object ValidationErrorType {
  final case object invalid_client_data extends ValidationErrorType("invalid_client_data")
  final case object invalid_request extends ValidationErrorType("invalid_request")
}

object ReportingType extends Enumeration {
  type ReportingType = Value
  val
    ToResourceOwner,      // virheestä kuuluu raportoida vain loppukäyttäjälle
    ToRedirectUri = Value // virheestä kuuluu raportoida redirect_uri:n kautta
}

case class ClientInfo(
  clientId: String,
  redirectUri: String
)
