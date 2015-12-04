package fi.oph.tor.koodisto

import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.vm.sade.utils.slf4j.Logging

class RemoteKoodistoPalvelu(username: String, password: String, virkailijaUrl: String) extends LowLevelKoodistoPalvelu with Logging {
  val virkalijaClient = new VirkailijaHttpClient(username, password, virkailijaUrl, "/koodisto-service")
  val http = virkalijaClient.httpClient

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    http(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codeelement/codes/" + koodisto + noCache))(Http.parseJsonOptional[List[KoodistoKoodi]])
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
    http.post(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codes"), koodisto)
  }


  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    http.post(virkalijaClient.virkailijaUriFromString("/koodisto-service/rest/codeelement/" + koodistoUri), koodi)
  }
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)

