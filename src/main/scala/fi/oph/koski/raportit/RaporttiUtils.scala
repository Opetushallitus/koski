package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.raportointikanta.ROsasuoritusRow

object RaporttiUtils {
  def arvioituAikavälillä(alku: LocalDate, loppu: LocalDate)(row: ROsasuoritusRow): Boolean =
    row.arviointiPäivä.exists(d => arvioituAikavälillä(alku, loppu, d.toLocalDate))

  private def arvioituAikavälillä(alku: LocalDate, loppu: LocalDate, arviointiPäivä: LocalDate) =
    !alku.isAfter(arviointiPäivä) && !loppu.isBefore(arviointiPäivä)
}
