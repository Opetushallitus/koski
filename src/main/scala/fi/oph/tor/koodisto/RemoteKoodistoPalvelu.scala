package fi.oph.tor.koodisto

import fi.oph.tor.http.Http._
import fi.oph.tor.http.{Http, HttpStatusException}
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging

class RemoteKoodistoPalvelu(virkailijaUrl: String) extends KoodistoPalvelu with Logging {
  val http = Http(virkailijaUrl)

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    runTask(http(uri"/koodisto-service/rest/codeelement/codes/${koodisto.koodistoUri}/${koodisto.versio}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) =>
        val koodit: List[KoodistoKoodi] = Json.read[List[KoodistoKoodi]](text)
        Some(koodisto.koodistoUri match {
          case "ammatillisentutkinnonosanlisatieto" => // Vain tästä koodistosta haetaan kuvaukset (muista ei tarvita tässä vaiheessa)
            koodit.map(koodi => koodi.copy(metadata = getKoodiMetadata(koodi)))
          case _ => koodit
        })
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    runTask(http(uri"/koodisto-service/rest/codes/${koodisto.koodistoUri}/${koodisto.versio}${noCache}")(Http.parseJsonOptional[Koodisto]))
  }

  def getLatestVersion(koodisto: String): Option[KoodistoViite] = {
    val latestKoodisto: Option[KoodistoWithLatestVersion] = runTask(http(uri"/koodisto-service/rest/codes/${koodisto}${noCache}")(Http.parseJsonIgnoreError[KoodistoWithLatestVersion]))
    latestKoodisto.flatMap { latest => Option(latest.latestKoodistoVersio).map(v => KoodistoViite(koodisto, v.versio)) }
  }

  private def noCache = uri"?noCache=${System.currentTimeMillis()}"

  private def getKoodiMetadata(koodi: KoodistoKoodi) = {
    runTask(http(uri"/koodisto-service/rest/codeelement/${koodi.koodiUri}/${koodi.versio}${noCache}")(Http.parseJson[KoodiLisätiedot])).metadata
  }
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)
case class KoodiLisätiedot(metadata: List[KoodistoKoodiMetadata])

