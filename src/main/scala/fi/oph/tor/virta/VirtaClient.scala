package fi.oph.tor.virta

import com.typesafe.config.Config
import fi.oph.tor.config.TorApplication
import fi.oph.tor.http.Http
import fi.oph.tor.util.Files

import scala.xml.{Elem, PrettyPrinter}



object VirtaClient {
  def apply(config: Config) = config.hasPath("virta.serviceUrl") match {
    case false => MockVirtaClient
    case true => RemoteVirtaClient(VirtaConfig.fromConfig(config))
  }
}

trait VirtaClient {
  def fetchVirtaData(hakuehto: VirtaHakuehto): Option[Elem]
}

object MockVirtaClient extends VirtaClient {
  override def fetchVirtaData(hakuehto: VirtaHakuehto) = {
    hakuehto match {
      case VirtaHakuehtoHetu(hetu) =>
        Files.asString("src/main/resources/mockdata/virta/" + hetu + ".xml").map(scala.xml.XML.loadString)
      case _ =>
        throw new UnsupportedOperationException()
    }
  }
}

case class RemoteVirtaClient(config: VirtaConfig) extends VirtaClient {
  def fetchVirtaData(hakuehto: VirtaHakuehto) = {
    val hakuehdot = hakuehto match {
      case VirtaHakuehtoHetu(hetu) => <henkilotunnus>{hetu}</henkilotunnus>
      case VirtaHakuehtoKansallinenOppijanumero(oppijanumero) => <kansallinenOppijanumero>{oppijanumero}</kansallinenOppijanumero>
    }
    val body = <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Body><OpiskelijanKaikkiTiedotRequest xmlns="http://tietovaranto.csc.fi/luku">
      <Kutsuja>
        <jarjestelma>{config.jarjestelma}</jarjestelma>
        <tunnus>{config.tunnus}</tunnus>
        <avain>{config.avain}</avain>
      </Kutsuja>
      <Hakuehdot>{ hakuehdot }</Hakuehdot>
    </OpiskelijanKaikkiTiedotRequest></SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
    Some(Http(config.serviceUrl).post("", body)(Http.Encoders.xml, Http.parseXml))
  }
}

sealed trait VirtaHakuehto
case class VirtaHakuehtoHetu(hetu: String) extends VirtaHakuehto
case class VirtaHakuehtoKansallinenOppijanumero(numero: String) extends VirtaHakuehto

case class VirtaConfig(serviceUrl: String, jarjestelma: String, tunnus: String, avain: String)

object VirtaConfig {
  // Virta test environment config, see http://virtawstesti.csc.fi/
  val virtaTestEnvironment = VirtaConfig("http://virtawstesti.csc.fi/luku106/OpiskelijanTiedot", "", "", "salaisuus")
  def fromConfig(config: Config) = VirtaConfig(config.getString("virta.serviceUrl"), config.getString("virta.jarjestelma"), config.getString("virta.tunnus"), config.getString("virta.avain"))
}
