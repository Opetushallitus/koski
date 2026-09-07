package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresHsl
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.xml.NodeSeqImplicits._

import scala.xml.{Elem, Node}

class HslServlet(implicit val application: KoskiApplication) extends SoapServlet with RequiresHsl with NoCache {
  private val hslService = new HslService(application)

  post("/hsl") {
    val soapResp = (for {
      xml <- xmlBody
      hetu <- extractHetuHsl(xml)
      opiskeluoikeudet <- hslService.HslOpiskeluoikeudet(hetu)
    } yield hslBody(xml, opiskeluoikeudet)) match {
      case Right(soap) => soap
      case Left(status) => haltWithStatus(status)
    }

    writeXml(soapResp)
  }

  private def extractHetuHsl(soap: Elem) =
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

object HslServlet {
  def extractXRoadClient(soap: Elem): Option[String] = {
    for {
      objectType <- (soap \\ "Envelope" \\ "Header" \\ "client").headOption.flatMap(_.attribute("http://x-road.eu/xsd/identifiers", "objectType"))
      xRoadInstance <- (soap \\ "Envelope" \\ "Header" \\ "client" \\ "xRoadInstance").headOption.map(_.text)
      memberClass <- (soap \\ "Envelope" \\ "Header" \\ "client" \\ "memberClass").headOption.map(_.text)
      memberCode <- (soap \\ "Envelope" \\ "Header" \\ "client" \\ "memberCode").headOption.map(_.text)
      subsystemCode <- (soap \\ "Envelope" \\ "Header" \\ "client" \\ "subsystemCode").headOption.map(_.text)
    } yield s"${objectType}:${xRoadInstance}/${memberClass}/${memberCode}/${subsystemCode}"
  }
}
