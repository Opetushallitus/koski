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
  def get(oids: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse]
}

class RemoteMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  def get(oids: List[String], basicAuthRequest: BasicAuthRequest) = {
    val config = ServiceConfig(application.config.getString("opintopolku.virkailija.url"), basicAuthRequest.username, basicAuthRequest.password, true)
    val client = VirkailijaHttpClient(config,
      "/valinta-tulos-service",
      sessionCookieName = "session",
      serviceUrlSuffix = "auth/login",
      false)

    runIO(client.post(uri"/valinta-tulos-service/cas/migri/hakemukset/", oids)(json4sEncoderOf[List[String]]) {
      case (200, text, _) => Right(RawJsonResponse(text))
      case (status, text, _) =>
        logger.error(s"valinta-tulos-service returned status $status: $text with user ${basicAuthRequest.username} and first oid ${oids.headOption} from ${application.config.getString("opintopolku.virkailija.url")}")
        Left(KoskiErrorCategory.internalError("Virhe kutsuttaessa valinta-tulos-servicea"))
    })
  }
}

class MockMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  def get(oids: List[String], basicAuthRequest: BasicAuthRequest): Either[HttpStatus, RawJsonResponse] = {
    // Hyvin tynkä toteutus, lähinnä tarkistetaan, että BasicAuthRequest tulee testistä perille oikeanlaisena
    val data = MockResponse(oids, basicAuthRequest.username, basicAuthRequest.password)
    val json = JsonSerializer.writeWithRoot(data)
    Right(RawJsonResponse(json))
  }
}

case class MockResponse(
  oids: List[String],
  username: String,
  password: String,
)
