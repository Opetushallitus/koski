package fi.oph.koski.ytr

import java.time.LocalDate

case class YtrOppija(
  ssn: String,
  lastname: String,
  firstnames: String,
  graduationDate: Option[LocalDate], // Toteutunut valmistumispäivä
  graduationPeriod: Option[String], // Toteutunut tutkintokerta
  exams: List[YtrExam],
  certificateSchoolOphOid: Option[String],
  certificateSchoolYtlNumber: Option[Int],
  hasCompletedMandatoryExams: Boolean,
  language: Option[String]
)
case class YtrExam(
  period: String, // Esim 2013S, TODO: tää pitäis palastella ja kielistää
  examId: String,
  examRoleShort: Option[String],
  grade: String,
  points: Option[Int],
  sections: List[YtrSection]
)
case class YtrSection(
 sectionId: String,
 sectionPoints: Int
)
