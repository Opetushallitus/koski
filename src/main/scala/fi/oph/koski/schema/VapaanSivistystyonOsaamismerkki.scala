package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, ReadOnly}

import java.time.LocalDate

@Title("Vapaan sivistystyön osaamismerkin opiskeluoikeusjakso")
@OnlyWhen("../../../suoritukset/0/tyyppi/koodiarvo", "vstosaamismerkki")
case class VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
) extends VapaanSivistystyönOpiskeluoikeusjakso {
  override def withAlku(alku: LocalDate): VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso =
    this.copy(alku = alku)
}

@Title("Vapaan sivistystyön osaamismerkin suoritus")
case class VapaanSivistystyönOsaamismerkinSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstosaamismerkki")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstosaamismerkki", koodistoUri = "suorituksentyyppi"),
  @Title("Osaamismerkki")
  koulutusmoduuli: VapaanSivistystyönOsaamismerkki,
  arviointi: Option[List[VapaanSivistystyönOsaamismerkinArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
) extends VapaanSivistystyönPäätasonSuoritus with SuostumusPeruttavissaOpiskeluoikeudelta

@Description("Vapaan sivistystyön osaamismerkin tunnistetiedot")
case class VapaanSivistystyönOsaamismerkki(
  @KoodistoUri("osaamismerkit")
  tunniste: Koodistokoodiviite,
) extends KoodistostaLöytyväKoulutusmoduuli

@Title("Arviointi")
case class VapaanSivistystyönOsaamismerkinArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi
