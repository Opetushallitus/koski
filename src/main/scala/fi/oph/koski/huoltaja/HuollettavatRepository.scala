package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http._
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockHuollettavatRepository

trait HuollettavatRepository {
  def getHuollettavat(huoltajaHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]]
}

object HuollettavatRepository {
  def apply(config: Config): HuollettavatRepository = {
    if (Environment.isMockEnvironment(config) || config.getString("vtj.serviceUrl") == "mock") {
      new MockHuollettavatRepository
    } else {
      val vtjClient = VtjClientBuilder.build(config)
      new RemoteHuollettavatRepositoryVTJ(vtjClient)
    }
  }
}

class RemoteHuollettavatRepositoryVTJ(val vtjClient: VtjClient) extends HuollettavatRepository with Logging {
  def getHuollettavat(huoltajanHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    val response = vtjClient.getVtjResponse(huoltajanHetu, loppukayttaja = huoltajanHetu)
    val paluukoodi = VtjParser.parsePaluukoodiFromVtjResponse(response)
    paluukoodi.koodi match {
      case "0000" | "0018" => Right(VtjParser.parseHuollettavatFromVtjResponse(response))
      case "0001" | "0006" => Left(KoskiErrorCategory.unavailable.huollettavat())
      case _ =>
        logger.error("Tuntematon paluukoodi VTJ vastauksessa: " + response.toString())
        Left(KoskiErrorCategory.unavailable.huollettavat())
    }
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
            VtjHuollettavaHenkilö(eskari),
            VtjHuollettavaHenkilö(ylioppilasLukiolainen),
            VtjHuollettavaHenkilö("Olli", "Oiditon", "060488-681S"),
            VtjHuollettavaHenkilö(turvakielto),
          ))
        } else if (faijaFeilaa.hetu.contains(huoltajaHetu)) {
          Left(KoskiErrorCategory.unavailable.huollettavat())
        } else {
          Right(Nil)
        }
    }
  }
}

case class VtjHuollettavaHenkilö(etunimet: String, sukunimi: String, hetu: String)

object VtjHuollettavaHenkilö {
  def apply(h: LaajatOppijaHenkilöTiedot): VtjHuollettavaHenkilö = VtjHuollettavaHenkilö(
    etunimet = h.etunimet,
    sukunimi = h.sukunimi,
    hetu = h.hetu.get,
  )
}
