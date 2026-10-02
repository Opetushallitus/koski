package fi.oph.koski.ovara

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite, LocalizedString}

class OpiskelijavalintatietoConverter(koodistoViitePalvelu: KoodistoViitePalvelu) {
  def convert(valintatieto: OvaraOpiskelijavalintatieto): Opiskelijavalintatieto =
    Opiskelijavalintatieto(
      hakemukset = valintatieto.hakemukset.map { hakemus =>
        OpiskelijavalintaHakemus(
          hakemusOid = hakemus.hakemusOid,
          haunKohdejoukko = hakemus.haunKohdejoukko.map(validateKoodiUri("haunkohdejoukko", _)),
          hakutapa = hakemus.hakutapa.map(validateKoodiUri("hakutapa", _)),
          haku = OpiskelijavalintaHaku(
            oid = hakemus.haku.oid,
            nimi = ovaraNimiToLocalizedString(hakemus.haku.nimi)
          ),
          hakutoiveet = hakemus.hakutoiveet.map { hakutoive =>
            OpiskelijavalintaHakutoive(
              hakukohde = toOrganisaatio(hakutoive.hakukohde),
              tarjoaja = hakutoive.tarjoaja.map(toOrganisaatio),
              koulutuksenAlkamiskausi = hakutoive.koulutuksenAlkamiskausiUri.map(validateKoodiUri("kausi", _)),
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

  // Ovara palauttaa koodit versioidun koodi-URI:n muodossa, esim. "kausi_s#1"
  private def validateKoodiUri(koodistoUri: String, ovaraKoodiUri: String): Koodistokoodiviite =
    koodistoViitePalvelu.validateRequiredByKoodiUri(koodistoUri, ovaraKoodiUri.split("#").head)

  private def ovaraNimiToLocalizedString(nimi: OvaraNimi): LocalizedString =
    LocalizedString.sanitize(Map(
      "fi" -> nimi.fi.getOrElse(""),
      "sv" -> nimi.sv.getOrElse(""),
      "en" -> nimi.en.getOrElse("")
    )).getOrElse(Finnish(""))
}
