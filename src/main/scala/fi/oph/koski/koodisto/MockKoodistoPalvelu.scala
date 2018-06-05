package fi.oph.koski.koodisto
import fi.oph.koski.cache.{Cached, GlobalCacheManager}
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.koodisto.MockKoodistoPalvelu._

private class MockKoodistoPalvelu extends KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    koodistoKooditResourceName(koodisto.koodistoUri).flatMap(JsonResources.readResourceIfExists(_)).map(JsonSerializer.extract[List[KoodistoKoodi]](_, ignoreExtras = true))
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  private def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    koodistoResourceName(koodistoUri).flatMap(JsonResources.readResourceIfExists(_)).map(JsonSerializer.extract[Koodisto](_, ignoreExtras = true))
  }

  def getLatestVersionOptional(koodistoUri: String): Option[KoodistoViite] = getKoodisto(koodistoUri).map { _.koodistoViite }
}



object MockKoodistoPalvelu {
  // this is done to ensure that the cached instance is used everywhere (performance penalties are huge)
  private lazy val palvelu = KoodistoPalvelu.cached(new MockKoodistoPalvelu)(GlobalCacheManager)
  def apply(): KoodistoPalvelu with Cached = palvelu
  protected[koodisto] def koodistoKooditResourceName(koodistoUri: String): Option[String] = Koodistot.koodistot.find(_ == koodistoUri).map(uri => "/mockdata/koodisto/koodit/" + uri + ".json")
  protected[koodisto] def koodistoResourceName(koodistoUri: String): Option[String] = {
    val found: Option[String] = Koodistot.koodistot.find(_ == koodistoUri)
    found.map(uri => "/mockdata/koodisto/koodistot/" + uri + ".json")
  }

  def koodistoKooditFileName(koodistoUri: String): String = "src/main/resources" + koodistoKooditResourceName(koodistoUri).get
  def koodistoFileName(koodistoUri: String): String = "src/main/resources" + koodistoResourceName(koodistoUri).get


  private lazy val kieliOrdering: Ordering[String] = Ordering.by {
    kieli: String =>
      kieli match {
        case "FI" => 1
        case "SV" => 2
        case "EN" => 3
        case _ => 4
      }
  }(Ordering.Int)

  // koodisto-service returns metadata as a list in some (unspecified, possibly changing) order.
  // sort to avoid unnecessary diffs when updating
  def sortKoodistoMetadata(koodisto: Koodisto): Koodisto =
    koodisto.copy(
      metadata = koodisto.metadata.sortBy(_.kieli)(kieliOrdering),
      withinCodes = koodisto.withinCodes.map(_.sortBy(kr => (kr.codesUri, kr.codesVersion))(Ordering.Tuple2(Ordering.String, Ordering.Int)))
    )

  def sortKoodistoKoodiMetadata(koodistoKoodi: KoodistoKoodi): KoodistoKoodi =
    koodistoKoodi.copy(
      metadata = koodistoKoodi.metadata.sortBy(_.kieli)(Ordering.Option(kieliOrdering)),
      withinCodeElements = koodistoKoodi.withinCodeElements.map(_.sortBy(cr => (cr.codeElementUri, cr.codeElementVersion))(Ordering.Tuple2(Ordering.String, Ordering.Int)))
    )
}
