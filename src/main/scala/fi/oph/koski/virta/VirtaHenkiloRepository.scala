package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.henkilo.HetuBasedHenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.UusiHenkilö

import scala.concurrent.duration.DurationInt
import scala.util.control.NonFatal

case class VirtaHenkilöRepository(v: VirtaClient, accessChecker: VirtaAccessChecker)(implicit cacheInvalidator: CacheManager) extends HetuBasedHenkilöRepository with Logging {

  private val cache = KeyValueCache[String, Option[UusiHenkilö]](
    ExpiringCache(getClass.getSimpleName + ".henkilötiedot", 24.hours, 1000),
    fetchFromVirta
  )

  def findByHetuDontCreate(hetu: String): Either[HttpStatus, Option[UusiHenkilö]] = {
    try {
      Right(cache(hetu))
    } catch {
      case NonFatal(e) =>
        logger.error(e)("Failed to fetch data from Virta")
        Left(KoskiErrorCategory.unavailable.virta())
    }
  }

  private def fetchFromVirta(hetu: String): Option[UusiHenkilö] = {
    // Tänne tullaan vain, jos oppijaa ei löytynyt henkilöpalvelusta (ks CompositeHenkilöRepository)
    val hakuehto: VirtaHakuehtoHetu = VirtaHakuehtoHetu(hetu)
    // Oppijan organisaatiot haetaan ensin tällä raskaammalla kyselyllä
    val organisaatiot = v.opintotiedot(hakuehto).toSeq.flatMap(_ \\ "Opiskeluoikeus" \ "Myontaja").map(_.text)
    // Organisaatioiden avulla haetaan henkilötietoja ja valitaan niistä ensimmäinen validi
    val opiskelijaNodes = organisaatiot.flatMap(v.henkilötiedot(hakuehto, _)).flatMap(_ \\ "Opiskelija")
    opiskelijaNodes
      .map { opiskelijaNode => ((opiskelijaNode \ "Sukunimi").text, (opiskelijaNode \ "Etunimet").text) }
      .find { case (sukunimi, etunimet) => !sukunimi.isEmpty && !etunimet.isEmpty }
      .map { case (sukunimi, etunimet) =>
        val kutsumanimi = etunimet.split(" ").toList.headOption
        UusiHenkilö(hetu = hetu, etunimet = etunimet, kutsumanimi = kutsumanimi, sukunimi = sukunimi)
      }
  }

  override def hasAccess(user: KoskiSpecificSession): Boolean = accessChecker.hasAccess(user)
}
