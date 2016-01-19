package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus}
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import rx.lang.scala.Observable

trait OpiskeluOikeusRepository {
  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[OpiskeluOikeus])]
  def filterOppijat(oppijat: Seq[FullHenkilö])(implicit user: TorUser): Seq[FullHenkilö]
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[OpiskeluOikeus]
  def find(identifier: OpiskeluOikeusIdentifier)(implicit user: TorUser): Either[HttpStatus, Option[OpiskeluOikeus]]
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult]
}


sealed trait CreateOrUpdateResult {
  def oid: OpiskeluOikeus.Id
  def versionumero: Int
}

case class Created(oid: OpiskeluOikeus.Id, versionumero: OpiskeluOikeus.Versionumero) extends CreateOrUpdateResult
case class Updated(oid: OpiskeluOikeus.Id, versionumero: OpiskeluOikeus.Versionumero) extends CreateOrUpdateResult