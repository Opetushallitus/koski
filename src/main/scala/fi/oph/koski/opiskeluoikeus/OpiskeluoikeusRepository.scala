package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{Opiskeluoikeus, TaydellisetHenkilötiedot}
import fi.oph.koski.koski.QueryFilter
import fi.oph.koski.koskiuser.{KoskiUser, KoskiUser$}
import org.json4s.JValue
import rx.lang.scala.Observable

trait OpiskeluOikeusRepository {
  def query(filters: List[QueryFilter])(implicit user: KoskiUser): Observable[(Oid, List[Opiskeluoikeus])]
  def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: KoskiUser): Seq[TaydellisetHenkilötiedot]
  def findByOppijaOid(oid: String)(implicit user: KoskiUser): Seq[Opiskeluoikeus]
  def findById(id: Int)(implicit user: KoskiUser): Option[(Opiskeluoikeus, String)]
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: KoskiUser): Either[HttpStatus, CreateOrUpdateResult]
}


sealed trait CreateOrUpdateResult {
  def id: Opiskeluoikeus.Id
  def versionumero: Int
  def diff: JValue
}

case class Created(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class Updated(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class NotChanged(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult

