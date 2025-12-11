package fi.oph.koski.migri

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.http.{ErrorDetail, HttpStatus, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.RawJsonResponse

trait MigriService {
  def getByOids(oids: List[String], credentials: MigriCredentials): Either[HttpStatus, RawJsonResponse]
  def getByHetus(hetus: List[String], credentials: MigriCredentials): Either[HttpStatus, RawJsonResponse]
}

class RemoteMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  private def getClient(credentials: MigriCredentials) = {
    val config = ServiceConfig(application.config.getString("opintopolku.virkailija.url"), credentials.username, credentials.password, useCas = true)
    VirkailijaHttpClient(config,
      "/valinta-tulos-service",
      sessionCookieName = "session",
      serviceUrlSuffix = "auth/login",
      preferGettingCredentialsFromSecretsManager = false)
  }

  def getByOids(oids: List[String], credentials: MigriCredentials): Either[HttpStatus, RawJsonResponse] = {
    val client = getClient(credentials)

    runIO(client.post(uri"/valinta-tulos-service/cas/migri/hakemukset/henkilo-oidit", oids)(json4sEncoderOf[List[String]]) {
      case (200, text, _) => Right(RawJsonResponse(text))
      case (status, text, _) =>
        logger.error(s"valinta-tulos-service returned status $status: $text with user ${credentials.username} and first oid ${oids.headOption} from ${application.config.getString("opintopolku.virkailija.url")}")
        Left(HttpStatus(status, List(ErrorDetail("valinta-tulos-service-error", text))))
    })
  }

  def getByHetus(hetus: List[String], credentials: MigriCredentials): Either[HttpStatus, RawJsonResponse] = {
    val client = getClient(credentials)

    runIO(client.post(uri"/valinta-tulos-service/cas/migri/hakemukset/hetut", hetus)(json4sEncoderOf[List[String]]) {
      case (200, text, _) => Right(RawJsonResponse(text))
      case (status, text, _) =>
        logger.error(s"valinta-tulos-service returned status $status: $text with user ${credentials.username} from ${application.config.getString("opintopolku.virkailija.url")}")
        Left(HttpStatus(status, List(ErrorDetail("valinta-tulos-service-error", text))))
    })
  }
}

class MockMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  def getByOids(oids: List[String], credentials: MigriCredentials): Either[HttpStatus, RawJsonResponse] = {
    // Hyvin tynkä toteutus, lähinnä tarkistetaan, että tunnukset tulevat testistä perille oikeanlaisena
    val data = MockResponseOids(oids, credentials.username, credentials.password)
    val json = JsonSerializer.writeWithRoot(data)
    Right(RawJsonResponse(json))
  }

  def getByHetus(hetus: List[String], credentials: MigriCredentials): Either[HttpStatus, RawJsonResponse] = {
    val data = MockResponseHetus(hetus, credentials.username, credentials.password)
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

case class MigriCredentials(username: String, password: String)
