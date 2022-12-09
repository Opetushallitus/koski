package fi.oph.koski.koodisto

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging

class RemoteKoodistoPalvelu(virkailijaUrl: String) extends KoodistoPalvelu with Logging {
  private val http = Http(virkailijaUrl, "koodisto")

  def getKoodistoKoodit(koodisto: KoodistoViite): List[KoodistoKoodi] = {
    getKoodistoKooditLisätietoineenOptional(koodisto).getOrElse(throw new RuntimeException(s"Koodistoa ei löydy: $koodisto"))
  }

  private def getKoodistoKooditLisätietoineenOptional(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    runIO(http.get(uri"/koodisto-service/rest/codeelement/codes/withrelations/${koodisto.koodistoUri}/${koodisto.versio}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) => Some(JsonSerializer.parse[List[KoodistoKoodi]](text, ignoreExtras = true))
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    runIO(http.get(uri"/koodisto-service/rest/codes/${koodisto.koodistoUri}/${koodisto.versio}${noCache}")(Http.parseJsonOptional[Koodisto]))
  }

  def getLatestVersionOptional(koodistoUri: String): Option[KoodistoViite] = {
    runIO(http.get(uri"/koodisto-service/rest/codes/${koodistoUri}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.generic", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) =>
        val koodisto = JsonSerializer.parse[KoodistoWithLatestVersion](text, ignoreExtras = true)
        Some(KoodistoViite(koodistoUri, koodisto.latestKoodistoVersio.versio))
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })
  }

  private def noCache = uri"?noCache=${System.currentTimeMillis()}"
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)
