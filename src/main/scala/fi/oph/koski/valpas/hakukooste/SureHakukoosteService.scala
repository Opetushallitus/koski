package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.http.Http.{StringToUriConverter, parseJsonWithDeserialize}
import fi.oph.koski.http.{HttpStatus, HttpStatusException, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import org.json4s.JValue


class SureHakukoosteService(config: Config, validatingAndResolvingExtractor: ValidatingAndResolvingExtractor) extends ValpasHakukoosteService with Logging with Timing {
  private val baseUrl = "/suoritusrekisteri"

  private val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), baseUrl)

  def getHakukoosteet
    (oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean = false, errorClue: String = "")
  : Either[HttpStatus, Seq[Hakukooste]] = {
    val encoder = json4sEncoderOf[Seq[ValpasHenkilö.Oid]]

    def deserialize(parsedJson: JValue): Either[HttpStatus, Seq[Hakukooste]] =
      validatingAndResolvingExtractor.extract[Seq[Hakukooste]](parsedJson)
        .left.map(e => {
          logger.error(s"Error deserializing JSON response from Suoritusrekisteri for ${errorClue}: " + e.toString)
          ValpasErrorCategory.badGateway.sure()
        }
      )

    val decoder = parseJsonWithDeserialize(deserialize) _
    val timedBlockname = if (oppijaOids.size == 1) "getHakukoosteetSingle" else "getHakukoosteetMultiple"

    val queryParams = if (ainoastaanAktiivisetHaut) "?ainoastaanAktiivisetHaut=true" else ""

    timed(timedBlockname, 10) {
      http.post(s"$baseUrl/rest/v1/valpas/${queryParams}".toUri, oppijaOids.toSeq)(encoder)(decoder)
        .handle {
          case e: HttpStatusException =>
            logger.error(s"Bad response from Suoritusrekisteri for ${errorClue}: " + e.toString)
            Left(ValpasErrorCategory.unavailable.sure())
          case e: Exception =>
            logger.error(s"Error fetching hakukoosteet for ${errorClue}: " + e.toString)
            Left(ValpasErrorCategory.internalError())
        }
        .unsafePerformSync
    }
  }
}
