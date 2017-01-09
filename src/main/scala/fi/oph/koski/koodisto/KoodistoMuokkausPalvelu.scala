package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException, VirkailijaHttpClient}
import fi.oph.koski.log.Logging

/** Koodistojen ja koodien lisäyspalvelu **/

object KoodistoMuokkausPalvelu {
  def apply(config: Config) = {
    new KoodistoMuokkausPalvelu(config.getString("opintopolku.virkailija.username"), config.getString("opintopolku.virkailija.password"), config.getString("opintopolku.virkailija.url"))
  }
}

class KoodistoMuokkausPalvelu(username: String, password: String, virkailijaUrl: String) extends Logging {
  import fi.oph.koski.json.Json._
  import fi.oph.koski.json.Json4sHttp4s._

  val secureHttp = VirkailijaHttpClient(username, password, virkailijaUrl, "/koodisto-service")

  def createKoodisto(koodisto: Koodisto): Unit = {
    try {
      runTask(secureHttp.post(uri"/koodisto-service/rest/codes", koodisto)(json4sEncoderOf[Koodisto])(Http.unitDecoder))
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _) =>
        createKoodistoRyhmä(KoodistoRyhmä(koodisto.codesGroupUri.replaceAll("http://", "")))
        createKoodisto(koodisto)
    }
  }

  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    runTask(secureHttp.post(uri"/koodisto-service/rest/codeelement/${koodistoUri}", koodi)(json4sEncoderOf[KoodistoKoodi])(Http.unitDecoder))
  }

  def updateKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    runTask(secureHttp.put(uri"/koodisto-service/rest/codeelement", koodi)(json4sEncoderOf[KoodistoKoodi])(Http.unitDecoder))
  }

  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä) = {
    runTask(secureHttp.post(uri"/koodisto-service/rest/codesgroup", ryhmä)(json4sEncoderOf[KoodistoRyhmä])(Http.unitDecoder))
  }
}
