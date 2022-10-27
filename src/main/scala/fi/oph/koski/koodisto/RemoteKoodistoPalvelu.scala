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

  private def getKoodistoKooditOptional(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    runIO(http.get(uri"/koodisto-service/rest/codeelement/codes/${koodisto.koodistoUri}/${koodisto.versio}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) =>
        val koodit: List[KoodistoKoodi] = JsonSerializer.parse[List[KoodistoKoodi]](text, ignoreExtras = true)
        Some(koodisto.koodistoUri match {
          case uri if haetaankoKoodienLisätiedot(uri) =>
            koodit.map(koodi => koodi.withAdditionalInfo(getAdditionalInfo(koodi)))
          case _ =>
            koodit
        })
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })
  }

  private def getKoodistoKooditLisätietoineenOptional(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    runIO(http.get(uri"/koodisto-service/rest/codeelement/codes/withrelations/${koodisto.koodistoUri}/${koodisto.versio}${noCache}") {
      case (404, _, _) => None
      case (500, "error.codes.not.found", _) => None // If codes are not found, the service actually returns 500 with this error text.
      case (200, text, _) => Some(JsonSerializer.parse[List[KoodistoKoodi]](text, ignoreExtras = true))
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })
  }

  private def haetaankoKoodienLisätiedot(koodistoUri: String): Boolean = {
    // Haetaan lisätiedot koski-koodistoista ja muutamasta muusta (koulutustyyppi koska halutaan withinCodeElements,
    // virtaopiskeluoikeuden* koska halutaan kuvaus). Optimointina skipataan lukionkurssitops2003nuoret,
    // joka on iso (yli 500 http requestia), ja jolle ei löydy mitään lisätietoja.
    (Koodistot.koskiKoodistot.contains(koodistoUri) ||
     List("koulutustyyppi", "virtaopiskeluoikeudentila", "virtaopiskeluoikeudentyyppi").contains(koodistoUri)) &&
    (koodistoUri != "lukionkurssitops2003nuoret")
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

  private def getAdditionalInfo(koodi: KoodistoKoodi) = {
    runIO(http.get(uri"/koodisto-service/rest/codeelement/${koodi.koodiUri}/${koodi.versio}${noCache}")(Http.parseJson[CodeAdditionalInfo]))
  }
}

case class KoodistoWithLatestVersion(latestKoodistoVersio: LatestVersion)
case class LatestVersion(versio: Int)
