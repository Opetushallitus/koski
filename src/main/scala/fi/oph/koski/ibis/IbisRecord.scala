package fi.oph.koski.ibis

import java.time.LocalDate

// IBIS-tulostiedoston rivi, eli yksi ehdokasrekisteröinti. Kentät ja niiden järjestys ovat IBO:n dokumentin
// "IB data file formats" (v1.0, 2021) muodon "CSV file new Language format (numeric subject code)" mukaiset.
// TODO TOR-747: Dokumentti on vanha. Kenttäjärjestys ja arvot on varmistettava ensimmäisestä oikeasta aineistosta.
case class IbisRecord(
  year: Int,
  month: String,
  school: String,
  personalCode: String,
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
  eeTokPoints: Option[Int],
  subjects: List[IbisSubject],
) {
  def session: IbisSession = IbisSession(year, month)
}

case class IbisSubject(
  year: Int,
  month: String,
  code: String,
  level: String,
  grade: String,
  additional: Boolean,
  language: String,
) {
  def session: IbisSession = IbisSession(year, month)
  def isCore: Boolean = IbisSubject.coreLevels.contains(level)
}

object IbisSubject {
  val coreLevels: Set[String] = Set("EE", "TK")
  val subjectLevels: Set[String] = Set("HL", "SL")
  val gradelessGrades: Set[String] = Set("N", "P")
}

case class IbisSession(year: Int, month: String) {
  def koodiarvo: String = s"$year${IbisSession.kuukausikirjaimet(month)}"
}

object IbisSession {
  val kuukausikirjaimet: Map[String, String] = Map("MAY" -> "M", "NOVEMBER" -> "N")
  val lokalisointiavaimet: Map[String, String] = Map("MAY" -> "toukokuu", "NOVEMBER" -> "marraskuu")
  def validMonth(month: String): Boolean = kuukausikirjaimet.contains(month)
}

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
