package fi.oph.koski.ytr

import java.time.LocalDate

case class YtrOppija(
  lastname: String,
  firstnames: String,
  graduationDate: Option[LocalDate],
  exams: List[YtrExam],
  certificateSchoolOphOid: Option[String],
  hasCompletedMandatoryExams: Boolean
) {
  def examPapers: List[String] = exams.flatMap(_.copyOfExamPaper)
}

case class YtrExam(
  period: String,
  examId: String,
  grade: String,
  points: Option[Int],
  copyOfExamPaper: Option[String] = None,
  toCertificate: Option[Boolean] = None
)
