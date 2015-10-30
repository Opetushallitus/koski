package fi.oph.tor.koodisto
import fi.oph.tor.http.Http
import fi.oph.tor.util.Timed

class RemoteKoodistoPalvelu(koodistoRoot: String) extends KoodistoPalvelu with Timed {
  val http = Http()
  override def getKoodisto(koodisto: KoodistoViittaus) = timed("getKoodisto") {
    http.apply(koodistoRoot + "/rest/codeelement/codes/" + koodisto.koodistoUri + "/" + koodisto.versio + noCache)(Http.parseJsonOptional[List[KoodistoKoodi]])
  }

  override def getAlakoodit(koodiarvo: String) = timed("getAlakoodit") {
    http.apply(koodistoRoot + "/rest/json/relaatio/sisaltyy-alakoodit/" + koodiarvo + noCache)(Http.parseJson[List[Alakoodi]])
  }

  private def noCache = "?noCache=" + System.currentTimeMillis()
}
