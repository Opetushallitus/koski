package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema.{Opiskeluoikeus, TaydellisetHenkilötiedot}
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import org.json4s.JValue
import rx.lang.scala.Observable

trait OpiskeluOikeusRepository {
  def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[Opiskeluoikeus])]
  def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser): Seq[TaydellisetHenkilötiedot]
  def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[Opiskeluoikeus]
  def findById(id: Int)(implicit user: TorUser): Option[(Opiskeluoikeus, String)]
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult]
}


sealed trait CreateOrUpdateResult {
  def id: Opiskeluoikeus.Id
  def versionumero: Int
  def diff: JValue
}

case class Created(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class Updated(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class NotChanged(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult

