package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.http.HttpStatus

object KituClient {
  def apply(config: Config): KituClient = {
    if (config.getString("kitu.baseUrl") == "mock") {
      new MockKituClient
    } else {
      new RemoteKituClient(config)
    }
  }
}

trait KituClient {
  def getExamineeDetails(opiskeluoikeusOid: String): Either[HttpStatus, KituExamineeDetails]
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
