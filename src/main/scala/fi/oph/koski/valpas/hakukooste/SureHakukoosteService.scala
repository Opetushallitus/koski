package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.http.Http.{StringToUriConverter, parseJson}
import fi.oph.koski.http.{HttpStatus, HttpStatusException, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.repository.ValpasHenkilö


class SureHakukoosteService(config: Config) extends ValpasHakukoosteService with Logging with Timing {
  private val baseUrl = "/suoritusrekisteri"

  private val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), baseUrl)

  def getHakukoosteet(oppijaOids: Set[ValpasHenkilö.Oid], errorClue: String = ""): Either[HttpStatus, Seq[Hakukooste]] = {
    val encoder = json4sEncoderOf[Seq[ValpasHenkilö.Oid]]
    val decoder = parseJson[Seq[Hakukooste]] _

    val timedBlockname = if (oppijaOids.size == 1) "getHakukoosteetSingle" else "getHakukoosteetMultiple"

    timed(timedBlockname, 10) {
      http.post(s"$baseUrl/rest/v1/valpas/".toUri, oppijaOids.toSeq)(encoder)(decoder)
        .map(Right(_))
        .handle {
          case e: HttpStatusException =>
            logger.error(s"Bad response from Suoritusrekisteri for ${errorClue}: " + e.toString)
            Left(ValpasErrorCategory.unavailable.sure())
          case e: Exception =>
            logger.error(s"Error fetching hakukoosteet for ${errorClue}: " + e.toString)
            Left(ValpasErrorCategory.internalError())
        }
        .unsafePerformSync
    }.map(_.map(withVaroitusPuuttuvastaSisällöstäLogitus(errorClue)))
  }

  // Logita varoitus tyhjistä kentistä, joita ilman käyttöliittymä toimii, mutta joiden arvon pitäisi kuitenkin aina tulla hakukoostepalvelusta
  private def withVaroitusPuuttuvastaSisällöstäLogitus(baseErrorClue: String = "")(hakukooste: Hakukooste) = {
    if (hakukooste.hakuNimi.valueList.isEmpty) {
      val errorClue = s"${baseErrorClue} oppijaOid:${hakukooste.oppijaOid} hakemusOid:${hakukooste.hakemusOid}"
      logger.warn(s"Properties missing for ${errorClue}: hakuNimi}")
    }

    hakukooste.hakutoiveet.map(hakutoive => {
      val puuttuvatPropertyt = Seq(
        ("valintatila", hakutoive.valintatila.isEmpty),
        ("vastaanottotieto", hakutoive.vastaanottotieto.isEmpty),
        ("ilmoittautumistila", hakutoive.ilmoittautumistila.isEmpty),
        ("harkinnanvaraisuus", hakutoive.harkinnanvaraisuus.isEmpty),
        ("hakukohdeNimi", hakutoive.hakukohdeNimi.valueList.isEmpty),
        ("organisaatioNimi", hakutoive.organisaatioNimi.valueList.isEmpty),
        ("koulutusNimi", hakutoive.koulutusNimi.valueList.isEmpty)
      ).flatMap({
        case (a, true) => Seq(a)
        case _ => Seq()
      })

      if (!puuttuvatPropertyt.isEmpty) {
        val errorClue = s"${baseErrorClue} oppijaOid:${hakukooste.oppijaOid} hakemusOid:${hakukooste.hakemusOid} hakukohdeOid:${hakutoive.hakukohdeOid}"
        logger.warn(s"Properties missing for ${errorClue}: ${puuttuvatPropertyt.mkString(",")}")
      }
    })

    hakukooste
  }
}
