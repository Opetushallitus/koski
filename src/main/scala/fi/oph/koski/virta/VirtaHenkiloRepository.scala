package fi.oph.koski.virta

import fi.oph.koski.henkilo.HetuBasedHenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.common.log.Logging
import fi.oph.koski.schema.UusiHenkilö

import scala.util.control.NonFatal

case class VirtaHenkilöRepository(v: VirtaClient, accessChecker: VirtaAccessChecker) extends HetuBasedHenkilöRepository with Logging {
  def findByHetuDontCreate(hetu: String): Either[HttpStatus, Option[UusiHenkilö]] = {
    try {
      // Tänne tullaan vain, jos oppijaa ei löytynyt henkilöpalvelusta (ks CompositeHenkilöRepository)
      val hakuehto: VirtaHakuehtoHetu = VirtaHakuehtoHetu(hetu)
      // Oppijan organisaatiot haetaan ensin tällä raskaammalla kyselyllä
      val organisaatiot = v.opintotiedot(hakuehto).toSeq.flatMap(_ \\ "Opiskeluoikeus" \ "Myontaja").map(_.text)
      // Organisaatioden avulla haetaan henkilötietoja ja valitaan niistä ensimmäinen validi
      val opiskelijaNodes = organisaatiot.flatMap(v.henkilötiedot(hakuehto, _)).flatMap(_ \\ "Opiskelija")
      Right(opiskelijaNodes
        .map { opiskelijaNode => ((opiskelijaNode \ "Sukunimi").text, (opiskelijaNode \ "Etunimet").text) }
        .find { case (sukunimi, etunimet) => !sukunimi.isEmpty && !etunimet.isEmpty }
        .map { case (sukunimi, etunimet) =>
          val kutsumanimi = etunimet.split(" ").toList.headOption
          UusiHenkilö(hetu = hetu, etunimet = etunimet, kutsumanimi = kutsumanimi, sukunimi = sukunimi)
        })
    } catch {
      case NonFatal(e) =>
        logger.error(e)("Failed to fetch data from Virta")
        Left(KoskiErrorCategory.unavailable.virta())
    }
  }

  override def hasAccess(user: KoskiSession): Boolean = accessChecker.hasAccess(user)
}


