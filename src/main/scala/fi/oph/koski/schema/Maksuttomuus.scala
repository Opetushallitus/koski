package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

case class Maksuttomuus(
  alku: LocalDate,
  loppu: Option[LocalDate],
  maksuton: Boolean
) extends Jakso {
  def containsPidennysJakso(pidennys: OikeuttaMaksuttomuuteenPidennetty) = {
    this.contains(pidennys.alku) && this.contains(pidennys.loppu)
  }
}

case class OikeuttaMaksuttomuuteenPidennetty (
  alku: LocalDate,
  loppu: LocalDate
) extends Alkupäivällinen with DateContaining {
  def overlaps(other: OikeuttaMaksuttomuuteenPidennetty): Boolean = {
    !alku.isBefore(other.alku) && !alku.isAfter(other.loppu) || !loppu.isBefore(other.alku) && !loppu.isAfter(other.loppu)
  }

  def contains(d: LocalDate): Boolean = !d.isBefore(alku) && d.isAfter(loppu)
}

@Description("Laajennetun oppivelvollisuuden suoritus")
trait SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta extends PäätasonSuoritus
