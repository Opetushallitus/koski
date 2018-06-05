package fi.oph.koski.koodisto

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging

class RemoteKoodistoPalvelu(virkailijaUrl: String) extends KoodistoPalvelu with Logging {
  val http = Http(virkailijaUrl)

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    runTask(http.get(uri"/koodisto-service/rest/codeelement/codes/${koodisto.koodistoUri}/${koodisto.versio}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) =>
        val koodit: List[KoodistoKoodi] = JsonSerializer.parse[List[KoodistoKoodi]](text, ignoreExtras = true)
        Some(koodisto.koodistoUri match {
          case uri if ((Koodistot.koskiKoodistot.contains(uri) || uri == "koulutustyyppi") && uri != "lukionkurssitops2003nuoret") =>
            // Vain koski-koodistoista haetaan kaikki lisätiedot. Optimointina skipataan lukionkurssitops2003nuoret,
            // joka on iso (yli 500 http requestia), ja jolle ei löydy mitään lisätietoja.
            koodit.map(koodi => koodi.withAdditionalInfo(getAdditionalInfo(koodi)))
          case _ =>
            koodit
        })
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    runTask(http.get(uri"/koodisto-service/rest/codes/${koodisto.koodistoUri}/${koodisto.versio}${noCache}")(Http.parseJsonOptional[Koodisto]))
  }

  def getLatestVersionOptional(koodistoUri: String): Option[KoodistoViite] = {
    runTask(http.get(uri"/koodisto-service/rest/codes/${koodistoUri}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.generic", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) =>
        val koodisto = JsonSerializer.parse[KoodistoWithLatestVersion](text, ignoreExtras = true)
        Some(KoodistoViite(koodistoUri, koodisto.latestKoodistoVersio.versio))
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })
  }

  private def noCache = uri"?noCache=${System.currentTimeMillis()}"

  private def getAdditionalInfo(koodi: KoodistoKoodi) = {
    runTask(http.get(uri"/koodisto-service/rest/codeelement/${koodi.koodiUri}/${koodi.versio}${noCache}")(Http.parseJson[CodeAdditionalInfo]))
  }
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)
