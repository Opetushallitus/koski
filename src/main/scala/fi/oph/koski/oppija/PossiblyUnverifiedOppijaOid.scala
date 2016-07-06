package fi.oph.koski.oppija

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.log.Loggable
import fi.oph.koski.schema.Henkilö

trait PossiblyUnverifiedOppijaOid extends Loggable {
  def oppijaOid: Henkilö.Oid
  def verifiedOid: Option[Henkilö.Oid]

  def logString = oppijaOid
}

case class VerifiedOppijaOid(val oppijaOid: Henkilö.Oid) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = Some(oppijaOid)
}

case class UnverifiedOppijaOid(val oppijaOid: Henkilö.Oid, oppijaRepository: OppijaRepository)(implicit user: KoskiUser) extends PossiblyUnverifiedOppijaOid {
  override lazy val verifiedOid = oppijaRepository.findByOid(oppijaOid).map(oppija => oppijaOid)
}
