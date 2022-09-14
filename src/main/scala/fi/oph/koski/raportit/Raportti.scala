package fi.oph.koski.raportit

import fi.oph.koski.koodisto.KoodistoPalvelu
import fi.oph.koski.localization.LocalizationReader

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.raportit.aikuistenperusopetus.AikuistenPerusopetusRaporttiType
import fi.oph.koski.raportit.perusopetus.PerusopetuksenRaportitRepository
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}

trait Raportti {

  def columnSettings(t: LocalizationReader): Seq[(String, Column)]
}

trait AikajaksoRaportti extends Raportti {

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): String

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadStarted: LocalDateTime, t: LocalizationReader): String

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): String

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): Seq[Product]

  def name: String = this.getClass.getSimpleName.toLowerCase.filterNot(_ == '$')
}

trait VuosiluokkaRaporttiPaivalta extends Raportti {

  def title(etuliite: String, oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String

  def documentation(t: LocalizationReader): String

  def filename(etuliite: String, oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String

  def buildRaportti(raportointiDatabase: PerusopetuksenRaportitRepository, oppilaitosOid: Seq[Organisaatio.Oid], paiva: LocalDate, vuosiluokka: String, t: LocalizationReader): Seq[Product]
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
  loppu: LocalDate,
  lang: String
) extends RaporttiAikajaksoltaRequest

case class PerusopetuksenVuosiluokkaRequest
(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  paiva: LocalDate,
  vuosiluokka: String,
  lang: String
) extends RaporttiRequest

case class OppilaitosRaporttiResponse(
  sheets: Seq[Sheet],
  workbookSettings: WorkbookSettings,
  filename: String,
  downloadToken: Option[String]
)

case class AikajaksoRaporttiAikarajauksellaRequest(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate,
  osasuoritustenAikarajaus: Boolean,
  lang: String
) extends RaporttiAikajaksoltaRequest

case class AikuistenPerusopetusRaporttiRequest(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate,
  osasuoritustenAikarajaus: Boolean,
  raportinTyyppi: AikuistenPerusopetusRaporttiType,
  lang: String
) extends RaporttiAikajaksoltaRequest

case class IBSuoritustiedotRaporttiRequest(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  alku: LocalDate,
  loppu: LocalDate,
  osasuoritustenAikarajaus: Boolean,
  raportinTyyppi: IBSuoritustiedotRaporttiType,
  lang: String
) extends RaporttiAikajaksoltaRequest

case class RaporttiPäivältäRequest(
  oppilaitosOid: Organisaatio.Oid,
  downloadToken: Option[String],
  password: String,
  paiva: LocalDate,
  lang: String
) extends RaporttiRequest

sealed trait RaportinTyyppi {
  val opiskeluoikeudenTyyppi: String
  override def toString: String = this.getClass.getSimpleName.toLowerCase.filterNot(_ == '$')
}

sealed trait AmmatillinenRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo
}

case object AmmatillinenOpiskelijavuositiedot extends AmmatillinenRaportti
case object AmmatillinenOsittainenSuoritustietojenTarkistus extends  AmmatillinenRaportti
case object AmmatillinenTutkintoSuoritustietojenTarkistus extends AmmatillinenRaportti
case object MuuAmmatillinenKoulutus extends AmmatillinenRaportti
case object TOPKSAmmatillinen extends AmmatillinenRaportti

case object PerusopetuksenVuosiluokka extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.perusopetus.koodiarvo
}

case object PerusopetuksenOppijaMääräRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.perusopetus.koodiarvo
}

case object TuvaPerusopetuksenOppijaMääräRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.tuva.koodiarvo
}

case object PerusopetuksenLisäopetuksenOppijaMääräRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus.koodiarvo
}

case object PerusopetukseenValmistavanOpetuksenTarkistus extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus.koodiarvo
}

case object LukionSuoritustietojenTarkistus extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo
}

case object LukioKurssikertyma extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo
}

case object LukioOpintopistekertyma extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo
}

case object LukioDiaIbInternationalOpiskelijamaarat extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo
}

case object LuvaOpiskelijamaarat extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.luva.koodiarvo
}

case object EsiopetuksenOppijaMäärienRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.esiopetus.koodiarvo
}

case object EsiopetuksenRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.esiopetus.koodiarvo
}

case object AikuistenPerusopetusSuoritustietojenTarkistus extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo
}

case object AikuistenPerusopetusOppijaMäärienRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo
}

case object AikuistenPerusopetusKurssikertymänRaportti extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo
}

case object IBSuoritustietojenTarkistus extends RaportinTyyppi {
  val opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo
}
