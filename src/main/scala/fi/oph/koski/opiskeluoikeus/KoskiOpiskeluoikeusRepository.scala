package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.{HenkilönTunnisteet, PossiblyUnverifiedHenkilöOid, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import org.json4s.JValue

trait KoskiOpiskeluoikeusRepository {
  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusRow]
  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, List[Henkilö.Oid]]
  def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean, allowDeleteComplete: Boolean = false)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult]
  def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSession): List[A]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
  def findByCurrentUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

trait AuxiliaryOpiskeluoikeusRepository {
  def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSession): List[A]
  def findByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
  def findByCurrentUser(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
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

  def henkilötiedot: Option[OppijaHenkilöWithMasterInfo]
}

case class Created(id: Opiskeluoikeus.Id, oid: Opiskeluoikeus.Oid, lähdejärjestelmänId: Option[LähdejärjestelmäId], oppija: OppijaHenkilöWithMasterInfo, versionumero: Opiskeluoikeus.Versionumero, diff: JValue, data: JValue) extends CreateOrUpdateResult {
  def changed = true
  def created = true
  override def oppijaOid: Oid = oppija.henkilö.oid
  override def henkilötiedot: Option[OppijaHenkilöWithMasterInfo] = Some(oppija)
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
