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

  def name: String = this.getClass.getSimpleName.toLowerCase.filterNot(_ == '$')
}

trait VuosiluokkaRaporttiPaivalta extends Raportti {

  def title(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String

  def documentation(oppilaitosOid: String, alku: LocalDate, vuosiluokka: String, loadCompleted: Timestamp): String

  def filename(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String

  def buildRaportti(raportointiDatabase: PerusopetuksenRaportitRepository, oppilaitosOid: Set[Organisaatio.Oid], paiva: LocalDate, vuosiluokka: String): Seq[Product]
}

trait OppilaitosRaporttiRequest {
  def oppilaitosOid: Organisaatio.Oid

  def downloadToken: Option[String]

  def password: String
}

case class AikajaksoRaporttiRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate
) extends OppilaitosRaporttiRequest

case class PerusopetuksenVuosiluokkaRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  paiva: LocalDate,
  vuosiluokka: String
) extends OppilaitosRaporttiRequest

case class OppilaitosRaporttiResponse
(
  rows: Seq[Product],
  sheets: Seq[Sheet],
  workbookSettings: WorkbookSettings,
  filename: String,
  downloadToken: Option[String]
)
