package fi.oph.koski.koodisto

import com.typesafe.config.Config
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

  val secureHttp = VirkailijaHttpClient(serviceConfig, "/koodisto-service", sessionCookieName = "SESSION", true)

  def updateKoodisto(koodisto: Koodisto): Unit = {
    runIO(secureHttp.put(uri"/koodisto-service/rest/codes/save", koodisto)(json4sEncoderOf[Koodisto])(Http.unitDecoder))
  }
  def createKoodisto(koodisto: Koodisto): Unit = {
    try {
      runIO(secureHttp.post(uri"/koodisto-service/rest/codes", koodisto)(json4sEncoderOf[Koodisto])(Http.unitDecoder))
    } catch {
      case HttpStatusException(500, "error.codesgroup.not.found", _, _) =>
        createKoodistoRyhmä(KoodistoRyhmä(koodisto.codesGroupUri.replaceAll("http://", "")))
        createKoodisto(koodisto)
    }
  }

  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = {
    runIO(secureHttp.post(uri"/koodisto-service/rest/codeelement/${koodistoUri}", lisääPuuttuvatPakollisetKäännökset(koodi))(json4sEncoderOf[KoodistoKoodi])(Http.unitDecoder))
    updateKoodi(koodistoUri, koodi)
  }

  def updateKoodi(koodistoUri: String, koodi: KoodistoKoodi) =
    try {
      runUpdateKoodi(modifyKoodiForUpdate(koodi))
    }
    catch {
      case HttpStatusException(500, "error.codeelement.locking", _, _) =>
        runUpdateKoodi(modifyKoodiForUpdate(koodi.copy(version = getKoodistoKoodi(koodi).version)))
    }

  def modifyKoodiForUpdate(koodi: KoodistoKoodi): KoodistoKoodi =
    lisääPuuttuvatPakollisetKäännökset(
      koodi.copy(
        tila = koodi.tila.orElse(Some("LUONNOS")),
        version = koodi.version.orElse(Some(0))
      )
    )

  private def lisääPuuttuvatPakollisetKäännökset(koodi: KoodistoKoodi): KoodistoKoodi =
    koodi.copy(
      metadata = lisääPuuttuvatPakollisetKäännökset(koodi.metadata)
    )

  // Olemassaolevista koodeista, joiden perusteella lokaalit mock:it välillä päivitetään, puuttuu kielikäännöksiä FI tai SV, jotka ovat kuitenkin
  // pakollisia uusia koodeja lisättäessä tai muokattaessa.
  private def lisääPuuttuvatPakollisetKäännökset(metadata: List[KoodistoKoodiMetadata]): List[KoodistoKoodiMetadata] = {
    val metadataFI = metadata.find(_.kieli == Some("FI")).getOrElse(KoodistoKoodiMetadata.apply(nimi = None, lyhytNimi = None, kuvaus = None, kieli = Some("FI")))
    val metadataSV = metadata.find(_.kieli == Some("SV")).getOrElse(KoodistoKoodiMetadata.apply(nimi = None, lyhytNimi = None, kuvaus = None, kieli = Some("SV")))

    val nimiFI = metadataFI.nimi.orElse(metadataSV.nimi)
    val lyhytNimiFI = metadataFI.lyhytNimi.orElse(metadataSV.lyhytNimi)
    val kuvausFI = metadataFI.kuvaus.orElse(metadataSV.kuvaus)

    val nimiSV = metadataSV.nimi.orElse(nimiFI)
    val lyhytNimiSV = metadataSV.lyhytNimi.orElse(lyhytNimiFI)
    val kuvausSV = metadataSV.kuvaus.orElse(kuvausFI)

    metadata.map {
      case md if md.kieli.contains("SV") => metadataSV.copy(
        nimi = nimiSV,
        lyhytNimi = lyhytNimiSV,
        kuvaus = kuvausSV
      )
      case md if md.kieli.contains("FI") => metadataFI.copy(
        nimi = nimiFI,
        lyhytNimi = lyhytNimiFI,
        kuvaus = kuvausFI
      )
      case md => md
    }
  }

  private def runUpdateKoodi(koodi: KoodistoKoodi) =
    runIO(secureHttp.put(uri"/koodisto-service/rest/codeelement/save", koodi)(json4sEncoderOf[KoodistoKoodi])(Http.unitDecoder))

  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä) = {
    runIO(secureHttp.post(uri"/koodisto-service/rest/codesgroup", ryhmä)(json4sEncoderOf[KoodistoRyhmä])(Http.unitDecoder))
  }

  def getKoodistoKoodi(koodi: KoodistoKoodi): KoodistoKoodi =
    runIO(secureHttp.get(uri"/koodisto-service/rest/codeelement/${koodi.koodiUri}/${koodi.versio}")(Http.parseJson[KoodistoKoodi]))
}
