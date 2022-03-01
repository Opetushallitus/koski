package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytysRow
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.util.ChainingSyntax._

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

class OppivelvollisuudenKeskeytysService(application: KoskiApplication) extends Logging {
  protected lazy val db = application.valpasOppivelvollisuudenKeskeytysRepository
  protected lazy val rajapäivät = application.valpasRajapäivätService

  def getKeskeytykset(oppijaOids: Seq[String]): Seq[ValpasOppivelvollisuudenKeskeytys] =
    db.getKeskeytykset(oppijaOids)
      .map(toValpasOppivelvollisuudenKeskeytys)

  def create
    (keskeytys: UusiOppivelvollisuudenKeskeytys)
    (implicit session: ValpasSession)
  : Option[ValpasOppivelvollisuudenKeskeytys] = {
    db.setKeskeytys(OppivelvollisuudenKeskeytysRow(
      oppijaOid = keskeytys.oppijaOid,
      alku = keskeytys.alku,
      loppu = keskeytys.loppu,
      tekijäOid = session.oid,
      tekijäOrganisaatioOid = keskeytys.tekijäOrganisaatioOid,
      luotu = LocalDateTime.now(),
    ))
      .tap(_.map(db.addToHistory(session.user.oid)))
      .map(toValpasOppivelvollisuudenKeskeytys)
  }

  def getSuppeatTiedot(uuid: UUID): Option[ValpasOppivelvollisuudenKeskeytys] = {
    getLaajatTiedot(uuid).map(toValpasOppivelvollisuudenKeskeytys)
  }

  def getLaajatTiedot(uuid: UUID): Option[OppivelvollisuudenKeskeytysRow] = {
    db.getKeskeytys(uuid)
  }

  def update
    (keskeytys: OppivelvollisuudenKeskeytyksenMuutos)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    db.updateKeskeytys(keskeytys)
      .tap(db.addToHistory(session.user.oid))
      .map(toValpasOppivelvollisuudenKeskeytys)
  }

  def delete
    (uuid: UUID)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppivelvollisuudenKeskeytys] = {
    db.deleteKeskeytys(uuid)
      .tap(db.addToHistory(session.user.oid))
      .map(toValpasOppivelvollisuudenKeskeytys)
  }

  def getMuutoshistoria(uuid: UUID): Seq[OppivelvollisuudenKeskeytyshistoriaRow] =
    db.getHistory(uuid)

  def toValpasOppivelvollisuudenKeskeytys(row: OppivelvollisuudenKeskeytysRow): ValpasOppivelvollisuudenKeskeytys =
    ValpasOppivelvollisuudenKeskeytys.apply(rajapäivät.tarkastelupäivä)(row)

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }
}

case class ValpasOppivelvollisuudenKeskeytys(
  id: String,
  tekijäOrganisaatioOid: Organisaatio.Oid,
  alku: LocalDate,
  loppu: Option[LocalDate],
  voimassa: Boolean,
  tulevaisuudessa: Boolean,
)

object ValpasOppivelvollisuudenKeskeytys {
  def apply
    (tarkastelupäivä: LocalDate)
    (row: OppivelvollisuudenKeskeytysRow)
  : ValpasOppivelvollisuudenKeskeytys = {
    val tarkastelupäiväAikavälillä = isBetween(tarkastelupäivä) _
    def keskeytysTulevaisuudessa(alku: LocalDate) = isBetween(alku)(tarkastelupäivä.plusDays(1), None)

    ValpasOppivelvollisuudenKeskeytys(
      id = row.uuid.toString,
      tekijäOrganisaatioOid = row.tekijäOrganisaatioOid,
      alku = row.alku,
      loppu = row.loppu,
      voimassa = !row.peruttu && tarkastelupäiväAikavälillä(row.alku, row.loppu),
      tulevaisuudessa = !row.peruttu && keskeytysTulevaisuudessa(row.alku),
    )
  }

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }
}

case class UusiOppivelvollisuudenKeskeytys(
  oppijaOid: String,
  alku: LocalDate,
  loppu: Option[LocalDate], // Jos None --> voimassa toistaiseksi
  tekijäOrganisaatioOid: String,
)
