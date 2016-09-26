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
  /*
    Aakkostettu listaus mockatuista koodistoista.

    Uuden koodiston lisäys:

    1) Lisää tähän listaan koodiston nimi
    2a) Olemassa oleva koodisto QA-ympäristöstä: Aja KoodistoMockDataUpdater -Dconfig.resource=qa.conf, jolloin koodiston sisältö haetaan qa-ympäristöstä paikallisiin json-fileisiin.
    2b) Uusi Koski-spesifinen koodisto: Tee käsin koodistofileet src/main/resources/koodisto
    3) Kommitoi uudet json-fileet. Muutoksia olemassa oleviin fileisiin ei kannattane tässä yhteydessä kommitoida.
    4) Aja koski-applikaatio -Dconfig.resource=koskidev.conf -Dkoodisto.create=true, jolloin uusi koodisto kopioituu myös koskidev-ympäristöön.

   */
  val koodistot = List (
    "aineryhmaib",
    "ammatillisenerityisopetuksenperuste",
    "ammatillisennaytonarvioinnistapaattaneet",
    "ammatillisennaytonarviointikeskusteluunosallistuneet",
    "ammatillisennaytonarviointikohde",
    "ammatillisennaytonsuorituspaikka",
    "ammatillisentutkinnonosanlisatieto",
    "ammatillisentutkinnonsuoritustapa",
    "arviointiasteikkoammatillinenhyvaksyttyhylatty",
    "arviointiasteikkoammatillinent1k3",
    "arviointiasteikkocorerequirementsib",
    "arviointiasteikkoib",
    "arviointiasteikkolisapisteetib",
    "arviointiasteikkoyleissivistava",
    "effortasteikkoib",
    "erityinenkoulutustehtava",
    "jarjestamismuoto",
    "kieli",
    "kielivalikoima",
    "koskiopiskeluoikeudentila",
    "koskioppiaineetyleissivistava",
    "koskiyoarvosanat",
    "koulutus",
    "kunta",
    "lahdejarjestelma",
    "lasnaolotila",
    "lukionkurssintyyppi",
    "lukionkurssit",
    "lukionoppimaara",
    "lukionoppimaaransuorituksenlisatieto",
    "maatjavaltiot2",
    "opetusryhma",
    "opintojenlaajuusyksikko",
    "opintojenrahoitus",
    "opintojentavoite",
    "opiskeluoikeudentyyppi",
    "oppiaineaidinkielijakirjallisuus",
    "oppiaineentasoib",
    "oppiaineetib",
    "oppiainematematiikka",
    "oppilaitosnumero",
    "oppilaitostyyppi",
    "osaamisala",
    "perusopetuksenluokkaaste",
    "perusopetuksenoppimaara",
    "perusopetuksenoppimaaransuorituksenliitetieto",
    "perusopetuksensuoritustapa",
    "perusopetuksentoimintaalue",
    "perusopetuksentukimuoto",
    "suorituksentila",
    "suorituksentyyppi",
    "tutkinnonosat",
    "tutkintonimikkeet",
    "virtaarvosana",
    "virtalukukausiilmtila",
    "virtaopiskeluoikeudentila"
  )

  protected[koodisto] def koodistoKooditResourceName(koodistoUri: String) = koodistot.find(_ == koodistoUri).map(uri => "/mockdata/koodisto/koodit/" + uri + ".json")
  protected[koodisto] def koodistoResourceName(koodistoUri: String): Option[String] = {
    koodistot.find(_ == koodistoUri).map(uri => "/mockdata/koodisto/koodistot/" + uri + ".json")
  }

  protected[koodisto] def koodistoKooditFileName(koodistoUri: String): String = "src/main/resources" + koodistoKooditResourceName(koodistoUri)
  protected[koodisto] def koodistoFileName(koodistoUri: String): String = "src/main/resources" + koodistoResourceName(koodistoUri)
}