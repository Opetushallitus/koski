package fi.oph.koski.ytr.download

import fi.oph.koski.henkilo.Hetu

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class YtrLaajaOppija(
  oid: Option[String],
  ssn: String,
  firstNames: String,
  lastName: String,

  // TODO: TOR-1639: Kaisa kysyy Thomakselta, tarvitaanko me tätä johonkin?
  graduationDate: Option[LocalDate],

  // TODO: TOR-1639: Kaisa selvittää, tarvitaanko tätä
  // Ministeriön datalistalla tällainenkin, puuttuu YTL-datasta, tallennetaan päätason suoritukseen, jos päätetään haluta ja saadaan YTL:ltä
  // examinationStartedPeriod: Option[String]

  // TODO: TOR-1639: Kaisa kysyy: Onko tämä sama kuin ministeriön listan "certificate" periodi? Tallennetaan päätason suoritukselle
  graduationPeriod: Option[String],

  hasCompletedMandatoryExams: Option[Boolean],
  certificateDate: Option[LocalDate],

  // TODO: TOR-1639: Tallenna erilliseen kenttään (vanha data ei ole luotettavaa, mutta uusi on)
  certificateSchoolOphOid: Option[String],

  examinations: List[YtrExamination],
) {
  def birthMonth: String =
    Hetu.toBirthday(ssn).map(_.format(DateTimeFormatter.ofPattern("yyyy-MM"))).getOrElse("-")
}

// TODO: TOR-1639: Tallenna indeksi tutkintokokonaisuudesta jokaiseen kokeeseen (tai keksi parempi tapa mallintaa sama 2-tasoiseen tietomalliin)
case class YtrExamination(

  // TODO: TOR-1639: Tallenna suorituskieli-kenttään osasuorituksiin
  language: String,

  // TODO: TOR-1639: Tallenna jokaiseen kokeeseen, koodistoarvona
  examinationType: String,

  // TODO: TOR-1639: Tallenna jokaiseen kokeeseen, koodistoarvona
  examinationState: Option[String],

  examinationPeriods: List[YtrExaminationPeriod]
)

case class YtrExaminationPeriod(
  examinationPeriod: String,

  // TODO: TOR-1639: Tallenna jokaiseen kokeeseen "oppilaitos"-kenttä, johon tämä.
  // - Kaisa selvittää, minkä päivän mukana tallennettava organisaation nimi valitaan? 1.9.xxxx tai 1.2.xxxx tms.?
  schoolOid: Option[String], // kokeen osasuoritukseen oppilaitos-kenttä

  schoolName: Option[String], // Ei tallenneta Koskeen
  totalFee: Option[Int], // Ei tallenneta Koskeen

  // TODO: TOR-1639: Tallenna koodistoarvona koetasolle
  education: Option[Int],

  studentNumber: Option[String], // Ei tallenneta Koskeen
  exams: List[YtrLaajaExam]
)

case class YtrLaajaExam(
  examId: String,
  grade: Option[String],
  gradePoints: Option[Int],

  // TODO: TOR-1639: Tallenna boolean-kenttänä
  aborted: Option[Boolean],
  examFee: Option[Int], // Ei tallenneta Koskeen

  // TODO: TOR-1639: Tallenna boolean-kenttänä
  freeOfCharge: Option[Boolean],
  totalScore: Option[Int], // Ei tallenneta Koskeen
  examItems: List[YtrExamItem] // Ei tallenneta Koskeen
)

// Ei tallenneta Koskeen
case class YtrExamItem(
  itemNumber: String,
  finalAssesmentScore: Option[Int]
)
