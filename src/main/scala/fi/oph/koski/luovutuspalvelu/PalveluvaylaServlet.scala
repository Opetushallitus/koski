package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.servlet.{ApiServlet, NoCache}

import scala.xml.{Elem, NodeSeq, PCData}

class PalveluvaylaServlet(implicit val application: KoskiApplication) extends SoapServlet with ApiServlet with RequiresLuovutuspalvelu with NoCache {
  private val suomiFiService = new SuomiFiService(application)

  post("/suomi-fi-rekisteritiedot") {
    requireSuomiFiUser
    val soapResp = (for {
      xml <- xmlBody
      hetu <- extractHetu(xml)
      opiskeluoikeudet <- suomiFiService.suomiFiOpiskeluoikeudet(hetu)
    } yield suomiFiBody(xml,opiskeluoikeudet)) match {
      case Right(soap) => soap
      case Left(status) => soapError(status)
    }

    writeXml(soapResp)
  }

  private def requireSuomiFiUser =
    if (koskiSession.oid != application.config.getString("suomi-fi-user-oid")) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }

  private def extractHetu(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "suomiFiRekisteritiedot" \\ "hetu")
      .headOption.map(_.text.trim)
      .toRight(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Hetu puuttuu"))

  private def suomiFiBody(soap: Elem, o: SuomiFiResponse): NodeSeq = {
    replaceSoapBody(soap,
      <ns1:suomiFiRekisteritiedotResponse xmlns:ns1="http://docs.koski-xroad.fi/producer">
        {PCData(JsonSerializer.writeWithRoot(o))}
      </ns1:suomiFiRekisteritiedotResponse>)
  }
}
