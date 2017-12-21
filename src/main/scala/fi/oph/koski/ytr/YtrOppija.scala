package fi.oph.koski.ytr

import java.time.LocalDate

case class YtrOppija(
  ssn: String,
  lastname: String,
  firstnames: String,
  graduationDate: Option[LocalDate], // Toteutunut valmistumispäivä
  graduationPeriod: Option[String], // Toteutunut tutkintokerta
  exams: List[YtrExam],
  graduationSchoolOphOid: Option[String],
  graduationSchoolYtlNumber: Option[Int]
  // TODO: language=fi/sv tutkinnon kieli
  // TODO: hasCompletedMandatoryExams: Boolean
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