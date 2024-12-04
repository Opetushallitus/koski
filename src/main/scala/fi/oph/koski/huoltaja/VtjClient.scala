package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.Http._
import fi.oph.koski.log.Logging

import scala.xml.Elem

class VtjConfigException(message: String) extends Exception(message)

case class VtjClient(appConfig: Config) extends Logging {
  val vtjConfig: VtjConfig = try {
    if (Environment.usesAwsSecretsManager) VtjConfig.fromSecretsManager else VtjConfig.fromAppConfig(appConfig)
  } catch {
    case e: Exception =>
      logger.error(e)("Configuration error for VTJ client")
      throw new VtjConfigException(e.getMessage)
  }

  private val vtjHttpClient = new VtjHttpClient(vtjConfig)

  logger.info("Using Vtj integration endpoint " + vtjConfig.serviceUrl)

  def getVtjResponse(huoltajanHetu: String, loppukayttaja: String): Elem = teeVtjKysely {
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
      <soapenv:Header/>
      <soapenv:Body>
        <tns:TeeHenkilonTunnusKysely xmlns:tns="http://tempuri.org/">
          <tns:SoSoNimi>OPHREK</tns:SoSoNimi>
          <tns:Kayttajatunnus>{vtjConfig.username}</tns:Kayttajatunnus>
          <tns:Salasana>{vtjConfig.password}</tns:Salasana>
          <tns:Loppukayttaja>{loppukayttaja}</tns:Loppukayttaja>
          <tns:Laskutustiedot></tns:Laskutustiedot>
          <tns:Henkilotunnus>{huoltajanHetu}</tns:Henkilotunnus>
          <tns:SahkoinenAsiointitunnus></tns:SahkoinenAsiointitunnus>
          <tns:VarmenteenMyontaja></tns:VarmenteenMyontaja>
          <tns:X509Certificate></tns:X509Certificate>
          <tns:VarmenteenVoimassaolotarkistus></tns:VarmenteenVoimassaolotarkistus>
          <tns:VarmenteenSulkulistatarkistus></tns:VarmenteenSulkulistatarkistus>
          <tns:Tunnistusportaali></tns:Tunnistusportaali>
          <tns:Vara1></tns:Vara1>
        </tns:TeeHenkilonTunnusKysely>
      </soapenv:Body>
    </soapenv:Envelope>
  }

  private def teeVtjKysely(xmlBody: Elem): Elem = {
    try {
      val response = runIO(vtjHttpClient.postXml(xmlBody))
      response
    } catch {
      case e: javax.net.ssl.SSLException =>
        logger.error(e)("SSL error during VTJ request")
        throw e
      case e: java.net.ConnectException =>
        logger.error(e)("Connection error during VTJ request")
        throw e
      case e: Throwable =>
        logger.error(e)("Unexpected error during VTJ request")
        throw e
    }
  }
}
