package fi.oph.koski.ytr

import fi.oph.koski.henkilo.{AuxiliaryHenkilöRepository, Hetu}
import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.schema.UusiHenkilö

case class YtrHenkilöRepository(ytr: YlioppilasTutkintoRekisteri, henkilöpalvelu: HenkilöRepository, accessChecker: AccessChecker) extends AuxiliaryHenkilöRepository with Logging {
  override def findOppijat(query: String)(implicit user: KoskiSession) = if (!accessChecker.hasAccess(user)) {
    Nil
  } else {
    Hetu.validFormat(query) match {
      case Left(_) =>
        Nil
      case Right(hetu) =>
        try {
          ytr.oppijaByHetu(hetu).flatMap { ytrOppija =>
            val kutsumanimi = ytrOppija.firstnames.split(" ").toList.head
            henkilöpalvelu.findOrCreate(UusiHenkilö(hetu, ytrOppija.firstnames, kutsumanimi, ytrOppija.lastname)) match {
              case Right(oid) =>
                henkilöpalvelu.findByOid(oid)
              case Left(error) =>
                logger.error("YTR-oppijan lisäys henkilöpalveluun epäonnistui: " + error)
                None
            }
          }.toList.map(_.toHenkilötiedotJaOid)
        } catch {
          case e: Exception =>
            logger.error(e)("Failed to fetch data from YTR")
            Nil
        }
    }
  }
}
