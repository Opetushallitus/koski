package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{RequiresHSL}
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.schema.filter.MyDataOppija
import fi.oph.koski.servlet.NoCache

import scala.xml.{Elem, Node, NodeSeq}

class HSLServlet(implicit val application: KoskiApplication) extends SoapServlet with RequiresHSL with NoCache {
  private val HSLService = new HSLService(application)

  post("/") {
    val soapResp = (for {
      xml <- xmlBody
      hetu <- extractHetu(xml)
      opiskeluoikeudet <- HSLService.HSLOpiskeluoikeudet(hetu)
    } yield hslBody(xml, opiskeluoikeudet)) match {
      case Right(soap) => soap
      case Left(status) => haltWithStatus(status)
    }

    writeXml(soapResp)
  }

  private def extractHetu(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "opintoOikeudetService" \\ "hetu")
      .headOption.map(_.text.trim)
      .toRight(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Hetu puuttuu"))

  private def hslBody(soap: Elem, resp: HslResponse): Node = {
    val jsonResponse = JsonSerializer.writeWithRoot(resp)
    replaceSoapBody(soap,
      <kns1:opintoOikeudetServiceResponse xmlns:kns1="http://docs.koski-xroad.fi/producer">
        <kns1:opintoOikeudet>
          {scala.xml.PCData(jsonResponse)}
        </kns1:opintoOikeudet>
      </kns1:opintoOikeudetServiceResponse>)
  }
}
