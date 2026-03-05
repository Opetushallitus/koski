package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus

object KituClient {
  def apply(): KituClient = new MockKituClient
}

trait KituClient {
  def getExamineeDetails(oppijaOid: String): Either[HttpStatus, KituExamineeDetails]
}

case class KituExamineeDetails(
  preferredLanguage: Option[String],
  postalAddress: Option[KituPostalAddress]
)

case class KituPostalAddress(
  street: Option[String],
  postalCode: Option[String],
  city: Option[String],
  country: Option[String]
)
