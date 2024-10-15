package fi.oph.koski.omadataoauth2

import org.scalatra.ScalatraServlet

import java.net.URLEncoder
import java.util.{Base64, UUID}

trait OmaDataOAuth2Support extends ScalatraServlet with OmaDataOAuth2Config {
  def urlEncode(str: String): String = URLEncoder.encode(str, "UTF-8")

  def base64UrlEncode(str: String): String = Base64.getUrlEncoder().encodeToString(str.getBytes("UTF-8"))
  def base64UrlDecode(str: String): String = new String(Base64.getUrlDecoder().decode(str), "UTF-8")

  // validoi parametrit, joissa olevista virheistä raportoidaan resource ownerille eikä clientille
  protected def validateQueryClientParams(): Either[ValidationError, ClientInfo] = {
    for {
      clientId <- validateParamExistsOnce("client_id", ValidationErrorType.invalid_client_data)
      redirectUri <- validateParamExistsOnce("redirect_uri", ValidationErrorType.invalid_client_data)
      state <- validateParamExistsAtMostOnce("state", ValidationErrorType.invalid_client_data)
      _ <- validateClientIdRekisteröity(clientId)
      _ <- validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId, redirectUri)
    } yield ClientInfo(clientId, redirectUri, state)
  }

  // validoi parametrit, joissa olevista virheistä raportoidaan clientille redirectin kautta
  protected def validateQueryOtherParams(): Either[ValidationError, Unit] = {
    // TODO: TOR-2210: tee muiden parametrien validoinnit, joiden virheistä voi/kuuluu raportoida redirect_uri:n kautta
    // clientille asti. Esim. onko S256 code challenge annettu jne.
    for {
      responseType <- validateResponseType()
      responseMode <- validateResponseMode()
      codeChallengeMethod <- validateCodeChallengeMethod()
      codeChallenge <- validateParamExistsOnce("code_challenge", ValidationErrorType.invalid_request)
      scope <- validateParamExistsOnce("scope", ValidationErrorType.invalid_request)
    } yield ParamInfo(responseType, responseMode, codeChallengeMethod, codeChallenge, scope)
  }

  private def validateResponseType() = {
    for {
      _ <- validateParamExistsOnce("response_type", ValidationErrorType.invalid_request)
      responseType <- validateParamIs("response_type", Seq("code"), ValidationErrorType.invalid_request)
    } yield(responseType)
  }

  private def validateResponseMode() = {
    for {
      _ <- validateParamExistsOnce("response_mode", ValidationErrorType.invalid_request)
      responseMode <- validateParamIs("response_mode", Seq("form_post"), ValidationErrorType.invalid_request)
    } yield(responseMode)
  }

  private def validateCodeChallengeMethod() = {
    for {
      _ <- validateParamExistsOnce("code_challenge_method", ValidationErrorType.invalid_request)
      codeChallengeMethod <- validateParamIs("code_challenge_method", Seq("S256"), ValidationErrorType.invalid_request)
    } yield(codeChallengeMethod)
  }

  private def validateParamExistsOnce(paramName: String, errorType: ValidationErrorType): Either[ValidationError, String] = {
    multiParams(paramName).length match {
      case 0 => Left(ValidationError(errorType, s"required parameter ${paramName} missing"))
      case 1 => Right(params(paramName))
      case _ => Left(ValidationError(errorType, s"parameter ${paramName} is repeated"))
    }
  }

  private def validateParamExistsAtMostOnce(paramName: String, errorType: ValidationErrorType): Either[ValidationError, Option[String]] = {
    multiParams(paramName).length match {
      case 0 => Right(None)
      case 1 => Right(Some(params(paramName)))
      case _ => Left(ValidationError(errorType, s"parameter ${paramName} is repeated"))
    }
  }

  private def validateParamIs(paramName: String, allowedValues: Seq[String], errorType: ValidationErrorType): Either[ValidationError, String] = {
    val paramValue = params(paramName)
    allowedValues.contains(paramValue) match {
      case true => Right(paramValue)
      case _ => Left(ValidationError(errorType, s"${paramName}=${paramValue} not supported. Supported values: ${allowedValues.mkString(",")}"))
    }
  }

  protected def validateClientIdRekisteröity(clientId: String): Either[ValidationError, String] = {
    // TODO: TOR-2210: esim. koodisto voisi olla parempi source kuin konffitiedosto clientien tiedoille
    if (hasConfigForClient(clientId)) {
      Right(clientId)
    } else {
      Left(ValidationError(ValidationErrorType.invalid_client_data, s"unregistered client ${clientId}"))
    }
  }

  protected def createParamsString(params: Seq[(String, String)]): String = params.map {
    case (name, value) => s"${name}=${urlEncode(value)}"
  }.mkString("&")


  private def validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId: String, redirectUri: String): Either[ValidationError, Unit] = {
    if (hasRedirectUri(clientId, redirectUri)) {
      Right(Unit)
    } else {
      Left(ValidationError(ValidationErrorType.invalid_client_data, s"unregeistered redirection URI ${redirectUri} for ${clientId}"))
    }
  }

  protected def redirectToPostResponse(postResponseParams: String): Unit = {
    redirect(s"/koski/omadata-oauth2/post-response/?${postResponseParams}")
  }

  protected def redirectToPostResponseViaLogout(postResponseParams: String) = {
    val postResponseParamsBase64UrlEncoded = base64UrlEncode(postResponseParams)
    val logoutRedirect = s"/koski/omadata-oauth2/cas-workaround/post-response/${postResponseParamsBase64UrlEncoded}"
    val logoutUri = s"/koski/user/logout?target=${logoutRedirect}"

    redirect(logoutUri)
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

case class ClientInfo(
  clientId: String,
  redirectUri: String,
  state: Option[String]
) {
  def getPostResponseServletParams = Seq(("client_id", clientId), ("redirect_uri", redirectUri)) ++ state.toSeq.map(v => ("state", v))
}

case class ParamInfo(
  responseType: String,
  responseMode: String,
  codeChallengeMethod: String,
  codeChallenge: String,
  scope: String
)
