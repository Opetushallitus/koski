package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, Title}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}

import java.time.LocalDate

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
) extends VapaanSivistystyönPäätasonSuoritus with SuostumusPeruttavissaOpiskeluoikeudelta

@Description("Vapaatavoitteisen vapaan sivistystyön koulutuksen tunnistetiedot")
case class VapaanSivistystyönVapaatavoitteinenKoulutus(
@KoodistoKoodiarvo("099999")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("099999", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  laajuus: Option[LaajuusOpintopisteissä] = None
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
) extends KoulutusmoduuliPakollinenLaajuusOpintopisteissä with PaikallinenKoulutusmoduuli with StorablePreference

@Title("Arviointi")
case class VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkovstvapaatavoitteinen")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovstvapaatavoitteinen"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi
