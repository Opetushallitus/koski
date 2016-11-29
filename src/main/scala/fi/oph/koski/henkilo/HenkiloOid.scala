package fi.oph.koski.henkilo

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Loggable
import fi.oph.koski.schema.Henkilö

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
  def verifiedOid: Option[Henkilö.Oid]

  def logString = oppijaOid
}

case class VerifiedHenkilöOid(val oppijaOid: Henkilö.Oid) extends PossiblyUnverifiedHenkilöOid {
  override def verifiedOid = Some(oppijaOid)
}

case class UnverifiedHenkilöOid(val oppijaOid: Henkilö.Oid, oppijaRepository: HenkilöRepository)(implicit user: KoskiSession) extends PossiblyUnverifiedHenkilöOid {
  override lazy val verifiedOid = oppijaRepository.findByOid(oppijaOid).map(oppija => oppijaOid)
}
