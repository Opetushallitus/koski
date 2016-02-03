package fi.oph.tor.api

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._
import org.json4s._

trait OpiskeluOikeusData {
  val defaultHenkilö = toJValue(Map(
    "etunimet" -> "Testi",
    "sukunimi" -> "Toivola",
    "kutsumanimi" -> "Testi",
    "hetu" -> "010101-123N"
  ))

  def makeOppija(henkilö: JValue = defaultHenkilö, opiskeluOikeudet: List[JValue] = List(defaultOpiskeluOikeus)) = toJValue(Map(
    "henkilö" -> henkilö,
    "opiskeluoikeudet" -> opiskeluOikeudet
  ))

  val defaultOpiskeluOikeus: JValue = toJValue(Map(
    "oppilaitos" ->  Map("oid" ->  MockOrganisaatiot.stadinAmmattiopisto),
    "suoritus" ->  Map(
      "koulutusmoduulitoteutus" ->  Map(
        "koulutusmoduuli" ->  Map(
          "tunniste" ->  Map(
            "koodiarvo" ->  "351301",
            "nimi" ->  "Autoalan perustutkinto",
            "koodistoUri" ->  "koulutus"),
          "perusteenDiaarinumero" ->  "39/011/2014")),
      "toimipiste" ->  Map(
        "oid" ->  "1.2.246.562.10.42456023292",
        "nimi" ->  "Stadin ammattiopisto, Lehtikuusentien toimipaikka"
      )
    )))
  private val defaultTutkinnonOsa: OpsTutkinnonosa = OpsTutkinnonosa(KoodistoKoodiViite("100023", "tutkinnonosat"), true, Some(Laajuus(11, KoodistoKoodiViite("6", "opintojenlaajuusyksikko"))), None, None)

  val defaultTutkinnonOsaToteutus: OpsTutkinnonosatoteutus = OpsTutkinnonosatoteutus(defaultTutkinnonOsa, None, None)

  val defaultTutkinnonOsaSuoritus = Suoritus(
    None, defaultTutkinnonOsaToteutus, None, None, None,
    OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka")),
    Some(List(Arviointi(KoodistoKoodiViite("2", "arviointiasteikkoammatillinent1k3"), None))), None, None)
}
