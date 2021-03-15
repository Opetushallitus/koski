package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.http.Http.{StringToUriConverter, parseJson}
import fi.oph.koski.http.{HttpStatus, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.repository.ValpasHenkilö


class SureHakukoosteService(config: Config) extends ValpasHakukoosteService with Logging {
  private val baseUrl = "/suoritusrekisteri"

  private val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), baseUrl)

  def getHakukoosteet(oppijaOids: Set[ValpasHenkilö.Oid]): Either[HttpStatus, Seq[Hakukooste]] = {
    val encoder = json4sEncoderOf[Seq[ValpasHenkilö.Oid]]
    val decoder = parseJson[Seq[Hakukooste]] _
    http.post(s"$baseUrl/rest/v1/valpas/".toUri, oppijaOids.toSeq)(encoder)(decoder)
      .map(Right(_))
      .handle {
        case e: Exception =>
          logger.error("Error fetching hakukoosteet: " + e.toString)
          Left(ValpasErrorCategory.unavailable.sure())
      }
      .unsafePerformSync
  }
}
