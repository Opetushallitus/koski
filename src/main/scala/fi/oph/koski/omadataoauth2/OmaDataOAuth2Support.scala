package fi.oph.koski.omadataoauth2

import fi.oph.koski.http.{ErrorDetail, HttpStatus}
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoViite}

import java.util.UUID

trait OmaDataOAuth2Support extends OmaDataOAuth2Config {
  protected def validateScope(scope: String): Either[OmaDataOAuth2Error, String] = {
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
