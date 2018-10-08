package fi.oph.koski.virta

import fi.oph.koski.henkilo.{FindByHetu, OpintopolkuHenkilöRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{HenkilötiedotJaOid, UusiHenkilö}

import scala.util.control.NonFatal

case class VirtaHenkilöRepository(v: VirtaClient, henkilöpalvelu: OpintopolkuHenkilöRepository, accessChecker: VirtaAccessChecker) extends FindByHetu with Logging {
  override def findByHetu(hetu: String)(implicit user: KoskiSession): Option[HenkilötiedotJaOid] = {
    if (!accessChecker.hasAccess(user)) {
      None
    } else {
      // Tänne tullaan vain, jos oppijaa ei löytynyt henkilöpalvelusta (ks CompositeHenkilöRepository)
      findByHetuDontCreate(hetu) match {
        case Right(Some(uusiOppija)) =>
          // Validi oppija lisätään henkilöpalveluun, jolloin samaa oppijaa ei haeta enää uudestaan Virrasta
          henkilöpalvelu.findOrCreate(uusiOppija) match {
            case Right(henkilö) => Some(henkilö.toHenkilötiedotJaOid)
            case Left(error) =>
              logger.error("Virta-oppijan lisäys henkilöpalveluun epäonnistui: " + error)
              None
          }
        case Right(None) => None
        case Left(_) => None
      }
    }
  }

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


