package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging

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

  private def isBetween(date: LocalDate)(start: LocalDate, end: Option[LocalDate]): Boolean = {
    date.compareTo(start) >= 0 && end.forall(date.compareTo(_) <= 0)
  }
}

case class ValpasOppivelvollisuudenKeskeytys(
  alku: LocalDate,
  loppu: Option[LocalDate],
  voimassa: Boolean,
)
