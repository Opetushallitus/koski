package fi.oph.tor.koodisto

import fi.oph.tor.http.{Http, HttpStatusException}
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging
class RemoteKoodistoPalvelu(virkailijaUrl: String) extends KoodistoPalvelu with Logging {
  val http = Http(virkailijaUrl)

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    http("/koodisto-service/rest/codeelement/codes/" + koodisto + noCache) {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) => Some(Json.read[List[KoodistoKoodi]](text))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    }.run
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    http("/koodisto-service/rest/codes/" + koodisto + noCache)(Http.parseJsonOptional[Koodisto]).run
  }

  def getLatestVersion(koodisto: String): Option[KoodistoViite] = {
    val latestKoodisto: Option[KoodistoWithLatestVersion] = http("/koodisto-service/rest/codes/" + koodisto + noCache)(Http.parseJsonIgnoreError[KoodistoWithLatestVersion]).run
    latestKoodisto.flatMap { latest => Option(latest.latestKoodistoVersio).map(v => KoodistoViite(koodisto, v.versio)) }
  }

  private def noCache = "?noCache=" + System.currentTimeMillis()
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)

