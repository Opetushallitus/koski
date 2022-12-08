package fi.oph.koski.migri

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.RawJsonResponse
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

trait MigriService {
  def getByOids(oids: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse]
  def getByHetus(hetus: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse]
}

class RemoteMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  private def getClient(username: String, password: String) = {
    val config = ServiceConfig(application.config.getString("opintopolku.virkailija.url"), username, password, useCas = true)
    VirkailijaHttpClient(config,
      "/valinta-tulos-service",
      sessionCookieName = "session",
      serviceUrlSuffix = "auth/login",
      preferGettingCredentialsFromSecretsManager = false)
  }

  def getByOids(oids: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse] = {
    val client = getClient(basicAuthRequest.username, basicAuthRequest.password)

    runIO(client.post(uri"/valinta-tulos-service/cas/migri/hakemukset/henkilo-oidit", oids)(json4sEncoderOf[List[String]]) {
      case (200, text, _) => Right(RawJsonResponse(text))
      case (status, text, _) =>
        logger.error(s"valinta-tulos-service returned status $status: $text with user ${basicAuthRequest.username} and first oid ${oids.headOption} from ${application.config.getString("opintopolku.virkailija.url")}")
        Left(KoskiErrorCategory.internalError("Virhe kutsuttaessa valinta-tulos-servicea"))
    })
  }

  def getByHetus(hetus: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse] = {
    val client = getClient(basicAuthRequest.username, basicAuthRequest.password)

    runIO(client.post(uri"/valinta-tulos-service/cas/migri/hakemukset/hetut", hetus)(json4sEncoderOf[List[String]]) {
      case (200, text, _) => Right(RawJsonResponse(text))
      case (status, text, _) =>
        logger.error(s"valinta-tulos-service returned status $status: $text with user ${basicAuthRequest.username} from ${application.config.getString("opintopolku.virkailija.url")}")
        Left(KoskiErrorCategory.internalError("Virhe kutsuttaessa valinta-tulos-servicea"))
    })
  }
}

class MockMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  def getByOids(oids: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse] = {
    // Hyvin tynkä toteutus, lähinnä tarkistetaan, että BasicAuthRequest tulee testistä perille oikeanlaisena
    val data = MockResponseOids(oids, basicAuthRequest.username, basicAuthRequest.password)
    val json = JsonSerializer.writeWithRoot(data)
    Right(RawJsonResponse(json))
  }

  def getByHetus(hetus: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse] = {
    val data = MockResponseHetus(hetus, basicAuthRequest.username, basicAuthRequest.password)
    val json = JsonSerializer.writeWithRoot(data)
    Right(RawJsonResponse(json))
  }
}

case class MockResponseOids(
  oids: List[String],
  username: String,
  password: String,
)


case class MockResponseHetus(
  hetus: List[String],
  username: String,
  password: String,
)
