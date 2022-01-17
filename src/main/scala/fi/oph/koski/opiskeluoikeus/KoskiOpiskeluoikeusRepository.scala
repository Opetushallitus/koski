package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.{HenkilönTunnisteet, OppijaHenkilöWithMasterInfo, PossiblyUnverifiedHenkilöOid}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Session}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import org.json4s.JValue

trait KoskiOpiskeluoikeusRepository {
  def findByOid(oid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, OpiskeluoikeusRow]
  def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[Henkilö.Oid]]
  def createOrUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteComplete: Boolean = false
  )(implicit user: KoskiSpecificSession): Either[HttpStatus, CreateOrUpdateResult]
  def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSpecificSession): List[A]
  def findByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus]
  def findByCurrentUserOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus]
  def findHuollettavaByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus]
  def getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(oppijaOid: String): Seq[Päivämääräväli]
  def getLukionMuidenOpiskeluoikeuksienAlkamisajatIlmanKäyttöoikeustarkistusta(
    oppijaOid: String,
    muutettavanOpiskeluoikeudenOid: Option[String]
  ): Seq[LocalDate]
  def merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(oid: String): HttpStatus
  def suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oid: String): Boolean
}

trait AuxiliaryOpiskeluoikeusRepository {
  def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSpecificSession): List[A]
  def findByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus]
  def findByCurrentUser(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus]
  def findHuollettavaByOppija(tunnisteet: HenkilönTunnisteet)(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus]
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
