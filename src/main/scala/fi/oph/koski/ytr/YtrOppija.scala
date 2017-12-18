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
  examId: String, // TODO: näistä pitäis päätellä nimet. Ehkä koodistoon?
  examNameFi: Option[String], // TODO: nimet poistettu rajapinnasta
  examNameSv: Option[String],
  examNameEn: Option[String],
  examRole: Option[String], // TODO: nykyisin examRoleShort, examRoleLegacy
  grade: String,
  points: Int,
  sections: List[YtrSection]
)
case class YtrSection(
 sectionId: String,
 sectionPoints: Int
)