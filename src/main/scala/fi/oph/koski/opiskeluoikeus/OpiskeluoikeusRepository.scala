package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.util.PaginationSettings
import org.json4s.JValue
import rx.lang.scala.Observable

trait OpiskeluoikeusRepository extends AuxiliaryOpiskeluoikeusRepository {
  def streamingQuery(filters: List[OpiskeluoikeusQueryFilter], sorting: Option[OpiskeluoikeusSortOrder], pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[(OpiskeluoikeusRow, HenkilöRow)]

  def findById(id: Int)(implicit user: KoskiSession): Option[OpiskeluoikeusRow]
  def delete(id: Int)(implicit user: KoskiSession): HttpStatus
  def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult]
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession): Seq[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
  def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

trait AuxiliaryOpiskeluoikeusRepository {
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession): Seq[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

sealed trait CreateOrUpdateResult {
  def id: Opiskeluoikeus.Id
  def versionumero: Int
  def diff: JValue
}

case class Created(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class Updated(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class NotChanged(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
