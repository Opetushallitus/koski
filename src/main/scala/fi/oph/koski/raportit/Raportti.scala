package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio

private[raportit] trait Raportti {

  val columnSettings: Seq[(String, Column)]
}

private[raportit] trait AikajaksoRaportti extends Raportti {

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[Product]

  def name: String = this.getClass.getSimpleName.toLowerCase.filterNot(_ == '$')
}

private[raportit] trait VuosiluokkaRaportti extends Raportti {
  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, vuosiluokka: String): String

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, vuosiluokka: String): String

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, vuosiluokka: String): Seq[Product]
}

private[raportit] trait OppilaitosRaporttiRequest {
  def oppilaitosOid: Organisaatio.Oid

  def downloadToken: Option[String]

  def password: String
}

private[raportit] case class AikajaksoRaporttiRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate,
) extends OppilaitosRaporttiRequest

private[raportit] case class VuosiluokkaRaporttiRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate,
  vuosi: String
) extends OppilaitosRaporttiRequest

private[raportit] case class OppilaitosRaporttiResponse
(
  rows: Seq[Product],
  sheets: Seq[Sheet],
  workbookSettings: WorkbookSettings,
  filename: String,
  downloadToken: Option[String]
)
