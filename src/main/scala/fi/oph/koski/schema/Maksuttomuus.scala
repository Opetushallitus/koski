package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

case class Maksuttomuus(
  alku: LocalDate,
  loppu: Option[LocalDate],
  maksuton: Boolean
) extends Jakso

case class MaksuttomuuttaPidennetty(
  alku: LocalDate,
  loppu: LocalDate
) extends Alkupäivällinen {
  def overlaps(other: MaksuttomuuttaPidennetty): Boolean = {
    !alku.isBefore(other.alku) && !alku.isAfter(other.loppu) || !loppu.isBefore(other.alku) && !loppu.isAfter(other.loppu)
  }
}

trait SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta extends PäätasonSuoritus
