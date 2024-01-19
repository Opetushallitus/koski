package fi.oph.koski.schema

import fi.oph.koski.schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, NotWhen, OnlyWhen, Title}

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

trait VapaanSivistystyönOpiskeluoikeusjakso extends Opiskeluoikeusjakso {
  def alku: LocalDate

  @KoodistoUri("koskiopiskeluoikeudentila")
  def tila: Koodistokoodiviite

  def opiskeluoikeusPäättynyt: Boolean = {
    OpiskeluoikeudenPäättymistila.koski(tila.koodiarvo)
  }
}

case class VapaanSivistystyönOpiskeluoikeudenLisätiedot(
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot with MaksuttomuusTieto

trait VapaanSivistystyönPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen {
  def koulutusmoduuli: Koulutusmoduuli
}

trait VapaanSivistystyönKoulutuksenPäätasonSuoritus extends VapaanSivistystyönPäätasonSuoritus with Suorituskielellinen with Todistus with Arvioinniton {
  @Title("Koulutus")
  def koulutusmoduuli: Koulutusmoduuli
}

@Title("Oppivelvollisille suunnatun vapaan sivistystyön opiskeluoikeusjakso")
@OnlyWhen("../../../suoritukset/0/tyyppi/koodiarvo", "vstoppivelvollisillesuunnattukoulutus")
@OnlyWhen("../../../suoritukset/0/tyyppi/koodiarvo", "vstmaahanmuuttajienkotoutumiskoulutus")
@OnlyWhen("../../../suoritukset/0/tyyppi/koodiarvo", "vstlukutaitokoulutus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("katsotaaneronneeksi")
  @KoodistoKoodiarvo("valmistunut")
  @KoodistoKoodiarvo("mitatoity")
  tila: Koodistokoodiviite,
) extends VapaanSivistystyönOpiskeluoikeusjakso

case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
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
) extends VapaanSivistystyönKoulutuksenPäätasonSuoritus
    with OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus[LaajuusOpintopisteissä]
    with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

@Description("Vapaan sivistystyön oppivelvollisuuskoulutuksen tunnistetiedot")
case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(
  @KoodistoKoodiarvo("999909")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999909", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with Laajuudeton

trait OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
  extends Suoritus
    with Vahvistukseton
    with Arvioinniton
    with Välisuoritus
    with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä]

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

trait OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli
  extends KoulutusmoduuliValinnainenLaajuus
    with KoodistostaLöytyväKoulutusmoduuli
    with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä] {
  @Description("Laajuus lasketaan yhteen opintokokonaisuuksien laajuuksista automaattisesti tietoja siirrettäessä")
  def laajuus: Option[LaajuusOpintopisteissä]
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
}

@Title("Valinnaiset suuntautumisopinnot")
case class OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(
  @KoodistoUri("vstmuutopinnot")
  @KoodistoKoodiarvo("valinnaisetsuuntautumisopinnot")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("valinnaisetsuuntautumisopinnot", "vstmuutopinnot"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli


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
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstopintokokonaisuus", koodistoUri = "suorituksentyyppi"),
  tunnustettu: Option[VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen] = None
) extends VapaanSivistystyönOpintokokonaisuudenSuoritus with VSTTunnustettu

@Title("Muualla suoritettujen opintojen suoritus")
case class MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
  @Title("Opintokokonaisuus")
  koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstmuuallasuoritetutopinnot")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmuuallasuoritetutopinnot", koodistoUri = "suorituksentyyppi"),
  tunnustettu: Option[VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen] = None
) extends VapaanSivistystyönOpintokokonaisuudenSuoritus with VSTTunnustettu

@Title("Opintokokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: LaajuusOpintopisteissä
) extends PaikallinenKoulutusmoduuliPakollinenLaajuus with StorablePreference

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
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

trait VapaanSivistystyönKoulutuksenArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkovst")
  def arvosana: Koodistokoodiviite
  def arvioitsijat = None
  def hyväksytty = VapaanSivistystyönKoulutuksenArviointi.hyväksytty(arvosana)
}
object VapaanSivistystyönKoulutuksenArviointi {
  def hyväksytty(arvosana: Koodistokoodiviite) = arvosana.koodiarvo == "Hyväksytty" ||
    // Vapaatavoitteisessa ja Jotpassa kaikki omista koodistoista tulevat arvosanat katsotaan hyväksytyiksi
    arvosana.koodistoUri == "arviointiasteikkovstvapaatavoitteinen" ||
    arvosana.koodistoUri == "arviointiasteikkojotpa"
}

@Description("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
@OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
case class VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen(
   @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite.")
   @Tooltip("Kuvaus siitä, miten aikaisemmin hankittu osaaminen on tunnustettu.")
   @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
   @Representative
   @MultiLineString(5)
   selite: LocalizedString,
)

trait VSTTunnustettu {
  def tunnustettu: Option[VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen]
}

@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", "OPH-123-2021")
@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", "1/011/2012")
@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", None)
case class OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
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
) extends VapaanSivistystyönKoulutuksenPäätasonSuoritus
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta
  with OpintopistelaajuuksienYhteislaskennallinenSuoritus[LaajuusOpintopisteissä]

@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen tunnistetiedot")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(
 @KoodistoKoodiarvo("999910")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("999910", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String] = None,
 koulutustyyppi: Option[Koodistokoodiviite] = None,
 laajuus: Option[LaajuusOpintopisteissä] = None
) extends DiaarinumerollinenKoulutus
  with Tutkinto
  with OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä] {
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
}

trait VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus extends Suoritus with Vahvistukseton

@Title("Maahanmuuttajien kotoutumiskoulutuksen suomen/ruotsin kielen ja viestintätaitojen suoritus")
@Description("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen tunnistetiedot")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
 @Title("Kotoutumiskoulutuksen kieliopinnot")
 koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli,
 @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus")
 @KoodistoUri(koodistoUri = "suorituksentyyppi")
 tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus", koodistoUri = "suorituksentyyppi"),
 override val arviointi: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi]]
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

@Title("Kieliopintojen arviointi")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", "arviointiasteikkovst"),
  kuullunYmmärtämisenTaitotaso: Option[VSTKehittyvänKielenTaitotasonArviointi],
  puhumisenTaitotaso: Option[VSTKehittyvänKielenTaitotasonArviointi],
  luetunYmmärtämisenTaitotaso: Option[VSTKehittyvänKielenTaitotasonArviointi],
  kirjoittamisenTaitotaso: Option[VSTKehittyvänKielenTaitotasonArviointi],
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

case class VSTKehittyvänKielenTaitotasonArviointi(
  @KoodistoUri("arviointiasteikkokehittyvankielitaidontasot")
  @KoodistoKoodiarvo("A1.1")
  @KoodistoKoodiarvo("A1.2")
  @KoodistoKoodiarvo("A1.3")
  @KoodistoKoodiarvo("A2.1")
  @KoodistoKoodiarvo("A2.2")
  @KoodistoKoodiarvo("B1.1")
  @KoodistoKoodiarvo("B1.2")
  @KoodistoKoodiarvo("B2.1")
  @KoodistoKoodiarvo("B2.2")
  @KoodistoKoodiarvo("C1.1")
  @KoodistoKoodiarvo("C1.2")
  @KoodistoKoodiarvo("C2.1")
  @KoodistoKoodiarvo("C2.2")
  taso: Koodistokoodiviite
)

@Title("Maahanmuuttajien kotoutumiskoulutuksen kokonaisuus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
  @KoodistoUri(koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus")
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus", "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida with KoulutusmoduuliValinnainenLaajuus

@Title("Maahanmuuttajien kotoutumiskoulutuksen ohjauksen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
  @Title("Kotoutumiskoulutuksen ohjaus")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli,
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]] = None
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

@Title("Maahanmuuttajien kotoutumiskoulutuksen kokonaisuus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli(
  @KoodistoUri(koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus")
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus", "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida with KoulutusmoduuliValinnainenLaajuus

@Title("Maahanmuuttajien kotoutumiskoulutuksen työelämä- ja yhteiskuntataitojen opintojen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
  @Title("Kotoutumiskoulutuksen työelämä- ja yhteiskuntaopinnot")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli,
  override val osasuoritukset: Option[List[VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]] = None
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

@Title("Maahanmuuttajien kotoutumiskoulutuksen kokonaisuus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli(
  @KoodistoUri(koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus")
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus", "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida with KoulutusmoduuliValinnainenLaajuus


@Title("Maahanmuuttajien kotoutumiskoulutuksen valinnaisten opintojen suoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
  @Title("Kotoutumiskoulutuksen valinnaiset opinnot")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli,
  override val osasuoritukset: Option[List[VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus", koodistoUri = "suorituksentyyppi"),
  override val arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]] = None
) extends VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus

@Title("Maahanmuuttajien kotoutumiskoulutuksen kokonaisuus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli(
  @KoodistoUri(koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus")
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus", "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends KoodistostaLöytyväKoulutusmoduuli with LaajuuttaEiValidoida with KoulutusmoduuliValinnainenLaajuus

@Title("Valinnaisten opintojen osasuoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
  @Title("Valinnaiset opinnot")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Vahvistukseton

trait VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus extends Suoritus with Vahvistukseton

@Title("Työelämäjakso")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
  @Title("Työelämäjakso")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajaksonsuoritus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajaksonsuoritus", koodistoUri = "suorituksentyyppi")
) extends VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus

@Title("Työelämä- ja yhteiskuntataidot")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
  @Title("Työelämä- ja yhteiskuntataidot")
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus")
  override val tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus", koodistoUri = "suorituksentyyppi")
) extends VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus

@Title("Maahanmuuttajien kotoutumiskoulutuksen osasuoritus")
case class VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOpintopisteissä]
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus with StorablePreference
