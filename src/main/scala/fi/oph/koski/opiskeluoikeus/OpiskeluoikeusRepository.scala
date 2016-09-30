package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koski.QueryFilter
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilötiedotJaOid, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus, PäätasonSuoritus}
import org.json4s.JValue
import rx.lang.scala.Observable

trait OpiskeluOikeusRepository extends AuxiliaryOpiskeluOikeusRepository {
  def query(filters: List[QueryFilter])(implicit user: KoskiUser): Observable[(Oid, List[OpiskeluOikeusRow])]
  def findById(id: Int)(implicit user: KoskiUser): Option[OpiskeluOikeusRow]
  def delete(id: Int)(implicit user: KoskiUser): HttpStatus
  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiUser): Either[HttpStatus, CreateOrUpdateResult]
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiUser): Seq[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiUser): Seq[Opiskeluoikeus]
  def findByUserOid(oid: String)(implicit user: KoskiUser): Seq[Opiskeluoikeus]
}

trait AuxiliaryOpiskeluOikeusRepository {
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiUser): Seq[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiUser): Seq[Opiskeluoikeus]
}

sealed trait CreateOrUpdateResult {
  def id: Opiskeluoikeus.Id
  def versionumero: Int
  def diff: JValue
}

case class Created(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class Updated(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class NotChanged(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult

