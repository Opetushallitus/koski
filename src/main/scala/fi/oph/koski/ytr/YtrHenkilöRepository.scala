package fi.oph.koski.ytr

import fi.oph.koski.henkilo.{FindByHetu, Hetu, OpintopolkuHenkilöRepository}
import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.UusiHenkilö

case class YtrHenkilöRepository(ytr: YlioppilasTutkintoRekisteri, henkilöpalvelu: OpintopolkuHenkilöRepository, accessChecker: AccessChecker) extends FindByHetu with Logging {
  override def findByHetu(hetu: String)(implicit user: KoskiSession) = if (!accessChecker.hasAccess(user)) {
    None
  } else {
    try {
      ytr.oppijaByHetu(hetu).flatMap { ytrOppija =>
        val kutsumanimi = ytrOppija.firstnames.split(" ").toList.head
        henkilöpalvelu.findOrCreate(UusiHenkilö(hetu, ytrOppija.firstnames, kutsumanimi, ytrOppija.lastname)) match {
          case Right(henkilö) =>
            Some(henkilö)
          case Left(error) =>
            logger.error("YTR-oppijan lisäys henkilöpalveluun epäonnistui: " + error)
            None
        }
      }.map(_.toHenkilötiedotJaOid)
    } catch {
      case e: Exception =>
        logger.error(e)("Failed to fetch data from YTR")
        None
    }
  }
}
