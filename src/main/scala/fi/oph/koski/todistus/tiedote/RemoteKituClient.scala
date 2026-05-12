package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.http._
import fi.oph.koski.http.Http.{parseJson, runIO}
import fi.oph.koski.log.Logging

class RemoteKituClient(config: Config) extends KituClient with Logging {
  private val baseUrl = config.getString("kitu.baseUrl")
  private val otuvaTokenEndpoint = config.getString("otuvaTokenEndpoint")

  private val http = new OtuvaOAuth2ClientFactory(OtuvaOAuth2Credentials.fromSecretsManager, otuvaTokenEndpoint)
    .apply(baseUrl, Http.retryingClient("kitu"))

  override def getExamineeDetails(lähdejärjestelmänId: String): Either[HttpStatus, KituExamineeDetails] = {
    getExamineeDetails(KituClient.examineeDetailsUri(lähdejärjestelmänId))
  }

  override def getExamineeDetailsByOpiskeluoikeusOid(opiskeluoikeusOid: String): Either[HttpStatus, KituExamineeDetails] = {
    getExamineeDetails(KituClient.examineeDetailsByOpiskeluoikeusOidUri(opiskeluoikeusOid))
  }

  private def getExamineeDetails(uri: Http.ParameterizedUriWrapper): Either[HttpStatus, KituExamineeDetails] = {
    try {
      val result = runIO(
        http.get(uri)(parseJson[KituExamineeDetails])
      )
      Right(result)
    } catch {
      case e: HttpStatusException =>
        logger.error(s"Kitu-kutsu epäonnistui: ${e.status} ${e.msg}")
        Left(KoskiErrorCategory.unavailable(s"Kitu-kutsu epäonnistui: ${e.status}"))
      case e: Exception =>
        logger.error(e)(s"Kitu-kutsu epäonnistui: ${e.getMessage}")
        Left(KoskiErrorCategory.internalError(s"Kitu-kutsu epäonnistui: ${e.getMessage}"))
    }
  }
}
