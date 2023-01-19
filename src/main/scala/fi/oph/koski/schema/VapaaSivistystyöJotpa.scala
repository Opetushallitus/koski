package fi.oph.koski.schema

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}

import java.time.LocalDate

@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen suoritus")
case class VapaanSivistystyönJotpaKoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstjotpakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstjotpakoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Suorituskokonaisuudet")
  override val osasuoritukset: Option[List[VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus]] = None,
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
) extends VapaanSivistystyönPäätasonSuoritus

@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen tunnistetiedot")
case class VapaanSivistystyönJotpaKoulutus(
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
  opintokokonaisuus: Koodistokoodiviite,
) extends Koulutus with KoulutusmoduuliValinnainenLaajuus

case class VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
  @Title("Jatkuvaan oppimiseen suunnattu vapaan sivistyön koulutuksen osasuoritus")
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus,
  @KoodistoKoodiarvo("vstjotpakoulutuksenosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstjotpakoulutuksenosasuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyöJotpaKoulutuksenArviointi]] = None,
  override val osasuoritukset: Option[List[VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus]] = None,
) extends Suoritus with Vahvistukseton

case class VapaanSivistystyönJotpaKoulutuksenOsasuoritus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusOpintopisteissä]
) extends KoulutusmoduuliValinnainenLaajuus with PaikallinenKoulutusmoduuli with StorablePreference

@Title("Arviointi")
case class VapaanSivistystyöJotpaKoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkovstjotpa")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

@OnlyWhen("../../../suoritukset/0/tyyppi/koodiarvo", "vstjotpakoulutus")
@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön opiskeluoikeusjakso")
case class VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("keskeytynyt")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  @KoodistoKoodiarvo("14")
  @KoodistoKoodiarvo("15")
  override val opintojenRahoitus: Option[Koodistokoodiviite],
) extends VapaanSivistystyönOpiskeluoikeusjakso with KoskiOpiskeluoikeusjakso
