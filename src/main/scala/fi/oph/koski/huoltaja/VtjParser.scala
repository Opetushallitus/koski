package fi.oph.koski.huoltaja

import fi.oph.koski.log.Logging

import scala.xml.Elem

class VtjXmlParsingException(message: String) extends Exception(message)

object VtjParser extends Logging {
  def parseHuollettavatFromVtjResponse(xmlElem: Elem): List[VtjHuollettavaHenkilö] = {
    try {
      xmlElem
        .toList
        .flatMap(_ \\ "Henkilo" \ "Huollettava")
        .map(x =>
          VtjHuollettavaHenkilö(
            etunimet = (x \\ "Etunimet").text,
            sukunimi = (x \\ "Sukunimi").text,
            hetu = (x \\ "Henkilotunnus").text
          )
        )
        .filter(_.hetu.nonEmpty)
    } catch {
      case e: Exception =>
        logger.error(e)("Failed to parse VTJ response")
        throw new VtjXmlParsingException(s"Failed to parse Huollettavat from VTJ response")
    }
  }

  case class VtjPaluukoodi(koodi: String, arvo: String)

  def parsePaluukoodiFromVtjResponse(xmlElem: Elem): VtjPaluukoodi = {
    try {
      val paluukoodi = xmlElem \\ "Paluukoodi"
      VtjPaluukoodi(
        koodi = (paluukoodi \ "@koodi").text,
        arvo = paluukoodi.text
      )
    } catch {
      case e: Exception =>
        logger.error(e)("Failed to parse VTJ response")
        throw new VtjXmlParsingException(s"Failed to parse Paluukoodi from VTJ response")
    }
  }
}
