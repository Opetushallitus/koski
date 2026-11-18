package fi.oph.koski.ibis

import java.time.LocalDate

// IBIS-tulostiedoston rivi, eli yksi candidate registration. Kentät ja niiden järjestys ovat IBO:n dokumentin
// "IB data file formats" (v1.0, 2021) muodon "CSV file (normal)" mukaiset, ja ne on varmistettu kevään 2026 tiedostosta.
case class IbisRecord(
  year: Int,
  month: String,
  school: String,
  sessionNumber: String,
  personalCode: String,
  category: String,
  lastName: String,
  firstName: String,
  universityId: String,
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
  name: String,
  level: String,
  grade: String,
  additional: Boolean,
) {
  def session: IbisSession = IbisSession(year, month)
}

object IbisSubject {
  // IBIS merkitsee nämä SUBJECT GRADE -kenttään arvosanan sijaan.
  val noGradeCodes: Map[String, String] = Map("N" -> "no grade issued", "P" -> "pending")
}

case class IbisSession(year: Int, month: String) {
  def koodiarvo: String = s"$year${IbisSession.vuodenaikakirjaimet(month)}"
}

// Tutkintokerrat koodataan kuten ylioppilastutkinnossa: toukokuun tutkintokerta on kevät ja marraskuun syksy.
object IbisSession {
  val vuodenaikakirjaimet: Map[String, String] = Map("MAY" -> "K", "NOVEMBER" -> "S")
  val lokalisointiavaimet: Map[String, String] = Map("MAY" -> "kevät", "NOVEMBER" -> "syksy")
  def validMonth(month: String): Boolean = vuodenaikakirjaimet.contains(month)
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
