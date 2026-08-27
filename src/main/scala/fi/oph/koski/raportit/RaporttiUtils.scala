package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.raportointikanta.ROsasuoritusRow
import fi.oph.koski.schema.{DateContaining, Jakso}

object RaporttiUtils {
  private[raportit] def jaksotMerkkijonona[T <: DateContaining](jaksot: Option[Seq[T]]): Option[String] =
    jaksot.map(_.map(_.toString).mkString(", ")).filter(_.nonEmpty)

  private[raportit] def jaksotMerkkijonona[T <: Jakso](jaksot: Option[Seq[T]], rajaus: Jakso): Option[String] =
    jaksotMerkkijonona(jaksot.map(_.filter(_.overlaps(rajaus))))

  def arvioituAikavälillä(alku: LocalDate, loppu: LocalDate)(row: ROsasuoritusRow): Boolean =
    row.arviointiPäivä.exists(d => arvioituAikavälillä(alku, loppu, d.toLocalDate))

  private def arvioituAikavälillä(alku: LocalDate, loppu: LocalDate, arviointiPäivä: LocalDate) =
    !alku.isAfter(arviointiPäivä) && !loppu.isBefore(arviointiPäivä)
}
