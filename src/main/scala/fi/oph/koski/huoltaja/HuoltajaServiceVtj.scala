package fi.oph.koski.huoltaja

import fi.oph.koski.henkilo.{HenkilöRepository, OppijaHenkilö}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging

class HuoltajaServiceVtj(henkilöRepository: HenkilöRepository, huollettavatRepository: HuollettavatRepository) extends Logging {
  def getHuollettavat(oppija: OppijaHenkilö): HuollettavatSearchResult = try {
    oppija.hetu match {
      case Some(_) =>
        huollettavatRepository
          .getHuollettavat(oppija)
          .map(_.flatMap(oiditHuollettaville))
          .map(_.filterNot(onMenehtynyt)) match {
          case Right(huollettavat) =>
            HuollettavienHakuOnnistui(huollettavat)
          case Left(error) =>
            logger.error(s"Huollettavien haku epäonnistui. ${error.toString}")
            HuollettavienHakuEpäonnistui(KoskiErrorCategory.unavailable.huollettavat())
        }
      case None =>
        logger.info(s"Oppijalle ${oppija.oid} ei löydy hetua, asetetaan tyhjä hakutulos huollettaville")
        HuollettavienHakuOnnistui(List.empty)
    }
  } catch {
    case e: Exception =>
      logger.error(s"Huollettavien haku epäonnistui. ${e.toString}")
      HuollettavienHakuEpäonnistui(KoskiErrorCategory.unavailable.huollettavat())
  }

  private def oiditHuollettaville(vtj: VtjHuollettavaHenkilö) = {
    henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(vtj.hetu)
      .map(h => Huollettava(h.etunimet, h.sukunimi, Some(h.oid), h.hetu))
      .orElse(Some(Huollettava(vtj.etunimet, vtj.sukunimi, oid = None, Some(vtj.hetu))))
  }

  private def onMenehtynyt(h: Huollettava): Boolean = {
    h.oid.flatMap(oid => henkilöRepository.findByOid(oid)).exists(_.kuolinpäivä.nonEmpty)
  }
}

trait HuollettavatSearchResult

case class HuollettavienHakuEpäonnistui(status: HttpStatus) extends HuollettavatSearchResult

case class HuollettavienHakuOnnistui(huollettavat: List[Huollettava]) extends HuollettavatSearchResult

case class Huollettava(
  etunimet: String,
  sukunimi: String,
  oid: Option[String],
  hetu: Option[String],
)
