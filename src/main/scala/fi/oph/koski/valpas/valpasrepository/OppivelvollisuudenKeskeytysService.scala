package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytysRow
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.scalaschema.annotation.EnumValue

import java.time.{LocalDate, LocalDateTime}

class OppivelvollisuudenKeskeytysService(application: KoskiApplication) extends Logging {
  protected lazy val db = application.valpasOppivelvollisuudenKeskeytysRepository
  protected lazy val rajapäivät = application.valpasRajapäivätService

  def getKeskeytykset(oppijaOids: Seq[String]): Seq[ValpasOppivelvollisuudenKeskeytys] =
    db.getKeskeytykset(oppijaOids)
      .map(ValpasOppivelvollisuudenKeskeytys.apply(rajapäivät.tarkastelupäivä))

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
      .map(ValpasOppivelvollisuudenKeskeytys.apply(rajapäivät.tarkastelupäivä))
  }

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }
}

case class ValpasOppivelvollisuudenKeskeytys(
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
