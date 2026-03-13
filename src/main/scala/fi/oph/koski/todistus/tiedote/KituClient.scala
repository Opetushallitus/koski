package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus

object KituClient {
  def apply(): KituClient = new MockKituClient
}

trait KituClient {
  def getExamineeDetails(oppijaOid: String): Either[HttpStatus, KituExamineeDetails]
}

case class KituExamineeDetails(
  sukunimi: String,
  etunimet: String,
  katuosoite: Option[String],
  postinumero: Option[String],
  postitoimipaikka: Option[String],
  maa: Option[KituKoodiarvo],
  email: Option[String],
  todistuskieli: Option[KituKoodiarvo]
)

case class KituKoodiarvo(
  koodiarvo: String,
  koodistoUri: String
)
