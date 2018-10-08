package fi.oph.koski.ytr

import fi.oph.koski.henkilo.{FindByHetu, OpintopolkuHenkilöRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.UusiHenkilö

import scala.util.control.NonFatal

case class YtrHenkilöRepository(ytr: YtrClient, henkilöpalvelu: OpintopolkuHenkilöRepository, accessChecker: AccessChecker) extends FindByHetu with Logging {
  override def findByHetu(hetu: String)(implicit user: KoskiSession) = if (!accessChecker.hasAccess(user)) {
    None
  } else {
    findByHetuDontCreate(hetu) match {
      case Right(Some(uusiOppija)) =>
        // Validi oppija lisätään henkilöpalveluun, jolloin samaa oppijaa ei haeta enää uudestaan YTR:stä
        henkilöpalvelu.findOrCreate(uusiOppija) match {
          case Right(henkilö) => Some(henkilö.toHenkilötiedotJaOid)
          case Left(error) =>
            logger.error("YTR-oppijan lisäys henkilöpalveluun epäonnistui: " + error)
            None
        }
      case Right(None) => None
      case Left(_) => None
    }
  }

  def findByHetuDontCreate(hetu: String): Either[HttpStatus, Option[UusiHenkilö]] = {
    try {
      Right(ytr.oppijaByHetu(hetu).map { ytrOppija =>
        val kutsumanimi = ytrOppija.firstnames.split(" ").toList.head
        UusiHenkilö(hetu, ytrOppija.firstnames, Some(kutsumanimi), ytrOppija.lastname)
      })
    } catch {
      case NonFatal(e) =>
        logger.error(e)("Failed to fetch data from YTR")
        Left(KoskiErrorCategory.unavailable.ytr())
    }
  }

  override def hasAccess(user: KoskiSession): Boolean = accessChecker.hasAccess(user)
}
