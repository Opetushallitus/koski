package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresSuomiFiOrHsl
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.servlet.NoCache

import scala.xml.{Elem, Node, NodeSeq}

class PalveluvaylaServlet(implicit val application: KoskiApplication) extends SoapServlet with RequiresSuomiFiOrHsl with NoCache {
  private val suomiFiService = new SuomiFiService(application)
  private val hslService = new HslService(application)

  post("/suomi-fi-rekisteritiedot") {
    requireSuomiFiUser
    val soapResp = (for {
      xml <- xmlBody
      hetu <- extractHetu(xml)
      opiskeluoikeudet <- suomiFiService.suomiFiOpiskeluoikeudet(hetu)
    } yield suomiFiBody(xml, opiskeluoikeudet)) match {
      case Right(soap) => soap
      case Left(status) => haltWithStatus(status)
    }

    writeXml(soapResp)
  }

  if (!Environment.isProdEnvironment(application.config)) {
    post("/hsl") {
      requireHslUser
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
  }

  private def requireSuomiFiUser =
    if (koskiSession.oid != application.config.getString("suomi-fi-user-oid")) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }

  private def requireHslUser =
    if (koskiSession.oid != application.config.getString("hsl-user-oid")) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }

  private def extractHetu(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "suomiFiRekisteritiedot" \\ "hetu")
      .headOption.map(_.text.trim)
      .toRight(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Hetu puuttuu"))

  private def extractHetuHsl(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "opintoOikeudetService" \\ "hetu")
      .headOption.map(_.text.trim)
      .toRight(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Hetu puuttuu"))

  private def suomiFiBody(soap: Elem, resp: SuomiFiResponse): Node = {
    replaceSoapBody(soap,
      <suomiFiRekisteritiedotResponse xmlns="http://docs.koski-xroad.fi/producer">
        <oppilaitokset>
          {resp.oppilaitokset.map(ol =>
          <oppilaitos>
            <nimi>
              {localizedStringToXml(ol.nimi)}
            </nimi>
            <opiskeluoikeudet>
              {ol.opiskeluoikeudet.map(oo =>
              <opiskeluoikeus>
                {oo.tila.map(t => <tila>
                {localizedStringToXml(t)}
              </tila>).getOrElse(NodeSeq.Empty)}{oo.alku.map(a => <alku>
                {a.toString}
              </alku>).getOrElse(NodeSeq.Empty)}{oo.loppu.map(l => <loppu>
                {l.toString}
              </loppu>).getOrElse(NodeSeq.Empty)}<nimi>
                {localizedStringToXml(oo.nimi)}
              </nimi>
              </opiskeluoikeus>)}
            </opiskeluoikeudet>
          </oppilaitos>)}
        </oppilaitokset>
      </suomiFiRekisteritiedotResponse>)
  }

  private def hslBody(soap: Elem, resp: HslResponse): Node = {
    val jsonResponse = JsonSerializer.writeWithRoot(resp)
    replaceSoapBody(soap,
      <kns1:opintoOikeudetServiceResponse xmlns:kns1="http://docs.koski-xroad.fi/producer">
        <kns1:opintoOikeudet>
          {scala.xml.PCData(jsonResponse)}
        </kns1:opintoOikeudet>
      </kns1:opintoOikeudetServiceResponse>)
  }

  private def localizedStringToXml(s: LocalizedString) =
    s.values.map { case (k, v) => <x>{v}</x>.copy(label = k) }
}
