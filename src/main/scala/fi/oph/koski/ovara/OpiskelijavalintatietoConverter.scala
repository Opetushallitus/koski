package fi.oph.koski.ovara

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite, LocalizedString}

class OpiskelijavalintatietoConverter(koodistoViitePalvelu: KoodistoViitePalvelu) {
  def convert(valintatieto: OvaraOpiskelijavalintatieto): Opiskelijavalintatieto =
    Opiskelijavalintatieto(
      hakemukset = valintatieto.hakemukset.map { hakemus =>
        OpiskelijavalintaHakemus(
          hakemusOid = hakemus.hakemusOid,
          haunKohdejoukko = hakemus.haunKohdejoukko.map(parseKoodistokoodiviite),
          hakutapa = hakemus.hakutapa.map(parseKoodistokoodiviite),
          haku = OpiskelijavalintaHaku(
            oid = hakemus.haku.oid,
            nimi = ovaraNimiToLocalizedString(hakemus.haku.nimi)
          ),
          hakutoiveet = hakemus.hakutoiveet.map { hakutoive =>
            OpiskelijavalintaHakutoive(
              hakukohde = toOrganisaatio(hakutoive.hakukohde),
              tarjoaja = hakutoive.tarjoaja.map(toOrganisaatio),
              koulutuksenAlkamiskausi = hakutoive.koulutuksenAlkamiskausiUri.map(parseKoodistokoodiviite),
              koulutuksenAlkamisvuosi = hakutoive.koulutuksenAlkamisvuosi,
              valinnanTila = hakutoive.valinnanTila.map(validateTila("omadatavalinnantila", _)),
              vastaanotonTila = hakutoive.vastaanotonTila.map(validateTila("omadatavastaanotontila", _)),
              ilmoittautumisenTila = hakutoive.ilmoittautumisenTila.map(validateTila("omadatailmoittautumisentila", _)),
              johtaaTutkintoon = hakutoive.johtaaTutkintoon
            )
          }
        )
      }
    )

  private def toOrganisaatio(org: OvaraOrganisaatio): OpiskelijavalintaOrganisaatio =
    OpiskelijavalintaOrganisaatio(oid = org.oid, nimi = ovaraNimiToLocalizedString(org.nimi))

  private def validateTila(koodistoUri: String, ovaraTila: String): Koodistokoodiviite =
    koodistoViitePalvelu.validateRequired(koodistoUri, ovaraTila.toLowerCase.replace("_", ""))

  private def parseKoodistokoodiviite(str: String): Koodistokoodiviite = {
    val withoutVersion = str.split("#").head
    val lastUnderscore = withoutVersion.lastIndexOf('_')
    if (lastUnderscore < 0) {
      throw new IllegalArgumentException(s"Valintatiedoissa palautui tuntematon koodistokoodiviite: $str")
    }
    val koodistoUri = withoutVersion.substring(0, lastUnderscore)
    val koodiarvo = withoutVersion.substring(lastUnderscore + 1)
    Koodistokoodiviite(koodiarvo, koodistoUri)
  }

  private def ovaraNimiToLocalizedString(nimi: OvaraNimi): LocalizedString =
    LocalizedString.sanitize(Map(
      "fi" -> nimi.fi.getOrElse(""),
      "sv" -> nimi.sv.getOrElse(""),
      "en" -> nimi.en.getOrElse("")
    )).getOrElse(Finnish(""))
}
