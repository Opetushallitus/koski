package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}
import fi.oph.koski.schema.annotation.{InfoDescription, InfoLinkTitle, InfoLinkUrl, KoodistoKoodiarvo, KoodistoUri, Tooltip}

import java.time.LocalDate

@Title("Vapaan sivistystyön vapaatavoitteisen koulutuksen opiskeluoikeusjakso")
@OnlyWhen("../../../suoritukset/0/tyyppi/koodiarvo", "vstvapaatavoitteinenkoulutus")
case class VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("keskeytynyt")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
) extends VapaanSivistystyönOpiskeluoikeusjakso {
    override def withAlku(alku: LocalDate): VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
      this.copy(alku = alku)
  }

@Title("Vapaatavoitteisen vapaan sivistystyön koulutuksen suoritus")
case class VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstvapaatavoitteinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstvapaatavoitteinenkoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteinenKoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Suorituskokonaisuudet")
  override val osasuoritukset: Option[List[VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönKoulutuksenPäätasonSuoritus with SuostumusPeruttavissaOpiskeluoikeudelta

@Description("Vapaatavoitteisen vapaan sivistystyön koulutuksen tunnistetiedot")
case class VapaanSivistystyönVapaatavoitteinenKoulutus(
  @KoodistoKoodiarvo("099999")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("099999", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  laajuus: Option[LaajuusOpintopisteissä] = None,
  @KoodistoUri("opintokokonaisuudet")
  @Description("Opintokokonaisuus")
  @Tooltip("Opintokokonaisuus")
  @InfoDescription("opintokokonaisuuden_tarkemmat_tiedot_eperusteissa")
  @InfoLinkTitle("opintokokonaisuudet_eperusteissa")
  @InfoLinkUrl("eperusteet_opintopolku_url")
  opintokokonaisuus: Option[Koodistokoodiviite] = None,
) extends Koulutus with Tutkinto

@Title("Vapaatavoitteisen vapaan sivistystyön koulutuksen osasuorituksen suoritus")
case class VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
  @Title("Vapaatavoitteisen vapaan sivistyön koulutuksen osasuoritus")
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus,
  @KoodistoKoodiarvo("vstvapaatavoitteisenkoulutuksenosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstvapaatavoitteisenkoulutuksenosasuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi]] = None,
  override val osasuoritukset: Option[List[VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus]] = None
) extends Suoritus with Vahvistukseton

@Title("Vapaatavoitteisen vapaan sivistystyön koulutuksen osasuoritus")
case class VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(
  kuvaus: LocalizedString,
  tunniste: PaikallinenKoodi,
  laajuus: LaajuusOpintopisteissä
) extends KoulutusmoduuliPakollinenLaajuusOpintopisteissä with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference

@Title("Arviointi")
case class VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkovstvapaatavoitteinen")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovstvapaatavoitteinen"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi
