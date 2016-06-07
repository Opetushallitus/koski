package fi.oph.koski.koodisto
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._

object MockKoodistoPalvelu extends KoodistoPalvelu {
  /*
    Aakkostettu listaus mockatuista koodistoista.

    Uuden koodiston lisäys:

    1) lisää tähän listaan koodiston nimi
    2) aja KoodistoMockDataUpdater -Dconfig.resource=qa.conf, jolloin koodiston sisältö haetaan qa-ympäristöstä paikallisiin json-fileisiin.
    3) kommitui uudet json-fileet. Muutoksia olemassa oleviin fileisiin ei kannattane tässä yhteydessä kommitoida.
    4) aja koski-applikaatio -Dconfig.resource=koskidev.conf -Dkoodisto.create=true, jolloin uusi koodisto kopioituu myös koskidev-ympäristöön.

   */
  val koodistot = List (
    "ammatillisennaytonarvioinnistapaattaneet",
    "ammatillisennaytonarviointikeskusteluunosallistuneet",
    "ammatillisennaytonarviointikohde",
    "ammatillisennaytonsuorituspaikka",
    "ammatillisentutkinnonosanlisatieto",
    "ammatillisentutkinnonsuoritustapa",
    "arviointiasteikkoammatillinent1k3",
    "arviointiasteikkoammatillinenhyvaksyttyhylatty",
    "arviointiasteikkoyleissivistava",
    "jarjestamismuoto",
    "koskioppiaineetyleissivistava",
    "koulutus",
    "kieli",
    "kielivalikoima",
    "kunta",
    "lahdejarjestelma",
    "lasnaolotila",
    "lukionkurssit",
    "maatjavaltiot2",
    "opetusryhma",
    "opintojenlaajuusyksikko",
    "opintojenrahoitus",
    "opintojentavoite",
    "opiskeluoikeudentila",
    "opiskeluoikeudentyyppi",
    "oppiainematematiikka",
    "oppiaineuskonto",
    "oppiaineaidinkielijakirjallisuus",
    "oppilaitosnumero",
    "osaamisala",
    "perusopetuksenoppimaara",
    "perusopetuksensuoritustapa",
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

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    Json.readFileIfExists(koodistoKooditFileName(koodisto.koodistoUri)).map(_.extract[List[KoodistoKoodi]])
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    Json.readFileIfExists(koodistoFileName(koodistoUri)).map(_.extract[Koodisto])
  }

  def getLatestVersion(koodistoUri: String): Option[KoodistoViite] = getKoodisto(koodistoUri).map { _.koodistoViite }

  def koodistoKooditFileName(koodistoUri: String): String = {
    "src/main/resources/mockdata/koodisto/koodit/" + koodistoUri + ".json"
  }

  def koodistoFileName(koodistoUri: String): String = {
    "src/main/resources/mockdata/koodisto/koodistot/" + koodistoUri + ".json"
  }
}