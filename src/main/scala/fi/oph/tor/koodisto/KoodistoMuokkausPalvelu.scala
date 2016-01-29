package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.http.{Http, HttpStatusException, VirkailijaHttpClient}
import fi.oph.tor.log.Logging

/** Koodistojen ja koodien lisäyspalvelu **/

object KoodistoMuokkausPalvelu {
  def apply(config: Config) = {
    new KoodistoMuokkausPalvelu(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("koodisto.virkailija.url"))
  }
}

class KoodistoMuokkausPalvelu(username: String, password: String, virkailijaUrl: String) extends Logging {
  import fi.oph.tor.json.Json._
  import fi.oph.tor.json.Json4sHttp4s._

  val virkalijaClient = new VirkailijaHttpClient(username, password, virkailijaUrl, "/koodisto-service")
  val secureHttp = virkalijaClient.httpClient

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
