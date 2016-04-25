package fi.oph.tor.virta

import fi.oph.tor.http.Http

import scala.xml.PrettyPrinter

// Client for the Virta Opintotietopalvelu, see https://confluence.csc.fi/display/VIRTA/VIRTA-opintotietopalvelu
object VirtaClient extends App {
  val hetulla: VirtaHakuehtoHetu = VirtaHakuehtoHetu("290204A9999")
  val oppijanumerolla = VirtaHakuehtoKansallinenOppijanumero("aed09afd87a8c6d76b76bbd")
  VirtaClient(VirtaConfig.virtaTestEnvironment).fetchVirtaData(hetulla)
}

case class VirtaClient(config: VirtaConfig) {
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
    val result = Http(config.serviceUrl).post("", body)(Http.Encoders.xml, Http.parseXml)
    println(new PrettyPrinter(200, 2).format(result))
  }
}

sealed trait VirtaHakuehto
case class VirtaHakuehtoHetu(hetu: String) extends VirtaHakuehto
case class VirtaHakuehtoKansallinenOppijanumero(numero: String) extends VirtaHakuehto

case class VirtaConfig(serviceUrl: String = "http://virtawstesti.csc.fi/luku/OpiskelijanTiedot",
                       jarjestelma: String = "",
                       tunnus: String = "",
                       avain: String = "salaisuus")

object VirtaConfig {
  // Virta test environment config, see http://virtawstesti.csc.fi/
  val virtaTestEnvironment = VirtaConfig()
}
