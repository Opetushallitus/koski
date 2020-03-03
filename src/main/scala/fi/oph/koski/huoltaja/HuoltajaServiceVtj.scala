package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging


class HuoltajaServiceVtj(config: Config, henkilöRepository: HenkilöRepository) extends Logging {
  private val huollettavatRepository = HuollettavatRepository(config)

  def getHuollettavat(hetu: String): HuollettavatSearchResult = try {
    huollettavatRepository.getHuollettavat(hetu).map(_.flatMap(oiditHuollettaville)) match {
      case Right(huollettavat) =>
        HuollettavienHakuOnnistui(huollettavat)
      case Left(error) =>
        logger.error(s"Huollettavien haku epäonnistui. ${error.toString}")
        HuollettavienHakuEpäonnistui(KoskiErrorCategory.unavailable.huollettavat())
    }
  }
  catch {
    case _: Exception =>
      logger.error("Huollettavien haku epäonnistui")
      HuollettavienHakuEpäonnistui(KoskiErrorCategory.unavailable.huollettavat())
  }

  private def oiditHuollettaville(vtj: VtjHuollettavaHenkilö) = {
    henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(vtj.hetu)
      .map(h => Huollettava(h.etunimet, h.sukunimi, Some(h.oid)))
      .orElse(Some(Huollettava(vtj.etunimet, vtj.sukunimi, oid = None)))
  }
}

trait HuollettavatSearchResult

case class HuollettavienHakuEpäonnistui(status: HttpStatus) extends HuollettavatSearchResult

case class HuollettavienHakuOnnistui(huollettavat: List[Huollettava]) extends HuollettavatSearchResult

case class Huollettava(etunimet: String, sukunimi: String, oid: Option[String])
