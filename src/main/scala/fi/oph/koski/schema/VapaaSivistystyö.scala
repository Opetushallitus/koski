package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, MultiLineString, OksaUri, Representative, Tooltip}

@Description("Vapaan sivistystyön koulutuksen opiskeluoikeus")
case class VapaanSivistystyönOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  tila: VapaanSivistystyönOpiskeluoikeudenTila,
  lisätiedot: Option[VapaanSivistystyönOpiskeluoikeudenLisätiedot] = None,
  @MaxItems(1)
  suoritukset: List[VapaanSivistystyönPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus = this.copy(oppilaitos = Some(oppilaitos))
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

case class VapaanSivistystyönOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[VapaanSivistystyönOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class VapaanSivistystyönOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiSuppeaOpiskeluoikeusjakso

case class VapaanSivistystyönOpiskeluoikeudenLisätiedot() extends OpiskeluoikeudenLisätiedot

trait VapaanSivistystyönPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Suorituskielellinen with Todistus with Arvioinniton

case class OppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstoppivelvollisillesuunnattukoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstoppivelvollisillesuunnattukoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönPäätasonSuoritus with OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus

@Description("Vapaan sivistystyön oppivelvollisuuskoulutuksen tunnistetiedot")
case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(
  @KoodistoKoodiarvo("999909")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999909", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with Laajuudeton

trait OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus extends Suoritus with Vahvistukseton with Arvioinniton with Välisuoritus with OpintopistelaajuuksienYhteislaskennallinenSuoritus

@Title("Osaamiskokonaisuuden suoritus")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
  @Title("Osaamiskokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus,
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus]],
  @KoodistoKoodiarvo("vstosaamiskokonaisuus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstosaamiskokonaisuus", koodistoUri = "suorituksentyyppi"),
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

@Title("Valinnaisten suuntautumisopintojen suoritus")
case class OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
  @Title("Valinnaiset suuntautumisopinnot")
  koulutusmoduuli: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot = OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(),
  override val osasuoritukset: Option[List[VapaanSivistystyönOpintokokonaisuudenSuoritus]],
  @KoodistoKoodiarvo("vstvalinnainensuuntautuminen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstvalinnainensuuntautuminen", koodistoUri = "suorituksentyyppi")
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

trait OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli {
  @Description("Laajuus lasketaan yhteen opintokokonaisuuksien laajuuksista automaattisesti tietoja siirrettäessä")
  def laajuus: Option[LaajuusOpintopisteissä]
}

@Title("Valinnaiset suuntautumisopinnot")
case class OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(
  @KoodistoUri("vstmuutopinnot")
  @KoodistoKoodiarvo("valinnaisetsuuntautumisopinnot")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("valinnaisetsuuntautumisopinnot", "vstmuutopinnot"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli {
  override def nimi: LocalizedString = LocalizedString.empty
}

@Title("Osaamiskokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
  @KoodistoUri("vstosaamiskokonaisuus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli with KoodistostaLöytyväKoulutusmoduuli

trait VapaanSivistystyönOpintokokonaisuudenSuoritus extends Suoritus with Vahvistukseton

@Title("Opintokokonaisuuden suoritus")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
  @Title("Opintokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstopintokokonaisuus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstopintokokonaisuus", koodistoUri = "suorituksentyyppi")
) extends VapaanSivistystyönOpintokokonaisuudenSuoritus

@Title("Muualla suoritettujen opintojen suoritus")
case class MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
  @Title("Opintokokonaisuus")
  koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstmuuallasuoritetutopinnot")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmuuallasuoritetutopinnot", koodistoUri = "suorituksentyyppi"),
  tunnustettu: MuuallaSuoritetunVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
) extends VapaanSivistystyönOpintokokonaisuudenSuoritus with VSTTunnustettu

@Title("Opintokokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: LaajuusOpintopisteissä
) extends PaikallinenKoulutusmoduuliPakollinenLaajuus

@Title("Muualla suoritetut opinnot")
case class MuuallaSuoritetutVapaanSivistystyönOpinnot(
  @KoodistoUri("vstmuuallasuoritetutopinnot")
  tunniste: Koodistokoodiviite,
  kuvaus: LocalizedString,
  laajuus: LaajuusOpintopisteissä
) extends KoodistostaLöytyväKoulutusmoduuliPakollinenLaajuus

@Title("Arviointi")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  @KoodistoKoodiarvo("Hylätty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

trait VapaanSivistystyönKoulutuksenArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkovst")
  def arvosana: Koodistokoodiviite
  def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo match {
    case "Hylätty" => false
    case _ => true
  }
}

@Description("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
@OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
case class MuuallaSuoritetunVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen(
   @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite.")
   @Tooltip("Kuvaus siitä, miten aikaisemmin hankittu osaaminen on tunnustettu.")
   @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
   @Representative
   @MultiLineString(5)
   selite: LocalizedString,
)

trait VSTTunnustettu {
  def tunnustettu: MuuallaSuoritetunVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
}

case class OppivelvollisilleSuunnatunMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None
) extends VapaanSivistystyönPäätasonSuoritus with Laajuudellinen

@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen tunnistetiedot")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(
 @KoodistoKoodiarvo("999910")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("999910", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String] = None,
 koulutustyyppi: Option[Koodistokoodiviite] = None,
 laajuus: Option[LaajuusOpintoviikoissa] = None
) extends DiaarinumerollinenKoulutus with Tutkinto

trait VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus extends Suoritus with Vahvistukseton

@Title("Maahanmuuttajien kotoutumiskoulutuksen kieliopintojen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
 @Title("Kotoutumiskoulutuksen kieliopinnot")
 koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus,
 @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus")
 @KoodistoUri(koodistoUri = "suorituksentyyppi")
 tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus", koodistoUri = "suorituksentyyppi"),
 override val arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi]]
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

@Title("Kieliopintojen arviointi")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  @KoodistoKoodiarvo("Hylätty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  @KoodistoUri("arviointiasteikkosuullisenkielitaidonkoetaitotaso")
  kuullunYmmärtämisenTaitotaso: Koodistokoodiviite,
  @KoodistoUri("arviointiasteikkosuullisenkielitaidonkoetaitotaso")
  puhumisenTaitotaso: Koodistokoodiviite,
  @KoodistoUri("arviointiasteikkosuullisenkielitaidonkoetaitotaso")
  luetunYmmärtämisenTaitotaso: Koodistokoodiviite,
  @KoodistoUri("arviointiasteikkosuullisenkielitaidonkoetaitotaso")
  kirjoittamisenTaitotaso: Koodistokoodiviite,
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

@Title("Maahanmuuttajien kotoutumiskoulutuksen opinto-ohjauksen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
  @Title("Kotoutumiskoulutuksen ohjaus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus,
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi]] = None
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

@Title("Maahanmuuttajien kotoutumiskoulutuksen työelämä- ja yhteiskuntataitojen opintojen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
  @Title("Kotoutumiskoulutuksen työelämä- ja yhteiskuntaopinnot")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus,
  override val osasuoritukset: Option[List[VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi]] = None
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus


@Title("Maahanmuuttajien kotoutumiskoulutuksen valinnaisten opintojen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
  @Title("Kotoutumiskoulutuksen valinnaiset opinnot")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus,
  override val osasuoritukset: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi]] = None
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

trait OppivelvollisilleSuunnatunVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenKoulutusmoduuli extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli {
  def laajuus: Option[LaajuusOpintoviikoissa]
}

@Title("Maahanmuuttajien kotoutumiskoulutuksen kokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus(
  @KoodistoUri(koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintoviikoissa] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenKoulutusmoduuli with KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida

trait VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenValinnaistenOpintojenOsasuoritus extends Suoritus with Vahvistukseton

@Title("Valinnaisten opintojen osasuoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
  @Title("Valinnaiset opinnot")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus", koodistoUri = "suorituksentyyppi")
) extends VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenValinnaistenOpintojenOsasuoritus

trait VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus extends Suoritus with Vahvistukseton

@Title("Työelämäjakso")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
  @Title("Työelämäjakso")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajaksonsuoritus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajaksonsuoritus", koodistoUri = "suorituksentyyppi")
) extends VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus

@Title("Työelämä- ja yhteiskuntataidot")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
  @Title("Työelämä- ja yhteiskuntataidot")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus", koodistoUri = "suorituksentyyppi")
) extends VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus


@Title("Arviointi")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkovstkoto")
  @KoodistoKoodiarvo("Suoritettu")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Suoritettu", "arviointiasteikkovstkoto"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

@Title("Maahanmuuttajien kotoutumiskoulutuksen osasuoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOpintoviikoissa]
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus
