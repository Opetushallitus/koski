package fi.oph.koski.migri

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

trait MigriService {
  def get(oid: String, basicAuthRequest: BasicAuthRequest): Either[HttpStatus, String]
}

class RemoteMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  def get(oid: String, basicAuthRequest: BasicAuthRequest) = {
    val config = ServiceConfig(application.config.getString("opintopolku.virkailija.url"), basicAuthRequest.username, basicAuthRequest.password, true)
    val client = VirkailijaHttpClient(config,
      "/valinta-tulos-service",
      sessionCookieName = "session",
      serviceUrlSuffix = "auth/login")

    runIO(client.post(uri"/valinta-tulos-service/cas/migri/hakemukset/", List(oid))(json4sEncoderOf[List[String]]) {
      case (200, text, _) => Right(text)
      case (status, text, _) =>
        logger.error(s"valinta-tulos-service returned status $status: $text")
        Left(KoskiErrorCategory.internalError("Virhe kutsuttaessa valinta-tulos-servicea"))
    })
  }
}

class MockMigriService (implicit val application: KoskiApplication) extends Logging with MigriService {
  def get(oid: String, basicAuthRequest: BasicAuthRequest): Either[HttpStatus, String] = {
    // Hyvin tynkä toteutus, lähinnä tarkistetaan, että BasicAuthRequest tulee testistä perille oikeanlaisena
    Right(oid + basicAuthRequest.username + basicAuthRequest.password)
  }
}
