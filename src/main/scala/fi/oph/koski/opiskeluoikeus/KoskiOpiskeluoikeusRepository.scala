package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import org.json4s.JValue

trait KoskiOpiskeluoikeusRepository extends AuxiliaryOpiskeluoikeusRepository {
  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusRow]
  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, List[Henkilö.Oid]]
  def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult]
  def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
  def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

trait AuxiliaryOpiskeluoikeusRepository {
  def filterOppijat(oppijat: List[HenkilötiedotJaOid])(implicit user: KoskiSession): List[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
  def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

sealed trait CreateOrUpdateResult {
  def changed: Boolean
  def created: Boolean

  def id: Opiskeluoikeus.Id
  def oid: Opiskeluoikeus.Oid
  def lähdejärjestelmänId: Option[LähdejärjestelmäId]
  def oppijaOid: Henkilö.Oid
  def versionumero: Int
  def diff: JValue
  def data: JValue

  def henkilötiedot: Option[TäydellisetHenkilötiedotWithMasterInfo]
}

case class Created(id: Opiskeluoikeus.Id, oid: Opiskeluoikeus.Oid, lähdejärjestelmänId: Option[LähdejärjestelmäId], oppija: TäydellisetHenkilötiedotWithMasterInfo, versionumero: Opiskeluoikeus.Versionumero, diff: JValue, data: JValue) extends CreateOrUpdateResult {
  def changed = true
  def created = true
  override def oppijaOid: Oid = oppija.oid
  override def henkilötiedot: Option[TäydellisetHenkilötiedotWithMasterInfo] = Some(oppija)
}
case class Updated(id: Opiskeluoikeus.Id, oid: Opiskeluoikeus.Oid, lähdejärjestelmänId: Option[LähdejärjestelmäId], oppijaOid: Henkilö.Oid, versionumero: Opiskeluoikeus.Versionumero, diff: JValue, data: JValue, old: KoskeenTallennettavaOpiskeluoikeus) extends CreateOrUpdateResult {
  def changed = true
  def created = false
  override def henkilötiedot = None
}
case class NotChanged(id: Opiskeluoikeus.Id, oid: Opiskeluoikeus.Oid, lähdejärjestelmänId: Option[LähdejärjestelmäId], oppijaOid: Henkilö.Oid, versionumero: Opiskeluoikeus.Versionumero, diff: JValue, data: JValue) extends CreateOrUpdateResult {
  def changed = false
  def created = false
  override def henkilötiedot = None
}
