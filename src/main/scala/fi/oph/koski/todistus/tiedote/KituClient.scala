package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.Http.{ParameterizedUriWrapper, UriInterpolator}
import fi.oph.koski.http.HttpStatus

object KituClient {
  private val kituServicePrefix = "/kielitutkinnot"

  private[tiedote] def examineeDetailsUri(lähdejärjestelmänId: String): ParameterizedUriWrapper =
    uri"/kielitutkinnot/yhteystiedot/api/opiskeluoikeus/lahdejarjestelman/$lähdejärjestelmänId"

  private[tiedote] def examineeDetailsEndpoint(lähdejärjestelmänId: String): String =
    examineeDetailsUri(lähdejärjestelmänId).uri.path.toString.stripPrefix(kituServicePrefix)

  private[tiedote] def examineeDetailsByOpiskeluoikeusOidUri(opiskeluoikeusOid: String): ParameterizedUriWrapper =
    uri"/kielitutkinnot/yhteystiedot/api/opiskeluoikeus/$opiskeluoikeusOid"

  private[tiedote] def examineeDetailsByOpiskeluoikeusOidEndpoint(opiskeluoikeusOid: String): String =
    examineeDetailsByOpiskeluoikeusOidUri(opiskeluoikeusOid).uri.path.toString.stripPrefix(kituServicePrefix)

  def apply(config: Config): KituClient = {
    if (config.getString("kitu.baseUrl") == "mock") {
      if (Environment.isServerEnvironment(config)) {
        throw new IllegalStateException("MockKituClient ei ole sallittu palvelinympäristössä – aseta kitu.baseUrl konfiguraatioon")
      }
      new MockKituClient
    } else {
      new RemoteKituClient(config)
    }
  }
}

trait KituClient {
  def getExamineeDetails(lähdejärjestelmänId: String): Either[HttpStatus, KituExamineeDetails]
  def getExamineeDetailsByOpiskeluoikeusOid(opiskeluoikeusOid: String): Either[HttpStatus, KituExamineeDetails]
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
