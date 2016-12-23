package fi.oph.koski.virta

import fi.oph.koski.henkilo.{FindByHetu, HenkilöRepository, Hetu, OpintopolkuHenkilöRepository}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.UusiHenkilö

case class VirtaHenkilöRepository(v: VirtaClient, henkilöpalvelu: OpintopolkuHenkilöRepository, accessChecker: VirtaAccessChecker) extends FindByHetu with Logging {
  override def findByHetu(hetu: String)(implicit user: KoskiSession) = {
    if (!accessChecker.hasAccess(user)) {
      None
    } else {
      try {
        // Tänne tullaan vain, jos oppijaa ei löytynyt henkilöpalvelusta (ks CompositeHenkilöRepository)
        val hakuehto: VirtaHakuehtoHetu = VirtaHakuehtoHetu(hetu)
        // Oppijan organisaatiot haetaan ensin tällä raskaammalla kyselyllä
        val organisaatiot = v.opintotiedot(hakuehto).toSeq.flatMap(_ \\ "Opiskeluoikeus" \ "Myontaja").map(_.text)
        // Organisaatioden avulla haetaan henkilötietoja ja valitaan niistä ensimmäinen validi
        val opiskelijaNodes = organisaatiot.flatMap(v.henkilötiedot(hakuehto, _)).flatMap(_ \\ "Opiskelija")
        opiskelijaNodes
          .map { opiskelijaNode => ((opiskelijaNode \ "Sukunimi").text, (opiskelijaNode \ "Etunimet").text) }
          .find { case (sukunimi, etunimet) => !sukunimi.isEmpty && !etunimet.isEmpty }
          .flatMap { case (sukunimi, etunimet) =>
            val kutsumanimi = etunimet.split(" ").toList.head
            // Validi oppija lisätään henkilöpalveluun, jolloin samaa oppijaa ei haeta enää uudestaan Virrasta
            henkilöpalvelu.findOrCreate(UusiHenkilö(hetu, etunimet, kutsumanimi, sukunimi)) match {
              case Right(henkilö) => Some(henkilö)
              case Left(error) =>
                logger.error("Virta-oppijan lisäys henkilöpalveluun epäonnistui: " + error)
                None
            }
          }
          .map(_.toHenkilötiedotJaOid)
      } catch {
        case e: Exception =>
          logger.error(e)("Failed to fetch data from Virta")
          None
      }
    }
  }
}


