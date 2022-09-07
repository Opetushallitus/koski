package fi.oph.koski.koodisto
import fi.oph.koski.cache.{Cached, GlobalCacheManager}
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.koodisto.MockKoodistoPalvelu._

private class MockKoodistoPalvelu extends KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): List[KoodistoKoodi] = {
    koodistoKooditResourceName(koodisto.koodistoUri, Some(koodisto.versio))
      .flatMap(JsonResources.readResourceIfExists(_))
      .map(JsonSerializer.extract[List[KoodistoKoodi]](_, ignoreExtras = true))
      .getOrElse(throw new RuntimeException(s"Koodistoa ei löydy: $koodisto"))
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri, Some(koodisto.versio))
  }

  private def getKoodisto(koodistoUri: String, koodistoVersio: Option[Int]): Option[Koodisto] = {
    koodistoResourceName(koodistoUri, koodistoVersio).flatMap(JsonResources.readResourceIfExists(_)).map(JsonSerializer.extract[Koodisto](_, ignoreExtras = true))
  }

  def getLatestVersionOptional(koodistoUri: String): Option[KoodistoViite] = getKoodisto(koodistoUri, None).map { _.koodistoViite }
}



object MockKoodistoPalvelu {
  // this is done to ensure that the cached instance is used everywhere (performance penalties are huge)
  private lazy val palvelu = KoodistoPalvelu.cached(new MockKoodistoPalvelu)(GlobalCacheManager)
  def apply(): KoodistoPalvelu with Cached = palvelu

  protected[koodisto] def koodistoKooditResourceName(koodistoUri: String, koodistoVersio: Option[Int]): Option[String] = {
    findKoodistoResource(koodistoUri, koodistoVersio)
      .map(uri => "/mockdata/koodisto/koodit/" + uri.koodisto + uri.koodistoVersio.map(v => s"_$v").getOrElse("") + ".json")
  }

  protected[koodisto] def koodistoResourceName(koodistoUri: String, koodistoVersio: Option[Int]): Option[String] = {
    findKoodistoResource(koodistoUri, koodistoVersio)
      .map(uri => "/mockdata/koodisto/koodistot/" + uri.koodisto + uri.koodistoVersio.map(v => s"_$v").getOrElse("") + ".json")
  }

  private def findKoodistoResource(koodistoUri: String, koodistoVersio: Option[Int]): Option[KoodistoAsetus] = {
    val found = Koodistot.koodistoAsetukset.filter(_.koodisto == koodistoUri)
    found
      .find(_.koodistoVersio == koodistoVersio)
      .orElse(found.find(_.koodistoVersio.isEmpty))
  }

  def koodistoKooditFileName(koodistoUri: String, koodistoVersio: Option[Int]): String = "src/main/resources" + koodistoKooditResourceName(koodistoUri, koodistoVersio).get
  def koodistoFileName(koodistoUri: String, koodistoVersio: Option[Int]): String = "src/main/resources" + koodistoResourceName(koodistoUri, koodistoVersio).get


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
