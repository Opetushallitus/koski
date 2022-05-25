package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.servlet.NoCache

import scala.xml.{Elem, Node, NodeSeq}

class PalveluvaylaServlet(implicit val application: KoskiApplication) extends SoapServlet with RequiresLuovutuspalvelu with NoCache {
  private val suomiFiService = new SuomiFiService(application)

  post("/suomi-fi-rekisteritiedot") {
    logger.info("POST /suomi-fi-rekisteritiedot")
    requireSuomiFiUser
    logger.info("Oikeudet kunnossa")
    val soapResp = (for {
      xml <- xmlBody
      hetu <- extractHetu(xml)
      opiskeluoikeudet <- suomiFiService.suomiFiOpiskeluoikeudet(hetu)
    } yield suomiFiBody(xml,opiskeluoikeudet)) match {
      case Right(soap) => {
        logger.info("Parsittiin hetu onnistuneesti")
        soap
      }
      case Left(status) => {
        logger.info("Hetun parsinta epäonnistui")
        haltWithStatus(status)
      }
    }
    logger.info("Lähetetään vastaus: " + soapResp.toString)
    writeXml(soapResp)
  }

  // This check is in addition to RequiresLuovutuspalvelu
  private def requireSuomiFiUser = {
    logger.info("Käyttöoikeuksien tarkistus")
    logger.info("Sesssion oid: " + koskiSession.oid)
    logger.info("suomi-fi-user-oid: " + application.config.getString("suomi-fi-user-oid"))

    if (koskiSession.oid != application.config.getString("suomi-fi-user-oid")) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }
  }

  private def extractHetu(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "suomiFiRekisteritiedot" \\ "hetu")
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
                {oo.tila.map(t => <tila>{localizedStringToXml(t)}</tila>).getOrElse(NodeSeq.Empty)}
                {oo.alku.map(a => <alku>{a.toString}</alku>).getOrElse(NodeSeq.Empty)}
                {oo.loppu.map(l => <loppu>{l.toString}</loppu>).getOrElse(NodeSeq.Empty)}
                <nimi>{localizedStringToXml(oo.nimi)}</nimi>
              </opiskeluoikeus>)}
            </opiskeluoikeudet>
          </oppilaitos>)}
        </oppilaitokset>
      </suomiFiRekisteritiedotResponse>)
  }

  private def localizedStringToXml(s: LocalizedString) =
    s.values.map { case (k, v) => <x>{v}</x>.copy(label = k) }
}
