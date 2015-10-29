package fi.oph.tor.koodisto
import fi.oph.tor.http.Http

class RemoteKoodistoPalvelu(koodistoRoot: String) extends KoodistoPalvelu {
  val http = Http()
  override def getKoodisto(koodisto: KoodistoViittaus) = http.apply(koodistoRoot + "/rest/codeelement/codes/" + koodisto.koodistoUri + "/" + koodisto.versio)(Http.parseJsonOptional[List[KoodistoKoodi]])
}
