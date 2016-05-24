package fi.oph.tor.virta

import fi.oph.tor.henkilo.Hetu
import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.log.Logging
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.schema.UusiHenkilö

/*
  OppijaRepositoryn toteutus, jota käytetään oppijoiden etsimiseen Virta-järjestelmästä hetulla.
 */
case class VirtaOppijaRepository(v: VirtaClient, henkilöpalvelu: OppijaRepository) extends OppijaRepository with Logging {
  override def findOppijat(query: String) = Hetu.validFormat(query) match {
    case Left(_) =>
      Nil
    case Right(hetu) =>
      try {
        // Tänne tullaan vain, jos oppijaa ei löytynyt henkilöpalvelusta (ks CompositeOppijaRepository)
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
              case Right(oid) => henkilöpalvelu.findByOid(oid)
              case Left(error) =>
                logger.error("Virta-oppijan lisäys henkilöpalveluun epäonnistui: " + error)
                None
            }
        }
        .toList
      } catch {
        case e: Exception =>
          logger.error(e)("Failed to fetch data from Virta")
          Nil
      }
  }

  override def findOrCreate(henkilö: UusiHenkilö) = Left(TorErrorCategory.notImplemented.readOnly("Virta-järjestelmään ei voi lisätä henkilöitä"))

  override def findByOid(oid: String) = None

  override def findByOids(oids: List[String]) = Nil
}
