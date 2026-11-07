package fi.oph.koski.sdg

import fi.oph.koski.ovara.Opiskelijavalintatieto
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.scalaschema.annotation.{Description, Title}

@Title("Valintatiedot")
@Description("Oppijan opiskelijavalintatiedot: hakemukset, hakutoiveet sekä valinnan, opiskelupaikan vastaanoton ja ilmoittautumisen tilat.")
case class SdgValintatieto(
  hakemukset: List[SdgHakemus]
)

object SdgValintatieto {
  def from(valintatieto: Option[Opiskelijavalintatieto]): SdgValintatieto =
    SdgValintatieto(
      hakemukset = valintatieto.toList.flatMap(_.hakemukset).map { hakemus =>
        SdgHakemus(
          hakemusOid = hakemus.hakemusOid,
          haunKohdejoukko = hakemus.haunKohdejoukko,
          hakutapa = hakemus.hakutapa,
          haku = SdgHaku(oid = hakemus.haku.oid, nimi = hakemus.haku.nimi),
          hakutoiveet = hakemus.hakutoiveet.map { hakutoive =>
            SdgHakutoive(
              hakukohde = SdgHakutoiveOrganisaatio(hakutoive.hakukohde.oid, hakutoive.hakukohde.nimi),
              tarjoaja = hakutoive.tarjoaja.map(t => SdgHakutoiveOrganisaatio(t.oid, t.nimi)),
              koulutuksenAlkamiskausi = hakutoive.koulutuksenAlkamiskausi,
              koulutuksenAlkamisvuosi = hakutoive.koulutuksenAlkamisvuosi,
              valinnanTila = hakutoive.valinnanTila,
              vastaanotonTila = hakutoive.vastaanotonTila,
              ilmoittautumisenTila = hakutoive.ilmoittautumisenTila,
              johtaaTutkintoon = hakutoive.johtaaTutkintoon,
              nqf = hakutoive.nqf,
              eqf = hakutoive.eqf,
              opintojenLaajuus = hakutoive.opintojenLaajuus.map(l =>
                SdgOpintojenLaajuus(yksikkö = l.yksikkö, laajuus = l.laajuus, laajuusMin = l.laajuusMin, laajuusMax = l.laajuusMax)
              )
            )
          }
        )
      }
    )
}

@Title("Hakemus")
case class SdgHakemus(
  hakemusOid: String,
  @KoodistoUri("haunkohdejoukko")
  haunKohdejoukko: Option[Koodistokoodiviite],
  @KoodistoUri("hakutapa")
  hakutapa: Option[Koodistokoodiviite],
  haku: SdgHaku,
  hakutoiveet: List[SdgHakutoive]
)

@Title("Haku")
case class SdgHaku(
  oid: String,
  nimi: LocalizedString
)

@Title("Hakutoive")
case class SdgHakutoive(
  hakukohde: SdgHakutoiveOrganisaatio,
  tarjoaja: Option[SdgHakutoiveOrganisaatio],
  @KoodistoUri("kausi")
  koulutuksenAlkamiskausi: Option[Koodistokoodiviite],
  @Description("Vuosiluku merkkijonona")
  koulutuksenAlkamisvuosi: Option[String],
  @KoodistoUri("omadatavalinnantila")
  valinnanTila: Option[Koodistokoodiviite],
  @KoodistoUri("omadatavastaanotontila")
  vastaanotonTila: Option[Koodistokoodiviite],
  @KoodistoUri("omadatailmoittautumisentila")
  ilmoittautumisenTila: Option[Koodistokoodiviite],
  johtaaTutkintoon: Option[Boolean],
  @Description("Hakukohteen koulutuksen tasot tutkintojen ja muiden osaamiskokonaisuuksien kansallisessa viitekehyksessä (NQF)")
  @KoodistoUri("nqf")
  nqf: List[Koodistokoodiviite],
  @Description("Hakukohteen koulutuksen tasot eurooppalaisessa tutkintojen viitekehyksessä (EQF)")
  @KoodistoUri("eqf")
  eqf: List[Koodistokoodiviite],
  @Description("Hakukohteen koulutuksen laajuus")
  opintojenLaajuus: Option[SdgOpintojenLaajuus]
)

@Title("Opintojen laajuus")
case class SdgOpintojenLaajuus(
  @KoodistoUri("opintojenlaajuusyksikko")
  yksikkö: Option[Koodistokoodiviite],
  @Description("Laajuus, kun koulutuksella on yksi kiinteä laajuus")
  laajuus: Option[Double],
  @Description("Laajuuden alaraja, kun laajuus on vaihteluväli")
  laajuusMin: Option[Double],
  @Description("Laajuuden yläraja, kun laajuus on vaihteluväli")
  laajuusMax: Option[Double]
)

@Title("Hakutoiveen organisaatio")
case class SdgHakutoiveOrganisaatio(
  oid: String,
  nimi: LocalizedString
)
