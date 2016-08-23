package fi.oph.koski.koodisto
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koodisto.MockKoodistoPalvelu._

private class MockKoodistoPalvelu extends KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    Json.readResourceIfExists(koodistoKooditResourceName(koodisto.koodistoUri)).map(_.extract[List[KoodistoKoodi]])
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    Json.readResourceIfExists(koodistoResourceName(koodistoUri)).map(_.extract[Koodisto])
  }

  def getLatestVersion(koodistoUri: String): Option[KoodistoViite] = getKoodisto(koodistoUri).map { _.koodistoViite }

}

object MockKoodistoPalvelu {
  // this is done to ensure that the cached instance is used everywhere (performance penalties are huge)
  private lazy val palvelu = KoodistoPalvelu.cached(new MockKoodistoPalvelu)
  def apply() = palvelu
  /*
    Aakkostettu listaus mockatuista koodistoista.

    Uuden koodiston lisäys:

    1) lisää tähän listaan koodiston nimi
    2) aja KoodistoMockDataUpdater -Dconfig.resource=qa.conf, jolloin koodiston sisältö haetaan qa-ympäristöstä paikallisiin json-fileisiin.
    3) kommitui uudet json-fileet. Muutoksia olemassa oleviin fileisiin ei kannattane tässä yhteydessä kommitoida.
    4) aja koski-applikaatio -Dconfig.resource=koskidev.conf -Dkoodisto.create=true, jolloin uusi koodisto kopioituu myös koskidev-ympäristöön.

   */
  val koodistot = List (
    "ammatillisenerityisopetuksenperuste",
    "ammatillisennaytonarvioinnistapaattaneet",
    "ammatillisennaytonarviointikeskusteluunosallistuneet",
    "ammatillisennaytonarviointikohde",
    "ammatillisennaytonsuorituspaikka",
    "ammatillisentutkinnonosanlisatieto",
    "ammatillisentutkinnonsuoritustapa",
    "arviointiasteikkoammatillinent1k3",
    "arviointiasteikkoammatillinenhyvaksyttyhylatty",
    "arviointiasteikkoyleissivistava",
    "erityinenkoulutustehtava",
    "jarjestamismuoto",
    "koskioppiaineetyleissivistava",
    "koulutus",
    "kieli",
    "kielivalikoima",
    "koskiopiskeluoikeudentila",
    "kunta",
    "lahdejarjestelma",
    "lasnaolotila",
    "lukionkurssit",
    "lukionoppimaara",
    "maatjavaltiot2",
    "opetusryhma",
    "opintojenlaajuusyksikko",
    "opintojenrahoitus",
    "opintojentavoite",
    "opiskeluoikeudentyyppi",
    "oppiainematematiikka",
    "oppiaineaidinkielijakirjallisuus",
    "oppilaitosnumero",
    "oppilaitostyyppi",
    "osaamisala",
    "perusopetuksenluokkaaste",
    "perusopetuksenoppimaara",
    "perusopetuksensuoritustapa",
    "perusopetuksentoimintaalue",
    "perusopetuksentukimuoto",
    "suorituksentila",
    "suorituksentyyppi",
    "tutkintonimikkeet",
    "tutkinnonosat",
    "virtaarvosana",
    "virtalukukausiilmtila",
    "virtaopiskeluoikeudentila",
    "koskiyoarvosanat"
  )

  protected[koodisto] def koodistoKooditResourceName(koodistoUri: String) = "/mockdata/koodisto/koodit/" + koodistoUri + ".json"
  protected[koodisto] def koodistoResourceName(koodistoUri: String) = "/mockdata/koodisto/koodistot/" + koodistoUri + ".json"

  protected[koodisto] def koodistoKooditFileName(koodistoUri: String): String = "src/main/resources" + koodistoKooditResourceName(koodistoUri)
  protected[koodisto] def koodistoFileName(koodistoUri: String): String = "src/main/resources" + koodistoResourceName(koodistoUri)
}