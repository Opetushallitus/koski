package fi.oph.koski.henkilo

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Loggable
import fi.oph.koski.schema.{Henkilö, HenkilötiedotJaOid, TäydellisetHenkilötiedot}

object HenkilöOid {
  def isValidHenkilöOid(oid: String) = {
    """^1\.2\.246\.562\.24\.\d{11}$""".r.findFirstIn(oid).isDefined
  }

  def validateHenkilöOid(oid: String): Either[HttpStatus, String] = {
    if (isValidHenkilöOid(oid)) {
      Right(oid)
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: " + oid + ". Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
    }
  }
}

trait PossiblyUnverifiedHenkilöOid extends Loggable {
  def oppijaOid: Henkilö.Oid
  def verified: Option[TäydellisetHenkilötiedot]

  def logString = oppijaOid
}

case class VerifiedHenkilöOid(henkilö: TäydellisetHenkilötiedot) extends PossiblyUnverifiedHenkilöOid {
  def oppijaOid = henkilö.oid
  override def verified = Some(henkilö)
}

case class UnverifiedHenkilöOid(val oppijaOid: Henkilö.Oid, henkilöRepository: HenkilöRepository)(implicit user: KoskiSession) extends PossiblyUnverifiedHenkilöOid {
  override lazy val verified = henkilöRepository.findByOid(oppijaOid)
}
