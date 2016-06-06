package fi.oph.koski.ytr

import java.time.LocalDate

case class YtrOppija(ssn: String, lastname: String, firstnames: String, graduationDate: Option[LocalDate], graduationPeriod: Option[String], exams: List[YtrExam], graduationSchoolOphOid: Option[String], graduationSchoolYtlNumber: Option[String])
case class YtrExam(period: String, examId: String, examNameFi: String, examNameSv: Option[String], examNameEn: Option[String], examRole: String, grade: String, points: Int, sections: List[YtrSection])
case class YtrSection(sectionId: String, sectionPoints: Int)