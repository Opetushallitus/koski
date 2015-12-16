package fi.oph.tor.koodisto

import fi.oph.tor.http.{HttpStatusException, Http, VirkailijaHttpClient}
import fi.oph.tor.json.Json
import fi.vm.sade.utils.slf4j.Logging
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
class RemoteKoodistoPalvelu(username: String, password: String, virkailijaUrl: String) extends LowLevelKoodistoPalvelu with Logging {
  val virkalijaClient = new VirkailijaHttpClient(username, password, virkailijaUrl, "/koodisto-service")
  val secureHttp = virkalijaClient.httpClient
  val http = Http()

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codeelement/codes/" + koodisto + noCache)) {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) => Some(Json.read[List[KoodistoKoodi]](text))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    }
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codes/" + koodisto + noCache))(Http.parseJsonOptional[Koodisto])
  }

  def getLatestVersion(koodisto: String): Option[KoodistoViite] = {
    val latestKoodisto: Option[KoodistoWithLatestVersion] = http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codes/" + koodisto + noCache))(Http.parseJsonIgnoreError[KoodistoWithLatestVersion])
    latestKoodisto.flatMap { latest => Option(latest.latestKoodistoVersio).map(v => KoodistoViite(koodisto, v.versio)) }
  }

  private def noCache = "?noCache=" + System.currentTimeMillis()

  def createKoodisto(koodisto: Koodisto): Unit = {
    try {
      secureHttp.post(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codes"), koodisto)(json4sEncoderOf[Koodisto])
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _) =>
        createKoodistoRyhmä(new KoodistoRyhmä(koodisto.codesGroupUri.replaceAll("http://", "")))
        createKoodisto(koodisto)
    }

  }


  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    secureHttp.post(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codeelement/" + koodistoUri), koodi)(json4sEncoderOf[KoodistoKoodi])
  }

  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä) = {
    secureHttp.post(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codesgroup"), ryhmä)(json4sEncoderOf[KoodistoRyhmä])
  }

  def removeKoodistoRyhmä(ryhmä: Int) = {
    try {
      secureHttp.post(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codesgroup/delete/" + ryhmä), Map("id" -> ryhmä.toString))(json4sEncoderOf[Map[String, String]])
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _) => // ignore
    }
  }
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)

