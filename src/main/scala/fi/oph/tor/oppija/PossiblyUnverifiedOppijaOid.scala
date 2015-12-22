package fi.oph.tor.oppija

import fi.oph.tor.schema.Henkilö

trait PossiblyUnverifiedOppijaOid {
  def oppijaOid: Henkilö.Oid
  def verifiedOid: Option[Henkilö.Oid]

  override def toString = oppijaOid
}

case class VerifiedOppijaOid(val oppijaOid: Henkilö.Oid) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = Some(oppijaOid)
}

case class UnverifiedOppijaOid(val oppijaOid: Henkilö.Oid, oppijaRepository: OppijaRepository) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = oppijaRepository.findByOid(oppijaOid).map(oppija => oppijaOid)
}
