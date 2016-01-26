package fi.oph.tor.koodisto

import fi.oph.tor.http.{HttpStatusException, Http, VirkailijaHttpClient}
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
class RemoteKoodistoPalvelu(username: String, password: String, virkailijaUrl: String) extends KoodistoPalvelu with Logging {
  val virkalijaClient = new VirkailijaHttpClient(username, password, virkailijaUrl, "/koodisto-service")
  val secureHttp = virkalijaClient.httpClient
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

  def createKoodisto(koodisto: Koodisto): Unit = {
    try {
      secureHttp.post("/koodisto-service/rest/codes", koodisto)(json4sEncoderOf[Koodisto], Http.unitDecoder)
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _) =>
        createKoodistoRyhmä(new KoodistoRyhmä(koodisto.codesGroupUri.replaceAll("http://", "")))
        createKoodisto(koodisto)
    }
  }

  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    secureHttp.post("/koodisto-service/rest/codeelement/" + koodistoUri, koodi)(json4sEncoderOf[KoodistoKoodi], Http.unitDecoder)
  }

  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä) = {
    secureHttp.post("/koodisto-service/rest/codesgroup", ryhmä)(json4sEncoderOf[KoodistoRyhmä], Http.unitDecoder)
  }

  def removeKoodistoRyhmä(ryhmä: Int) = {
    try {
      secureHttp.post("/koodisto-service/rest/codesgroup/delete/" + ryhmä, Map("id" -> ryhmä.toString))(json4sEncoderOf[Map[String, String]], Http.unitDecoder)
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _) => // ignore
    }
  }
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)

