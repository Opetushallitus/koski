package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.{PossiblyUnverifiedOppijaOid}

import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.schema.{Henkilö, FullHenkilö, OpiskeluOikeus}
import fi.oph.tor.toruser.TorUser

trait OpiskeluOikeusRepository {
  def query(filters: List[QueryFilter])(implicit userContext: TorUser): Iterable[(Oid, OpiskeluOikeus)]
  def filterOppijat(oppijat: Seq[FullHenkilö])(implicit userContext: TorUser): Seq[FullHenkilö]
  def findByOppijaOid(oid: String)(implicit userContext: TorUser): Seq[OpiskeluOikeus]
  def find(identifier: OpiskeluOikeusIdentifier)(implicit userContext: TorUser): Option[OpiskeluOikeus]
  def create(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus): Either[HttpStatus, OpiskeluOikeus.Id]
  def update(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus): HttpStatus
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit userContext: TorUser): Either[HttpStatus, CreateOrUpdateResult]
}


sealed trait CreateOrUpdateResult {
  def oid: OpiskeluOikeus.Id
}

case class Created(oid: OpiskeluOikeus.Id) extends CreateOrUpdateResult
case class Updated(oid: OpiskeluOikeus.Id) extends CreateOrUpdateResult