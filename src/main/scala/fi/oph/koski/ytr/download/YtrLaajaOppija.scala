package fi.oph.koski.ytr.download

import fi.oph.koski.henkilo.Hetu

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class YtrLaajaOppija(
  oid: Option[String],
  ssn: String,
  firstNames: String,
  lastName: String,
  graduationDate: Option[LocalDate],
  graduationPeriod: Option[String],
  hasCompletedMandatoryExams: Option[Boolean],
  certificateDate: Option[LocalDate],
  examinations: List[YtrExamination]
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
  schoolOid: Option[String],
  schoolName: Option[String],
  totalFee: Option[Int],
  education: Option[Int],
  studentNumber: Option[String],
  exams: List[YtrLaajaExam]
)

case class YtrLaajaExam(
  examId: String,
  grade: Option[String],
  gradePoints: Option[Int],
  aborted: Option[Boolean],
  examFee: Option[Int],
  freeOfCharge: Option[Boolean],
  totalScore: Option[Int],
  examItems: List[YtrExamItem]
)

case class YtrExamItem(
  itemNumber: String,
  finalAssesmentScore: Option[Int]
)
