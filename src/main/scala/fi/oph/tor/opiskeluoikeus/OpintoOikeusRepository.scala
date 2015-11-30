package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.{PossiblyUnverifiedOppijaOid}
import fi.oph.tor.user.UserContext
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus}
trait OpiskeluOikeusRepository {
  def filterOppijat(oppijat: Seq[FullHenkilö])(implicit userContext: UserContext): Seq[FullHenkilö]
  def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpiskeluOikeus]
  def find(identifier: OpiskeluOikeusIdentifier)(implicit userContext: UserContext): Option[OpiskeluOikeus]
  def create(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus): Either[HttpStatus, OpiskeluOikeus.Id]
  def update(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus): HttpStatus
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit userContext: UserContext): Either[HttpStatus, OpiskeluOikeus.Id]
}