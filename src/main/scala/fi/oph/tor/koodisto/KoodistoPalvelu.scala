package fi.oph.tor.koodisto

import java.io.File

import com.typesafe.config.Config
import fi.oph.tor.http.Http
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

trait KoodistoPalvelu {
  def getKoodisto(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]]
}

object KoodistoPalvelu {
  def apply(config: Config) = {
    if (config.hasPath("koodisto.url")) {
      new RemoteKoodistoPalvelu(config.getString("koodisto.url"))
    }
    else if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("opintopolku.virkailija.url") + "/koodisto-service")
    } else {
      new MockKoodistoPalvelu
    }
  }
}

class RemoteKoodistoPalvelu(koodistoRoot: String) extends KoodistoPalvelu {
  val http = Http()
  override def getKoodisto(koodisto: KoodistoViittaus) = http.apply(koodistoRoot + "/rest/codeelement/codes/" + koodisto.koodistoUri + "/" + koodisto.versio)(Http.parseJsonOptional[List[KoodistoKoodi]])
}

class MockKoodistoPalvelu extends KoodistoPalvelu {
  override def getKoodisto(koodisto: KoodistoViittaus) = {
    val filename = "src/main/resources/mockdata/koodisto/" + koodisto.koodistoUri + ".json"
    if (new File(filename).exists()) {
      Some(Json.readFile(filename).extract[List[KoodistoKoodi]])
    } else {
      None
    }
  }
}

