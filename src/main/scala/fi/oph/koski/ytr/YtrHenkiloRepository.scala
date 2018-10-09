package fi.oph.koski.ytr

import fi.oph.koski.henkilo.HetuBasedHenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.UusiHenkilö

import scala.util.control.NonFatal

case class YtrHenkilöRepository(ytr: YtrClient, accessChecker: AccessChecker) extends HetuBasedHenkilöRepository with Logging {
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
