package fi.oph.koski.virta

import fi.oph.koski.henkilo.HetuBasedHenkilöRepository
import fi.oph.koski.http.{HttpException, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
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
      case NonFatal(e) if isExpectedVirtaFailure(e) =>
        logger.warn(s"Failed to fetch data from Virta: ${e.getMessage}")
        Left(KoskiErrorCategory.unavailable.virta())
      case NonFatal(e) =>
        logger.error(e)("Failed to fetch data from Virta")
        Left(KoskiErrorCategory.unavailable.virta())
    }
  }

  // Virran ajoittaiset yksittäiset virheet (yhteys- tai HTTP-virheet) ovat odotettavissa eikä niistä hälytetä error-tasolla.
  // Muut poikkeukset (esim. bugi omassa koodissa) ovat odottamattomia ja lokitetaan edelleen error-tasolla.
  private def isExpectedVirtaFailure(t: Throwable): Boolean =
    Iterator.iterate(Option(t))(_.flatMap(e => Option(e.getCause)))
      .takeWhile(_.isDefined).flatten
      .exists(_.isInstanceOf[HttpException])

  override def hasAccess(user: KoskiSpecificSession): Boolean = accessChecker.hasAccess(user)
}


