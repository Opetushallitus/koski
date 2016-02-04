package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus}
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import org.json4s.JValue
import rx.lang.scala.Observable

trait OpiskeluOikeusRepository {
  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[OpiskeluOikeus])]
  def filterOppijat(oppijat: Seq[FullHenkilö])(implicit user: TorUser): Seq[FullHenkilö]
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[OpiskeluOikeus]
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult]
}


sealed trait CreateOrUpdateResult {
  def id: OpiskeluOikeus.Id
  def versionumero: Int
  def diff: JValue
}

case class Created(id: OpiskeluOikeus.Id, versionumero: OpiskeluOikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class Updated(id: OpiskeluOikeus.Id, versionumero: OpiskeluOikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class NotChanged(id: OpiskeluOikeus.Id, versionumero: OpiskeluOikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult