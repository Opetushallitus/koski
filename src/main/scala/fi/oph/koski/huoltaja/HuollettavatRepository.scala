package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.AppConfig
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{eskari, faija, faijaFeilaa, ylioppilasLukiolainen}
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockHuollettavatRepository


trait HuollettavatRepository {
  def getHuollettavat(huoltajaHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]]
}

object HuollettavatRepository {
  def apply(config: Config): HuollettavatRepository = {
    AppConfig.virkailijaOpintopolkuUrl(config) match {
      case None =>
        new MockHuollettavatRepository
      case Some(_) =>
        val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), "/vtj-service", true)
        new RemoteHuollettavatRepository(http)
    }
  }
}

class RemoteHuollettavatRepository(val http: Http) extends HuollettavatRepository with Logging {
  def getHuollettavat(huoltajanHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    runIO(
      http.get(uri"/vtj-service/resources/vtj/$huoltajanHetu")(Http
        .parseJson[VtjHuoltajaHenkilöResponse])
        .map(x => Right(x.huollettavat))
        .handleError {
          case e: Exception =>
            logger.error(e.toString)
            Left(KoskiErrorCategory.unavailable.huollettavat())
        }
    )
  }
}

class MockHuollettavatRepository extends HuollettavatRepository {
  override def getHuollettavat(huoltajaHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    ValpasMockHuollettavatRepository.getHuollettavat(huoltajaHetu) match {
      case Some(l) =>
        Right(l)

      case None =>
        if (faija.hetu.contains(huoltajaHetu)) {
          Right(List(
            VtjHuollettavaHenkilö(eskari.etunimet, eskari.sukunimi, eskari.hetu.get),
            VtjHuollettavaHenkilö(ylioppilasLukiolainen.etunimet, ylioppilasLukiolainen.sukunimi, ylioppilasLukiolainen.hetu.get),
            VtjHuollettavaHenkilö("Olli", "Oiditon", "060488-681S")
          ))
        } else if (faijaFeilaa.hetu.contains(huoltajaHetu)) {
          Left(KoskiErrorCategory.unavailable.huollettavat())
        } else {
          Right(Nil)
        }
    }
  }
}

case class VtjHuoltajaHenkilöResponse(huollettavat: List[VtjHuollettavaHenkilö])
case class VtjHuollettavaHenkilö(etunimet: String, sukunimi: String, hetu: String)
