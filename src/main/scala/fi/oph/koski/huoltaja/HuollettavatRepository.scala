package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.henkilo.MockOppijat.{eskari, faija, eiKoskessa, faijaFeilaa}
import fi.oph.koski.http.Http._
import fi.oph.koski.http._

trait HuollettavatRepository {
  def getHuollettavat(huoltajaHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]]
}

object HuollettavatRepository {
  def apply(config: Config): HuollettavatRepository = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        new MockHuollettavatRepository
      case url =>
        val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), "/vtj-service", sessionCookieName = "SESSION")
        new RemoteHuollettavatRepository(http)
    }
  }
}

class RemoteHuollettavatRepository(val http: Http) extends HuollettavatRepository {
  def getHuollettavat(huoltajanHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    http.get(uri"/vtj-service/resources/vtj/$huoltajanHetu")(Http.parseJson[VtjHuoltajaHenkilöResponse])
      .map(x => Right(x.huollettavat))
      .handle {
        case _: Exception => Left(KoskiErrorCategory.unavailable.huollettavat())
      }
      .run
  }
}

class MockHuollettavatRepository extends HuollettavatRepository {
  override def getHuollettavat(huoltajaHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    if (faija.hetu.contains(huoltajaHetu)) {
      Right(List(
        VtjHuollettavaHenkilö(eskari.etunimet, eskari.sukunimi, eskari.hetu.get),
        VtjHuollettavaHenkilö(eiKoskessa.etunimet, eiKoskessa.sukunimi, eiKoskessa.hetu.get)
      ))
    } else if (faijaFeilaa.hetu.contains(huoltajaHetu)) {
      Left(KoskiErrorCategory.unavailable.huollettavat())
    } else {
      Right(Nil)
    }
  }
}

case class VtjHuoltajaHenkilöResponse(huollettavat: List[VtjHuollettavaHenkilö])
case class VtjHuollettavaHenkilö(etunimet: String, sukunimi: String, hetu: String)
