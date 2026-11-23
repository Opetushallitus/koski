package fi.oph.koski.ibis

import java.time.LocalDate

// IBIS-tulostiedoston rivi, eli yksi candidate registration. Kentät ja niiden järjestys ovat IBO:n dokumentin
// "IB data file formats" (v1.0, 2021) muodon "CSV file new Language format" mukaiset, ja ne on varmistettu kevään 2026 tiedostosta.
case class IbisRecord(
  year: Int,
  month: String,
  school: String,
  iboTunniste: String,
  dateOfBirth: Option[LocalDate],
  gender: String,
  category: String,
  lastName: String,
  firstName: String,
  studentId: String,
  courseRef: String,
  lastChangeDate: Option[LocalDate],
  totalPoints: Option[Int],
  resultCode: String,
  combinedGrade: Option[Int],
  subjects: List[IbisSubject],
) {
  def session: IbisSession = IbisSession(year, month)
}

case class IbisSubject(
  year: Int,
  month: String,
  name: String,
  level: String,
  grade: String,
  additional: Boolean,
  language: String,
) {
  def session: IbisSession = IbisSession(year, month)
}

object IbisSubject {
  // IBIS merkitsee nämä SUBJECT GRADE -kenttään arvosanan sijaan.
  val noGradeCodes: Map[String, String] = Map("N" -> "no grade issued", "P" -> "pending")
}

case class IbisSession(year: Int, month: String)

object IbisCategory {
  val Diploma = "DIPLOMA"
  val Anticipated = "ANTICIPATED"
  val Retake = "RETAKE"
  val Course = "COURSE"
}

object IbisResultCode {
  val DiplomaAwarded = "D"
  val BilingualDiplomaAwarded = "B"
  val FailedDiploma = "F"
  val CareerRelatedProgrammeNotAwarded = "J"
  val CareerRelatedProgrammeAwarded = "K"
  val Pending = "P"
}
