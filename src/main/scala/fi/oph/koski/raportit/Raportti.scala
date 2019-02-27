package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio

trait Raportti {

  val columnSettings: Seq[(String, Column)]
}

trait AikajaksoRaportti extends Raportti {

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[Product]
}
