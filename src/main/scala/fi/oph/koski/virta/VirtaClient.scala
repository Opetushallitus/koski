package fi.oph.koski.virta

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpConnectionException}
import fi.oph.koski.log.{Logging, NotLoggable, TimedProxy}
import fi.oph.koski.util.Files

import scala.xml.{Elem, Node}

object VirtaClient extends Logging {
  def apply(config: Config) = {
    val serviceUrl = {
      if (Environment.usesAwsSecretsManager) {
        VirtaConfig.fromSecretsManager.serviceUrl
      } else {
        config.getString("virta.serviceUrl")
      }
    }
    serviceUrl match {
      case "mock" =>
        logger.info("Using mock Virta integration")
        MockVirtaClient(config)
      case "" =>
        logger.info("Virta integration disabled")
        EmptyVirtaClient
      case _ =>
        val virtaConfig = if (Environment.usesAwsSecretsManager) VirtaConfig.fromSecretsManager else VirtaConfig.fromConfig(config)
        logger.info("Using Virta integration endpoint " + virtaConfig.serviceUrl)
        TimedProxy[VirtaClient](RemoteVirtaClient(virtaConfig))
    }
  }
}

trait VirtaClient {
  def opintotiedot(hakuehto: VirtaHakuehto): Option[Elem]
  def opintotiedotMassahaku(hakuehdot: List[VirtaHakuehto]): Option[Elem]
  def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String): Option[Elem]
}

object EmptyVirtaClient extends VirtaClient {
  override def opintotiedot(hakuehto: VirtaHakuehto) = None
  override def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String) = None
  override def opintotiedotMassahaku(hakuehdot: List[VirtaHakuehto]): Option[Elem] = None
}

case class MockVirtaClient(config: Config) extends VirtaClient {
  private lazy val mockDataDir = config.getString("virta.mockDataDir")
  override def opintotiedot(hakuehto: VirtaHakuehto) = haeOpintotiedot(hakuehto)

  override def opintotiedotMassahaku(hakuehdot: List[VirtaHakuehto]): Option[Elem] = if (containsMixedEhdot(hakuehdot)) {
    // Tällä hetkellä Virrasta ei voi hakea sekä hetulla että oppijanumerolla
    throw new HttpConnectionException("Validation error", "POST", "http://localhost:666/")
  } else {
    hakuehdot.flatMap(haeOpintotiedot)
     .map(_ \\ "Opiskelija") match {
      case Nil => None
      case nodes => Some(<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
          <SOAP-ENV:Header/>
          <SOAP-ENV:Body>
            <virtaluku:OpiskelijanKaikkiTiedotResponse xmlns:virtaluku="http://tietovaranto.csc.fi/luku">
              <virta:Virta xmlns:virta="urn:mace:funet.fi:virta/2015/09/01">
              {nodes}
              </virta:Virta>
            </virtaluku:OpiskelijanKaikkiTiedotResponse>
          </SOAP-ENV:Body>
        </SOAP-ENV:Envelope>)
    }
  }

  override def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String) = {
    hakuehto match {
      case VirtaHakuehtoHetu("250390-680P") =>
        throw new HttpConnectionException("MockVirtaClient testing henkilötiedot failure", "POST", "http://localhost:666/")
      case VirtaHakuehtoHetu(hetu) => Hetu.validate(hetu, false) match {
        case Left(_) => throw new HttpConnectionException("MockVirtaClient testing henkilötiedot failure", "POST", "http://localhost:666/")
        case Right(hetu) => loadXml(s"$mockDataDir/henkilotiedot/$hetu.xml")
      }
      case _ =>
        throw new RuntimeException("henkilötiedot must be searched by VirtaHakuehtoHetu")
    }
  }

  private def haeOpintotiedot(virtaHakuehto: VirtaHakuehto) = {
    val tunnus = virtaHakuehto match {
      case VirtaHakuehtoHetu("250390-680P") =>
        throw new HttpConnectionException("MockVirtaClient testing opintotiedot failure", "POST", "http://localhost:666/")
      case VirtaHakuehtoHetu(hetu) => Hetu.validate(hetu, false) match {
        case Left(_) => throw new HttpConnectionException("MockVirtaClient testing opintotiedot failure", "POST", "http://localhost:666/")
        case Right(hetu) => hetu
      }
      case VirtaHakuehtoKansallinenOppijanumero(oid) => oid
    }
    loadXml(s"$mockDataDir/opintotiedot/$tunnus.xml")
  }

  private def containsMixedEhdot(hakuehdot: List[VirtaHakuehto]): Boolean =
    hakuehdot.exists(_.isInstanceOf[VirtaHakuehtoHetu]) && hakuehdot.exists(_.isInstanceOf[VirtaHakuehtoKansallinenOppijanumero])

  private def loadXml(filename: String) = {
    Files.asString(filename).map(scala.xml.XML.loadString)
  }
}

case class RemoteVirtaClient(config: VirtaConfig) extends VirtaClient {
  private val http = Http(config.serviceUrl, "virta")
  def opintotiedot(hakuehto: VirtaHakuehto): Option[Elem] = performHaku {
    <OpiskelijanKaikkiTiedotRequest xmlns="http://tietovaranto.csc.fi/luku">
      {kutsuja}
      <Hakuehdot>{hakuehdotXml(hakuehto)}</Hakuehdot>
    </OpiskelijanKaikkiTiedotRequest>
  }

  def opintotiedotMassahaku(hakuehdot: List[VirtaHakuehto]): Option[Elem] = if (hakuehdot.nonEmpty) {
    performHaku {
      <OpiskelijanKaikkiTiedotListaRequest xmlns="http://tietovaranto.csc.fi/luku">
        {kutsuja}
        <Hakuehdot>{hakuehdotXml(hakuehdot)}</Hakuehdot>
      </OpiskelijanKaikkiTiedotListaRequest>
    }
  } else {
    None
  }

  def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String): Option[Elem] = performHaku {
    <OpiskelijanTiedotRequest xmlns="http://tietovaranto.csc.fi/luku">
      {kutsuja}
      <Hakuehdot>
        {hakuehdotXml(hakuehto)}
        <organisaatio>{oppilaitosNumero}</organisaatio>
      </Hakuehdot>
    </OpiskelijanTiedotRequest>
  }

  private def performHaku(xmlBody: Elem): Option[Elem] = {
    Some(runIO(http.post(uri"", soapEnvelope(xmlBody))(Http.Encoders.xml)(Http.parseXml)))
  }

  private def hakuehdotXml(hakuehto: VirtaHakuehto) = hakuehto match {
    case VirtaHakuehtoHetu(hetu) => <henkilotunnus>{hetu}</henkilotunnus>
    case VirtaHakuehtoKansallinenOppijanumero(oppijanumero) => <kansallinenOppijanumero>{oppijanumero}</kansallinenOppijanumero>
  }

  private def hakuehdotXml(hakuehdot: List[VirtaHakuehto]) = hakuehdot.map {
    case VirtaHakuehtoHetu(hetu) => <henkilotunnusLista>{hetu}</henkilotunnusLista>
    case VirtaHakuehtoKansallinenOppijanumero(oppijanumero) => <kansallinenOppijanumeroLista>{oppijanumero}</kansallinenOppijanumeroLista>
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

case class VirtaConfig(serviceUrl: String, jarjestelma: String, tunnus: String, avain: String) extends NotLoggable

object VirtaConfig {
  // Virta test environment config, see http://virtawstesti.csc.fi/
  val virtaTestEnvironment = VirtaConfig("http://virtawstesti.csc.fi/luku106/OpiskelijanTiedot", "", "", "salaisuus")
  def fromConfig(config: Config) = VirtaConfig(
    config.getString("virta.serviceUrl"),
    config.getString("virta.jarjestelma"),
    config.getString("virta.tunnus"),
    config.getString("virta.avain"))
  def fromSecretsManager: VirtaConfig = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Virta secrets", "VIRTA_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[VirtaConfig](secretId)
  }
}
