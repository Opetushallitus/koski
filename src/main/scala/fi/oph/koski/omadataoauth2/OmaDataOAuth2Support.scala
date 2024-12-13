package fi.oph.koski.omadataoauth2

import fi.oph.koski.http.{ErrorDetail, HttpStatus}
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoViite}
import fi.oph.koski.koskiuser.KäyttöoikeusOrg
import org.scalatra.ScalatraServlet

import java.net.URLEncoder
import java.util.{Base64, UUID}

trait OmaDataOAuth2Support extends ScalatraServlet with OmaDataOAuth2Config {
  def queryStringUrlEncode(str: String): String = URLEncoder.encode(str, "UTF-8").replace("+", "%20")

  def base64UrlEncode(str: String): String = Base64.getUrlEncoder().encodeToString(str.getBytes("UTF-8"))
  def base64UrlDecode(str: String): String = new String(Base64.getUrlDecoder().decode(str), "UTF-8")

  // validoi parametrit, joissa olevista virheistä raportoidaan resource ownerille eikä clientille
  protected def validateQueryClientParams(): Either[OmaDataOAuth2Error, ClientInfo] = {
    for {
      clientId <- validateParamExistsOnce("client_id", OmaDataOAuth2ErrorType.invalid_client_data)
      redirectUri <- validateParamExistsOnce("redirect_uri", OmaDataOAuth2ErrorType.invalid_client_data)
      state <- validateParamExistsAtMostOnce("state", OmaDataOAuth2ErrorType.invalid_client_data)
      _ <- validateClientIdRekisteröity(clientId, OmaDataOAuth2ErrorType.invalid_client_data)
      _ <- validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId, redirectUri)
    } yield ClientInfo(clientId, redirectUri, state)
  }

  // validoi parametrit, joissa olevista virheistä raportoidaan clientille redirectin kautta
  protected def validateQueryOtherParams(clientInfo: ClientInfo): Either[OmaDataOAuth2Error, ParamInfo] = {
    // TODO: TOR-2210: tee muiden parametrien validoinnit, joiden virheistä voi/kuuluu raportoida redirect_uri:n kautta
    // clientille asti. Esim. onko S256 code challenge annettu jne.
    for {
      responseType <- validateResponseType()
      responseMode <- validateResponseMode()
      codeChallengeMethod <- validateCodeChallengeMethod()
      codeChallenge <- validateParamExistsOnce("code_challenge", OmaDataOAuth2ErrorType.invalid_request)
      scope <- validateParamExistsOnce("scope", OmaDataOAuth2ErrorType.invalid_request)
      scope <- validateScope(clientInfo.clientId, scope)
    } yield ParamInfo(responseType, responseMode, codeChallengeMethod, codeChallenge, scope)
  }

  private def validateResponseType() = {
    for {
      _ <- validateParamExistsOnce("response_type", OmaDataOAuth2ErrorType.invalid_request)
      responseType <- validateParamIs("response_type", Seq("code"), OmaDataOAuth2ErrorType.invalid_request)
    } yield(responseType)
  }

  private def validateResponseMode() = {
    for {
      _ <- validateParamExistsOnce("response_mode", OmaDataOAuth2ErrorType.invalid_request)
      responseMode <- validateParamIs("response_mode", Seq("form_post"), OmaDataOAuth2ErrorType.invalid_request)
    } yield(responseMode)
  }

  private def validateCodeChallengeMethod() = {
    for {
      _ <- validateParamExistsOnce("code_challenge_method", OmaDataOAuth2ErrorType.invalid_request)
      codeChallengeMethod <- validateParamIs("code_challenge_method", Seq("S256"), OmaDataOAuth2ErrorType.invalid_request)
    } yield(codeChallengeMethod)
  }

  private def validateParamExistsOnce(paramName: String, errorType: OmaDataOAuth2ErrorType): Either[OmaDataOAuth2Error, String] = {
    multiParams(paramName).length match {
      case 0 => Left(OmaDataOAuth2Error(errorType, s"required parameter ${paramName} missing"))
      case 1 => Right(params(paramName))
      case _ => Left(OmaDataOAuth2Error(errorType, s"parameter ${paramName} is repeated"))
    }
  }

  private def validateParamExistsAtMostOnce(paramName: String, errorType: OmaDataOAuth2ErrorType): Either[OmaDataOAuth2Error, Option[String]] = {
    multiParams(paramName).length match {
      case 0 => Right(None)
      case 1 => Right(Some(params(paramName)))
      case _ => Left(OmaDataOAuth2Error(errorType, s"parameter ${paramName} is repeated"))
    }
  }

  private def validateParamIs(paramName: String, allowedValues: Seq[String], errorType: OmaDataOAuth2ErrorType): Either[OmaDataOAuth2Error, String] = {
    val paramValue = params(paramName)
    allowedValues.contains(paramValue) match {
      case true => Right(paramValue)
      case _ => Left(OmaDataOAuth2Error(errorType, s"${paramName}=${paramValue} not supported. Supported values: ${allowedValues.mkString(",")}"))
    }
  }

  protected def validateClientIdRekisteröity(clientId: String, errorType: OmaDataOAuth2ErrorType): Either[OmaDataOAuth2Error, String] = {
    // TODO: TOR-2210: esim. koodisto voisi olla parempi source kuin konffitiedosto clientien tiedoille
    if (hasConfigForClient(clientId)) {
      Right(clientId)
    } else {
      Left(OmaDataOAuth2Error(errorType, s"unregistered client ${clientId}"))
    }
  }

  // TODO: TOR-2210 Tätä pitää kutsua myös resource endpointissa, koska oikeudet tai koodisto voi muuttua tokenin voimassaoloaikana
  protected def validateScope(client_id: String, scope: String): Either[OmaDataOAuth2Error, String] = {
    for {
      scope <- validateScopeContainsOnlyKoodistoValues(scope)
      scope <- validateScopeAtLeastOneHenkilotiedotScope(scope)
      scope <- validateScopeExactlyOneOpiskeluoikeudetScope(scope)
    } yield scope
  }

  private def validateScopeContainsOnlyKoodistoValues(scope: String): Either[OmaDataOAuth2Error, String] = {
    val requestedScopes = scope.toUpperCase.split(" ")

    val invalidScopes = requestedScopes
      .filterNot(validScopes.contains)
      .sorted

    if (invalidScopes.isEmpty) {
      Right(scope)
    } else {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${scope} contains unknown scopes (${invalidScopes.mkString(", ")}))"))
    }
  }

  private def validateScopeExactlyOneOpiskeluoikeudetScope(scope: String): Either[OmaDataOAuth2Error, String] = {
    val requestedScopes = scope.toUpperCase.split(" ").toSet

    val opiskeluoikeudetScopes = requestedScopes.filter(_.startsWith("OPISKELUOIKEUDET_")).toSeq.sorted

    if (opiskeluoikeudetScopes.length == 0) {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${scope} is missing a required OPISKELUOIKEUDET_ scope"))
    } else if (opiskeluoikeudetScopes.length > 1) {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${scope} contains an invalid combination of scopes (${opiskeluoikeudetScopes.mkString(", ")}))"))
    } else {
      Right(scope)
    }
  }

  private def validateScopeAtLeastOneHenkilotiedotScope(scope: String): Either[OmaDataOAuth2Error, String] = {
    val requestedScopes = scope.toUpperCase.split(" ").toSet

    if (requestedScopes.exists(_.startsWith("HENKILOTIEDOT_"))) {
      Right(scope)
    } else {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_scope, s"scope=${scope} is missing a required HENKILOTIEDOT_ scope"))
    }
  }

  protected def createParamsString(params: Seq[(String, String)]): String = params.map {
    case (name, value) => s"${name}=${queryStringUrlEncode(value)}"
  }.mkString("&")

  private def validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId: String, redirectUri: String): Either[OmaDataOAuth2Error, Unit] = {
    if (hasRedirectUri(clientId, redirectUri)) {
      Right(Unit)
    } else {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_client_data, s"unregistered redirection URI ${redirectUri} for ${clientId}"))
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

  protected def logoutAndRedirectWithErrorsToResourceOwnerFrontend(parameters: String) = {
    val parametersEncoded = base64UrlEncode(parameters)
    val logoutRedirect = s"/koski/omadata-oauth2/cas-workaround/authorize/${parametersEncoded}"
    val logoutUri = s"/koski/user/logout?target=${logoutRedirect}"

    redirect(logoutUri)
  }

  protected def redirectWithErrorsToResourceOwnerFrontend(parameters: String) = {
    val parametersEncoded = base64UrlEncode(parameters)
    val frontendRedirect = s"/koski/omadata-oauth2/cas-workaround/authorize/${parametersEncoded}"

    redirect(frontendRedirect)
  }

  protected def validScopes: Set[String] = {
    findKoodisto("omadataoauth2scope").map(_.koodiArvo.toUpperCase()).toSet
  }

  private def findKoodisto(koodistoUri: String, versioNumero: Option[String] = None): Seq[KoodistoKoodi] = {
    val versio: Option[KoodistoViite] = versioNumero match {
      case Some("latest") =>
        application.koodistoPalvelu.getLatestVersionOptional(koodistoUri)
      case Some(versio) =>
        Some(KoodistoViite(koodistoUri, versio.toInt))
      case _ =>
        application.koodistoPalvelu.getLatestVersionOptional(koodistoUri)
    }
    versio.toSeq.flatMap { koodisto => (application.koodistoPalvelu.getKoodistoKoodit(koodisto)) }
  }
}

object OmaDataOAuth2Error {
  def apply(clientError: OmaDataOAuth2ErrorType, errorDescription: String): OmaDataOAuth2Error =
    OmaDataOAuth2Error(s"omadataoauth2-error-${UUID.randomUUID()}", clientError, errorDescription)
}

case class OmaDataOAuth2Error(
  errorId: String,
  errorType: OmaDataOAuth2ErrorType,
  errorDescription: String
) {
  def getClientErrorParams = s"error=${errorType.toString}&error_id=${errorId}"
  def getLoggedErrorMessage = s"${errorId}: ${errorDescription}"

  def getPostResponseServletParams: Seq[(String, String)] =
    Seq(
      ("error", errorType.toString),
      ("error_description", s"${errorId}: ${errorDescription}")
    )

  def toKoskiHttpStatus: HttpStatus = {
    HttpStatus(400, List(ErrorDetail(key = errorType.errorType, errorDescription)))
  }

  def getAccessTokenErrorResponse: AccessTokenErrorResponse = {
    AccessTokenErrorResponse(
      errorType.toString,
      Some(s"${errorId}: ${errorDescription}"),
      None
    )
  }
}

sealed abstract class OmaDataOAuth2ErrorType(val errorType: String)

object OmaDataOAuth2ErrorType {
  final case object invalid_client_data extends OmaDataOAuth2ErrorType("invalid_client_data")
  final case object invalid_request extends OmaDataOAuth2ErrorType("invalid_request")
  final case object invalid_scope extends OmaDataOAuth2ErrorType("invalid_scope")
  final case object server_error extends OmaDataOAuth2ErrorType("server_error")
  final case object invalid_client extends OmaDataOAuth2ErrorType("invalid_client")
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
