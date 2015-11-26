package fi.oph.tor.oppija

import fi.oph.tor.schema.Henkilö

trait PossiblyUnverifiedOppijaOid {
  def oppijaOid: Henkilö.Id
  def verifiedOid: Option[Henkilö.Id]
}

case class VerifiedOppijaOid(val oppijaOid: Henkilö.Id) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = Some(oppijaOid)
}

case class UnverifiedOppijaOid(val oppijaOid: Henkilö.Id, oppijaRepository: OppijaRepository) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = oppijaRepository.findByOid(oppijaOid).map(oppija => oppijaOid)
}
