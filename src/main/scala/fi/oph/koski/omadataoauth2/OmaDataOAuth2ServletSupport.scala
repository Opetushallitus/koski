package fi.oph.koski.omadataoauth2

import org.scalatra.ScalatraServlet

import java.net.URLEncoder
import java.util.Base64

trait OmaDataOAuth2ServletSupport extends ScalatraServlet with OmaDataOAuth2Support {
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
    for {
      responseType <- validateResponseType()
      responseMode <- validateResponseMode()
      codeChallengeMethod <- validateCodeChallengeMethod()
      codeChallenge <- validateParamExistsOnce("code_challenge", OmaDataOAuth2ErrorType.invalid_request)
      scope <- validateParamExistsOnce("scope", OmaDataOAuth2ErrorType.invalid_request)
      scope <- validateScope(scope)
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
    if (hasConfigForClient(clientId)) {
      Right(clientId)
    } else {
      Left(OmaDataOAuth2Error(errorType, s"unregistered client ${clientId}"))
    }
  }

  protected def createParamsString(params: Seq[(String, String)]): String = params.map {
    case (name, value) => s"${name}=${queryStringUrlEncode(value)}"
  }.mkString("&")

  private def validateRedirectUriRekisteröityAnnetulleClientIdlle(clientId: String, redirectUri: String): Either[OmaDataOAuth2Error, Unit] = {
    if (hasRedirectUri(clientId, redirectUri)) {
      Right(())
    } else {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_client_data, s"unregistered redirection URI ${redirectUri} for ${clientId}"))
    }
  }

  protected def redirectToPostResponse(doLogout: Boolean, postResponseParams: String) = {
    val uri =
      if (doLogout) {
        createPostResponseUriViaLogout(postResponseParams)
      } else {
        createPostResponseUri(postResponseParams)
      }
    redirect(uri)
  }

  private def createPostResponseUriViaLogout(postResponseParams: String) = {
    val postResponseParamsBase64UrlEncoded = base64UrlEncode(postResponseParams)
    val postResponseRedirect = s"/koski/omadata-oauth2/cas-workaround/post-response/${postResponseParamsBase64UrlEncoded}"
    s"/koski/user/logout?target=${postResponseRedirect}"
  }

  private def createPostResponseUri(postResponseParams: String) = {
    s"/koski/omadata-oauth2/post-response/?${postResponseParams}"
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
}
