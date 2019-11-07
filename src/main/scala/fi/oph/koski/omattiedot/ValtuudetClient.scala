package fi.oph.koski.omattiedot

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{LogUtils, LoggerWithContext, Logging}
import org.http4s.Request

import scala.reflect.runtime.universe.TypeTag

object ValtuudetClient {
  def apply(config: Config): ValtuudetClient = if (config.getString("suomifi.valtuudet.host") == "mock") {
    MockValtuudetClient
  } else {
    new RemoteValtuudetClient(config)
  }
}

trait ValtuudetClient {
  def createSession(hetu: String): Either[ValtuudetFailure, SessionResponse]
  def getRedirectUrl(userId: String, callbackUrl: String, language: String): String
  def getAccessToken(code: String, callbackUrl: String): Either[ValtuudetFailure, String]
  def getSelectedPerson(sessionId: String, accessToken: String): Either[ValtuudetFailure, SelectedPersonResponse]
}

case class SessionResponse(sessionId: String, userId: String)
case class AccessTokenResponse(access_token: String)
case class SelectedPersonResponse(personId: String, name: String)

trait ValtuudetFailure {
  def toHttpStatus(logger: LoggerWithContext): List[HttpStatus] = {
    log(logger)
    toStatus
  }

  def log(logger: LoggerWithContext)

  protected def toStatus: List[HttpStatus]
}

case class InvalidGrant(code: String) extends ValtuudetFailure {
  override def log(logger: LoggerWithContext): Unit = logger.warn(s"Invalid grant for code $code")
  override protected def toStatus: List[HttpStatus] = Nil
}

case object SessionExpired extends ValtuudetFailure {
  override def log(logger: LoggerWithContext): Unit = logger.info("Session not found")
  override protected def toStatus: List[HttpStatus] = Nil
}

case class SuomifiValtuudetFailure(statusCode: Int, responseBody: String, request: Request) extends ValtuudetFailure with Logging {
  override def log(logger: LoggerWithContext): Unit = {
    val logDetails = LogUtils.maskSensitiveInformation(s"${request.method} ${request.uri} status $statusCode")
    logger.error(s"Error performing valtuudet request: $logDetails")
    logger.debug(LogUtils.maskSensitiveInformation(responseBody))
  }

  override protected def toStatus: List[HttpStatus] =
    List(KoskiErrorCategory.unavailable.suomifivaltuudet())
}
