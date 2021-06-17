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

  def getKeskeytykset(oppijaOids: Seq[String]): Seq[ValpasOppivelvollisuudenKeskeytys] = {
    val tarkastelupäiväAikavälillä = isBetween(rajapäivät.tarkastelupäivä) _
    db.getKeskeytykset(oppijaOids)
      .map(keskeytys => ValpasOppivelvollisuudenKeskeytys(
        alku = keskeytys.alku,
        loppu = keskeytys.loppu,
        voimassa = !keskeytys.peruttu && tarkastelupäiväAikavälillä(keskeytys.alku, keskeytys.loppu)
      ))
  }

  def create
    (keskeytys: UusiOppivelvollisuudenKeskeytys)
    (implicit session: ValpasSession)
  : Unit = {
    db.setKeskeytys(OppivelvollisuudenKeskeytysRow(
      oppijaOid = keskeytys.oppijaOid,
      alku = keskeytys.alku.getOrElse(rajapäivät.tarkastelupäivä),
      loppu = keskeytys.loppu,
      tekijäOid = session.oid,
      tekijäOrganisaatioOid = keskeytys.tekijäOrganisaatioOid,
      luotu = LocalDateTime.now(),
    ))
  }

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }
}

case class ValpasOppivelvollisuudenKeskeytys(
  alku: LocalDate,
  loppu: Option[LocalDate],
  voimassa: Boolean,
)

case class UusiOppivelvollisuudenKeskeytys(
  oppijaOid: String,
  alku: Option[LocalDate], // Jos None --> käytetään tarkastelupäivää
  loppu: Option[LocalDate], // Jos None --> voimassa toistaiseksi
  tekijäOrganisaatioOid: String,
)
