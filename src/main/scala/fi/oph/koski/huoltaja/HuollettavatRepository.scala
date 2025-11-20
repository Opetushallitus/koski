package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.http._
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockHuollettavatRepository

trait HuollettavatRepository {
  def getHuollettavat(oppija: OppijaHenkilö): Either[HttpStatus, List[VtjHuollettavaHenkilö]]
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

  def getHuollettavat(oppija: OppijaHenkilö): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    val huoltajanHetu = oppija.hetu.get

    val response = vtjClient.getVtjResponse(huoltajanHetu, loppukayttaja = huoltajanHetu)
    val paluukoodi = VtjParser.parsePaluukoodiFromVtjResponse(response)
    val koodi = Option(paluukoodi.koodi).getOrElse("")

    koodi match {
      case "0000" | "0018" =>
        logger.debug(s"VTJ-huollettavat kysely onnistui, oppija ${oppija.oid}")
        Right(VtjParser.parseHuollettavatFromVtjResponse(response))

      case "0001" | "0006" =>
        logger.info(s"VTJ-huollettavat kyselyn paluukoodi on $koodi oppijalle ${oppija.oid}")
        Left(KoskiErrorCategory.notFound.huoltajaaEiLöydyHetulla())

      case "0002" =>
        val hasNewHetu = VtjParser.parseNewHetuFromResponse(response, huoltajanHetu).nonEmpty
        if (hasNewHetu)
          logger.info(s"VTJ-huollettavat kyselyn mukaan hetu on vaihtunut (paluukoodi $koodi).")
        else
          logger.info(s"VTJ-huollettavat kyselyn mukaan henkilö on passivoitu (paluukoodi $koodi).")
        Left(KoskiErrorCategory.unavailable.huollettavat())

      case _ =>
        logger.warn(s"VTJ-huollettavat: tuntematon paluukoodi $koodi (oppija ${oppija.oid}).")
        Left(KoskiErrorCategory.internalError())
    }
  }
}

class MockHuollettavatRepository extends HuollettavatRepository {
  override def getHuollettavat(oppija: OppijaHenkilö): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    val huoltajaHetu = oppija.hetu.get
    ValpasMockHuollettavatRepository.getHuollettavat(oppija) match {
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
