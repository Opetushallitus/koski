package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.http._
import fi.oph.koski.log.{Logging, NotLoggable}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockHuollettavatRepository

import scala.xml.{Elem, Node}


trait HuollettavatRepository {
  def getHuollettavat(huoltajaHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]]
}

object HuollettavatRepository {
  def apply(config: Config): HuollettavatRepository = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        new MockHuollettavatRepository
      case url =>
        val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), "/vtj-service", true)
        new RemoteHuollettavatRepository(http)
    }
  }
}

object HuollettavatVtjRepository {
  def parseHuollettavatFromVtjResponse(xmlString: Elem): List[VtjHuollettavaHenkilö] = xmlString
    .toList
    .flatMap(_ \\ "Henkilo" \ "Huollettava")
    .map(x =>
      VtjHuollettavaHenkilö(
        etunimet = (x \\ "Etunimet").text,
        sukunimi = (x \\ "Sukunimi").text,
        hetu = (x \\ "Henkilotunnus").text
      )
    )

  def apply(config: Config): HuollettavatRepository = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        new MockHuollettavatRepository
      case _ =>
        new RemoteHuollettavatRepositoryVtj(config)
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


class RemoteHuollettavatRepositoryVtj(config: Config) extends HuollettavatRepository with Logging {
  def soapEnvelope(node: Node) = <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Body>
      {node}
    </SOAP-ENV:Body>
  </SOAP-ENV:Envelope>

  def getHuollettavat(huoltajanHetu: String): Either[HttpStatus, List[VtjHuollettavaHenkilö]] = {
    val vtjConfig = if (Environment.usesAwsSecretsManager) VtjConfig.fromSecretsManager else VtjConfig.fromConfig(config)
    val http = Http(vtjConfig.serviceUrl, "vtj-soap-client")

    // val uri = "https://vtjkysely.2016.vrk.fi/sosowebsite/soso.asmx"
    val xmlBody =
      <tns:TeeHenkilonTunnusKysely>
        <tns:SoSoNimi>OPHREK</tns:SoSoNimi>
        <tns:Kayttajatunnus>
          ${vtjConfig.username}
        </tns:Kayttajatunnus>
        <tns:Salasana>
          ${vtjConfig.password}
        </tns:Salasana>
        <tns:Loppukayttaja>
          ${vtjConfig.password}
        </tns:Loppukayttaja>
        <tns:Laskutustiedot></tns:Laskutustiedot>
        <tns:Henkilotunnus>
          ${huoltajanHetu}
        </tns:Henkilotunnus>
        <tns:SahkoinenAsiointitunnus></tns:SahkoinenAsiointitunnus>
        <tns:VarmenteenMyontaja></tns:VarmenteenMyontaja>
        <tns:X509Certificate></tns:X509Certificate>
        <tns:VarmenteenVoimassaolotarkistus></tns:VarmenteenVoimassaolotarkistus>
        <tns:VarmenteenSulkulistatarkistus></tns:VarmenteenSulkulistatarkistus>
        <tns:Tunnistusportaali></tns:Tunnistusportaali>
        <tns:Vara1></tns:Vara1>
      </tns:TeeHenkilonTunnusKysely>

    try {
      val response = runIO(http.post(uri"/sosowebsite/soso.asmx", soapEnvelope(xmlBody))(Http.Encoders.xml)(Http.parseXml))
      val huollettavat = HuollettavatVtjRepository.parseHuollettavatFromVtjResponse(response)
      Right(huollettavat)
    } catch {
      case e: Throwable =>
        // healthMonitoring.foreach(_.setSubsystemStatus(Subsystem.Virta, operational = false))
        logger.error(e.toString)
        Left(KoskiErrorCategory.unavailable.huollettavat())
    }
  }
}

case class VtjConfig(serviceUrl: String, username: String, password: String, enduser: String) extends NotLoggable

object VtjConfig {
  def fromConfig(config: Config): VtjConfig = VtjConfig(
    config.getString("vtj.serviceUrl"),
    config.getString("vtj.username"),
    config.getString("vtj.password"),
    config.getString("vtj.enduser")
  )

  def fromSecretsManager: VtjConfig = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("VTJ Secrets", "VTJ_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[VtjConfig](secretId)
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

case class VtjHuoltajaHenkilöResponse(huollettavat: List[VtjHuollettavaHenkilö])

case class VtjHuollettavaHenkilö(etunimet: String, sukunimi: String, hetu: String)

object VtjHuollettavaHenkilö {
  def apply(h: LaajatOppijaHenkilöTiedot): VtjHuollettavaHenkilö = VtjHuollettavaHenkilö(
    etunimet = h.etunimet,
    sukunimi = h.sukunimi,
    hetu = h.hetu.get,
  )
}
