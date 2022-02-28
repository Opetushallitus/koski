package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.http.Http.{StringToUriConverter, parseJsonWithDeserialize, unsafeRetryingClient}
import fi.oph.koski.http.{Http, HttpException, HttpStatus, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.lenientDeserialization
import fi.oph.koski.util.Timing
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import org.json4s.JValue

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class SureHakukoosteService(
  config: Config,
  validatingAndResolvingExtractor: ValidatingAndResolvingExtractor
) extends ValpasHakukoosteService
  with Logging
  with Timing {

  private val baseUrl = "/suoritusrekisteri"

  private val totalTimeout = config.getInt("valpas.hakukoosteTimeoutSeconds").seconds

  private val http = {
    // Suoritusterkisteriin POST-metodilla tehtävät kyselyt ovat oikeasti idempotentteja,
    // joten niiden uudelleenyrittäminen on ok: siksi unsafeRetryingClient.

    // val (connectTimeout, headerTimeout) = (totalTimeout / 3, totalTimeout / 3 + 1.second)
    // Kokeilu saada Valpas tuottamaan vähän vähemmän virheitä.
    val (connectTimeout, headerTimeout) = totalTimeout match {
      case t if t >= 4.seconds => (totalTimeout - 3.seconds, totalTimeout - 2.seconds)
      case _ => (1.second, 2.seconds)
    }

    val client = unsafeRetryingClient(
      baseUrl, clientBuilder => clientBuilder
        .withConnectTimeout(connectTimeout)
        .withResponseHeaderTimeout(headerTimeout)
        .withRequestTimeout(totalTimeout)
    )

    VirkailijaHttpClient(
      ServiceConfig.apply(config, "opintopolku.virkailija"),
      baseUrl,
      client
    )
  }

  def getHakukoosteet
    (oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean, errorClue: String)
  : Either[HttpStatus, Seq[Hakukooste]] = {
    val encoder = json4sEncoderOf[Seq[ValpasHenkilö.Oid]]

    def deserialize(parsedJson: JValue): Either[HttpStatus, Seq[Hakukooste]] =
      validatingAndResolvingExtractor.extract[Seq[Hakukooste]](lenientDeserialization)(parsedJson)
        .left.map(e => {
          logger.error(s"Error deserializing JSON response from Suoritusrekisteri for ${errorClue}: ${e.toString}")
          ValpasErrorCategory.badGateway.sure()
        }
      )

    val decoder = parseJsonWithDeserialize(deserialize) _
    val timedBlockname = if (oppijaOids.size == 1) "getHakukoosteetSingle" else "getHakukoosteetMultiple"

    val queryParams = if (ainoastaanAktiivisetHaut) "?ainoastaanAktiivisetHaut=true" else ""

    timed(timedBlockname, 10) {
      Http.runIO(
        http.post(
          s"$baseUrl/rest/v1/valpas/${queryParams}".toUri,
          oppijaOids.toSeq,
          timeout = totalTimeout
        )(encoder)(decoder)
          .handleError {
            case e: HttpException =>
              logger.error(s"Bad response from Suoritusrekisteri for ${errorClue}: ${e.toString}")
              Left(ValpasErrorCategory.unavailable.sure())
            case e: Exception =>
              logger.error(s"Error fetching hakukoosteet for ${errorClue}: ${e.toString}")
              Left(ValpasErrorCategory.internalError())
          }
      )
    }
  }
}
