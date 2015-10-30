package fi.oph.tor.oppija

trait PossiblyUnverifiedOppijaOid {
  def oppijaOid: Oppija.Id
  def verifiedOid: Option[Oppija.Id]
}

case class VerifiedOppijaOid(val oppijaOid: Oppija.Id) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = Some(oppijaOid)
}

case class UnverifiedOppijaOid(val oppijaOid: Oppija.Id, oppijaRepository: OppijaRepository) extends PossiblyUnverifiedOppijaOid {
  override def verifiedOid = oppijaRepository.findByOid(oppijaOid).map(oppija => oppijaOid)
}
