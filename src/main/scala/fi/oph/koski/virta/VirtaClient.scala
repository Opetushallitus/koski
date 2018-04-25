package fi.oph.koski.virta

import com.typesafe.config.Config
import fi.oph.koski.http.{Http, HttpConnectionException}
import fi.oph.koski.http.Http._
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.util.Files

import scala.xml.{Elem, Node}

object VirtaClient extends Logging {
  def apply(config: Config) = config.getString("virta.serviceUrl") match {
    case "mock" =>
      logger.info("Using mock Virta integration")
      MockVirtaClient
    case "" =>
      logger.info("Virta integration disabled")
      EmptyVirtaClient
    case _ =>
      val virtaConfig = VirtaConfig.fromConfig(config)
      logger.info("Using Virta integration endpoint " + virtaConfig.serviceUrl)
      TimedProxy[VirtaClient](RemoteVirtaClient(virtaConfig))
  }
}

trait VirtaClient {
  def opintotiedot(hakuehto: VirtaHakuehto): Option[Elem]
  def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String): Option[Elem]
}

object EmptyVirtaClient extends VirtaClient {
  override def opintotiedot(hakuehto: VirtaHakuehto) = None
  override def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String) = None
}

object MockVirtaClient extends VirtaClient {
  override def opintotiedot(hakuehto: VirtaHakuehto) = {
    hakuehto match {
      case VirtaHakuehtoHetu("020507-984V") =>
        throw new HttpConnectionException("MockVirtaClient testing opintotiedot failure", "POST", "http://localhost:666/")
      case VirtaHakuehtoHetu(hetu) =>
        loadXml("src/main/resources/mockdata/virta/opintotiedot/" + hetu + ".xml")
      case _ =>
        throw new RuntimeException("opintotiedot must be searched by VirtaHakuehtoHetu")
    }
  }
  override def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String) = {
    hakuehto match {
      case VirtaHakuehtoHetu("020507-984V") =>
        throw new HttpConnectionException("MockVirtaClient testing henkilötiedot failure", "POST", "http://localhost:666/")
      case VirtaHakuehtoHetu(hetu) =>
        loadXml("src/main/resources/mockdata/virta/henkilotiedot/" + hetu + ".xml")
      case _ =>
        throw new RuntimeException("henkilötiedot must be searched by VirtaHakuehtoHetu")
    }
  }

  private def loadXml(filename: String) = {
    Files.asString(filename).map(scala.xml.XML.loadString)
  }
}

case class RemoteVirtaClient(config: VirtaConfig) extends VirtaClient {
  def opintotiedot(hakuehto: VirtaHakuehto) = {
    val body = soapEnvelope(
      <OpiskelijanKaikkiTiedotRequest xmlns="http://tietovaranto.csc.fi/luku">
        {kutsuja}
        <Hakuehdot>{ hakuehdot(hakuehto) }</Hakuehdot>
      </OpiskelijanKaikkiTiedotRequest>)
    Some(runTask(Http(config.serviceUrl).post(uri"", body)(Http.Encoders.xml)(Http.parseXml)))
  }

  def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String) = {
    val body = soapEnvelope(
      <OpiskelijanTiedotRequest xmlns="http://tietovaranto.csc.fi/luku">
        {kutsuja}
        <Hakuehdot>{ hakuehdot(hakuehto) }<organisaatio>{oppilaitosNumero}</organisaatio></Hakuehdot>
      </OpiskelijanTiedotRequest>)
    Some(runTask(Http(config.serviceUrl).post(uri"", body)(Http.Encoders.xml)(Http.parseXml)))
  }

  private def hakuehdot(hakuehto: VirtaHakuehto) = hakuehto match {
    case VirtaHakuehtoHetu(hetu) => <henkilotunnus>{hetu}</henkilotunnus>
    case VirtaHakuehtoKansallinenOppijanumero(oppijanumero) => <kansallinenOppijanumero>{oppijanumero}</kansallinenOppijanumero>
  }

  private def kutsuja = <Kutsuja>
    <jarjestelma>{config.jarjestelma}</jarjestelma>
    <tunnus>{config.tunnus}</tunnus>
    <avain>{config.avain}</avain>
  </Kutsuja>


  private def soapEnvelope(node: Node) = <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Body>{node}</SOAP-ENV:Body>
  </SOAP-ENV:Envelope>
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
