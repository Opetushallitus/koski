package fi.oph.koski.ytr.download

import fi.oph.koski.henkilo.Hetu

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class YtrLaajaOppija(
  oid: Option[String],
  ssn: String,
  firstNames: Option[String],
  lastName: Option[String],
  graduationDate: Option[LocalDate],
  // TODO: tallennetaan päätason suoritukselle
  graduationPeriod: Option[String],
  // examinationStartedPeriod: Option[String] // Ei tallenneta koskeen
  hasCompletedMandatoryExams: Option[Boolean],
  certificateDate: Option[LocalDate],
  certificateSchoolOphOid: Option[String],
  examinations: List[YtrExamination],
) {
  def birthMonth: String =
    Hetu.toBirthday(ssn).map(_.format(DateTimeFormatter.ofPattern("yyyy-MM"))).getOrElse("-")
}

case class YtrExamination(

  language: String,

  examinationType: String,

  examinationState: Option[String],

  examinationPeriods: List[YtrExaminationPeriod]
)

case class YtrExaminationPeriod(
  examinationPeriod: String,

  schoolOid: Option[String], // kokeen osasuoritukseen oppilaitos-kenttä

  schoolName: Option[String], // Ei tallenneta Koskeen
  totalFee: Option[Int], // Ei tallenneta Koskeen

  education: Option[Int],

  studentNumber: Option[String], // Ei tallenneta Koskeen
  exams: List[YtrLaajaExam]
)

case class YtrLaajaExam(
  examId: String,
  grade: Option[String],
  gradePoints: Option[Int],

  aborted: Option[Boolean],
  examFee: Option[Int], // Ei tallenneta Koskeen

  freeOfCharge: Option[Boolean],
  totalScore: Option[Int], // Ei tallenneta Koskeen
  examItems: List[YtrExamItem] // Ei tallenneta Koskeen
)

// Ei tallenneta Koskeen
case class YtrExamItem(
  itemNumber: String,
  finalAssesmentScore: Option[Int]
)
