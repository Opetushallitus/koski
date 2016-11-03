package fi.oph.koski.koodisto
import fi.oph.koski.cache.{Cached, GlobalCacheManager}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koodisto.MockKoodistoPalvelu._

private class MockKoodistoPalvelu extends KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    koodistoKooditResourceName(koodisto.koodistoUri).flatMap(Json.readResourceIfExists(_)).map(_.extract[List[KoodistoKoodi]])
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    koodistoResourceName(koodistoUri).flatMap(Json.readResourceIfExists(_)).map(_.extract[Koodisto])
  }

  def getLatestVersion(koodistoUri: String): Option[KoodistoViite] = getKoodisto(koodistoUri).map { _.koodistoViite }
}



object MockKoodistoPalvelu {
  // this is done to ensure that the cached instance is used everywhere (performance penalties are huge)
  private lazy val palvelu = KoodistoPalvelu.cached(new MockKoodistoPalvelu)(GlobalCacheManager)
  def apply(): KoodistoPalvelu with Cached = palvelu
  protected[koodisto] def koodistoKooditResourceName(koodistoUri: String) = Koodistot.koodistot.find(_ == koodistoUri).map(uri => "/mockdata/koodisto/koodit/" + uri + ".json")
  protected[koodisto] def koodistoResourceName(koodistoUri: String): Option[String] = {
    Koodistot.koodistot.find(_ == koodistoUri).map(uri => "/mockdata/koodisto/koodistot/" + uri + ".json")
  }

  protected[koodisto] def koodistoKooditFileName(koodistoUri: String): String = "src/main/resources" + koodistoKooditResourceName(koodistoUri)
  protected[koodisto] def koodistoFileName(koodistoUri: String): String = "src/main/resources" + koodistoResourceName(koodistoUri)
}