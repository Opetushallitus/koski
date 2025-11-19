package fi.oph.koski.huoltaja

import fi.oph.koski.http.Http.runIO
import fi.oph.koski.log.Logging

import scala.xml.Elem
import fi.oph.koski.xml.NodeSeqImplicits._

class VtjClient(runtimeCfg: VtjRuntimeConfig, httpClient: VtjHttpClient) extends Logging {
  logger.info(s"Using VTJ integration endpoint ${runtimeCfg.serviceUrl}")

  def getVtjResponse(huoltajanHetu: String, loppukayttaja: String): Elem =
    teeVtjKysely(
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
        <soapenv:Header/>
        <soapenv:Body>
          <tns:TeeHenkilonTunnusKysely xmlns:tns="http://tempuri.org/">
            <tns:SoSoNimi>OPHREK</tns:SoSoNimi>
            <tns:Kayttajatunnus>{runtimeCfg.username}</tns:Kayttajatunnus>
            <tns:Salasana>{runtimeCfg.password}</tns:Salasana>
            <tns:Loppukayttaja>{loppukayttaja}</tns:Loppukayttaja>
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
    )

  private def teeVtjKysely(xmlBody: Elem): Elem = {
    try {
      val response = runIO(httpClient.postXml(xmlBody))
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
