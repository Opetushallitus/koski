package fi.oph.koski.ytr

import java.time.LocalDate

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
)

case class YtrExamination(
  language: String,
  examinationType: String,
  examinationState: Option[String],
  examinationPeriods: List[YtrExaminationPeriod]
)

case class YtrExaminationPeriod(
  examinationPeriod: String,
  schoolOid: String,
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
  aborted: Boolean,
  examFee: Option[Int],
  freeOfCharge: Boolean,
  totalScore: Option[Int],
  examItems: List[YtrExamItem]
)

case class YtrExamItem(
  itemNumber: String,
  finalAssesmentScore: Option[Int]
)
