package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.common.koodisto.{Koodisto, KoodistoKoodi, KoodistoRyhmä}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.log.Logging

/** Koodistojen ja koodien lisäyspalvelu **/

object KoodistoMuokkausPalvelu {
  def apply(config: Config) = {
    new KoodistoMuokkausPalvelu(ServiceConfig.apply(config, "opintopolku.virkailija"))
  }
}

class KoodistoMuokkausPalvelu(serviceConfig: ServiceConfig) extends Logging {
  import fi.oph.koski.json.Json4sHttp4s._

  val secureHttp = VirkailijaHttpClient(serviceConfig, "/koodisto-service", sessionCookieName = "SESSION")

  def updateKoodisto(koodisto: Koodisto): Unit = {
    runTask(secureHttp.put(uri"/koodisto-service/rest/codes/save", koodisto)(json4sEncoderOf[Koodisto])(Http.unitDecoder))
  }
  def createKoodisto(koodisto: Koodisto): Unit = {
    try {
      runTask(secureHttp.post(uri"/koodisto-service/rest/codes", koodisto)(json4sEncoderOf[Koodisto])(Http.unitDecoder))
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _, _) =>
        createKoodistoRyhmä(KoodistoRyhmä(koodisto.codesGroupUri.replaceAll("http://", "")))
        createKoodisto(koodisto)
    }
  }

  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    runTask(secureHttp.post(uri"/koodisto-service/rest/codeelement/${koodistoUri}", koodi)(json4sEncoderOf[KoodistoKoodi])(Http.unitDecoder))
    updateKoodi(koodistoUri, koodi)
  }

  def updateKoodi(koodistoUri: String, koodi: KoodistoKoodi) =
    try {
      runUpdateKoodi(koodi.copy(
        tila = koodi.tila.orElse(Some("LUONNOS")),
        version = koodi.version.orElse(Some(0))
      ))
    }
    catch {
      case HttpStatusException(500, "error.codeelement.locking", _, _) =>
        runUpdateKoodi(koodi.copy(version = getKoodistoKoodi(koodi).version))
    }

  private def runUpdateKoodi(koodi: KoodistoKoodi) =
    runTask(secureHttp.put(uri"/koodisto-service/rest/codeelement/save", koodi)(json4sEncoderOf[KoodistoKoodi])(Http.unitDecoder))

  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä) = {
    runTask(secureHttp.post(uri"/koodisto-service/rest/codesgroup", ryhmä)(json4sEncoderOf[KoodistoRyhmä])(Http.unitDecoder))
  }

  def getKoodistoKoodi(koodi: KoodistoKoodi): KoodistoKoodi =
    runTask(secureHttp.get(uri"/koodisto-service/rest/codeelement/${koodi.koodiUri}/${koodi.versio}")(Http.parseJson[KoodistoKoodi]))
}
