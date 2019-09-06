package fi.oph.koski.raportit

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio

trait Raportti {

  val columnSettings: Seq[(String, Column)]
}

trait AikajaksoRaportti extends Raportti {

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: LocalDateTime): String

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[Product]

  def name: String = this.getClass.getSimpleName.toLowerCase.filterNot(_ == '$')
}

trait VuosiluokkaRaporttiPaivalta extends Raportti {

  def title(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String

  def documentation(oppilaitosOid: String, alku: LocalDate, vuosiluokka: String, loadCompleted: LocalDateTime): String

  def filename(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String

  def buildRaportti(raportointiDatabase: PerusopetuksenRaportitRepository, oppilaitosOid: Set[Organisaatio.Oid], paiva: LocalDate, vuosiluokka: String): Seq[Product]
}

trait RaporttiRequest {
  def oppilaitosOid: Organisaatio.Oid
  def downloadToken: Option[String]
  def password: String
}

trait RaporttiAikajaksoltaRequest extends RaporttiRequest {
  def alku: LocalDate
  def loppu: LocalDate
}

case class AikajaksoRaporttiRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate
) extends RaporttiRequest

case class PerusopetuksenVuosiluokkaRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  paiva: LocalDate,
  vuosiluokka: String
) extends RaporttiRequest

case class OppilaitosRaporttiResponse
(
  sheets: Seq[Sheet],
  workbookSettings: WorkbookSettings,
  filename: String,
  downloadToken: Option[String]
)

case class AmmatillinenSuoritusTiedotRequest(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate,
  osasuoritustenAikarajaus: Boolean
) extends RaporttiAikajaksoltaRequest


sealed abstract trait RaportinTyyppi {
  override def toString: String = this.getClass.getSimpleName.toLowerCase.filterNot(_ == '$')
}

case object AmmatillinenOpiskelijavuositiedot extends RaportinTyyppi
case object AmmatillinenOsittainenSuoritustietojenTarkistus extends RaportinTyyppi
case object AmmatillinenTutkintoSuoritustietojenTarkistus extends RaportinTyyppi
case object PerusopetuksenVuosiluokka extends RaportinTyyppi
case object LukionSuoritustietojenTarkistus extends RaportinTyyppi
