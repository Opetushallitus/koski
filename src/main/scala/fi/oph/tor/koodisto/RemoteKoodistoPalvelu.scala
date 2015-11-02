package fi.oph.tor.koodisto
import fi.oph.tor.http.Http

class RemoteKoodistoPalvelu(koodistoRoot: String) extends KoodistoPalvelu {
  val http = Http()
  override def getKoodistoKoodit(koodisto: KoodistoViittaus) = {
    http.apply(koodistoRoot + "/rest/codeelement/codes/" + koodisto + noCache)(Http.parseJsonOptional[List[KoodistoKoodi]])
  }

  override def getAlakoodit(koodiarvo: String) = {
    http.apply(koodistoRoot + "/rest/json/relaatio/sisaltyy-alakoodit/" + koodiarvo + noCache)(Http.parseJson[List[Alakoodi]])
  }

  private def noCache = "?noCache=" + System.currentTimeMillis()

  override def getKoodisto(koodisto: KoodistoViittaus) = {
    http.apply(koodistoRoot + "/rest/codes/" + koodisto + noCache)(Http.parseJsonOptional[Koodisto])
  }
}
