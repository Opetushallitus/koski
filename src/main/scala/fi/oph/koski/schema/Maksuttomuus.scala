package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.scalaschema.annotation.Description

import scala.collection.mutable

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

  def contains(d: LocalDate): Boolean = !d.isBefore(alku) && !d.isAfter(loppu)

  override def toString: String = s"$alku – $loppu"
}

object OikeuttaMaksuttomuuteenPidennetty {
  def maksuttomuusJaksojenYhteenlaskettuPituus(jaksot: Seq[OikeuttaMaksuttomuuteenPidennetty]): Int = {
    val uniikitPäivät = mutable.HashSet.empty[Long]
    jaksot.foreach(
      jakso => {
        var päivä = jakso.alku
        while (päivä.isBefore(jakso.loppu.plusDays(1))) { // plusDays koska maksuttomuuden pidennyksen kesto on loppupäivä-inklusiivinen. 1.10 - 2.10 = 2 päivää.
          uniikitPäivät.add(päivä.toEpochDay)
          päivä = päivä.plusDays(1)
        }
      }
    )
    uniikitPäivät.size
  }
}

@Description("Laajennetun oppivelvollisuuden suoritus")
trait SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta extends PäätasonSuoritus
