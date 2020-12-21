package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{DefaultValue, Description, MaxItems, MinItems, Title}
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation.{Hidden, KoodistoKoodiarvo, KoodistoUri}

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
  toimipiste: Toimipiste,
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
) extends VapaanSivistystyönPäätasonSuoritus

@Description("Vapaan sivistystyön oppivelvollisuuskoulutuksen tunnistetiedot")
case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(
  @KoodistoKoodiarvo("099999")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("099999", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with Laajuudeton

trait OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus extends Suoritus with Vahvistukseton with Arvioinniton with Välisuoritus

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
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus]],
  @KoodistoKoodiarvo("vstvalinnainensuuntautuminen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstvalinnainensuuntautuminen", koodistoUri = "suorituksentyyppi")
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

trait OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli extends KoulutusmoduuliValinnainenLaajuus with KoodistostaLöytyväKoulutusmoduuli {
  @Description("Laajuus lasketaan yhteen opintokokonaisuuksien laajuuksista automaattisesti tietoja siirrettäessä") // TODO: Toteuta tämä
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
  @KoodistoUri("opintokokonaisuusnimet")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönOsasuorituksenKoulutusmoduuli with KoodistostaLöytyväKoulutusmoduuli

@Title("Opintokokonaisuuden suoritus")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
  @Title("Opintokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  @KoodistoKoodiarvo("vstopintokokonaisuus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "vstopintokokonaisuus", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Vahvistukseton

@Title("Opintokokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: LaajuusOpintopisteissä
) extends PaikallinenKoulutusmoduuliPakollinenLaajuus

@Title("Arviointi")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
  @KoodistoKoodiarvo("Hyväksytty")
  @KoodistoKoodiarvo("Hylätty")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkovst"),
  päivä: LocalDate
) extends ArviointiPäivämäärällä with VapaanSivistystyönKoulutuksenArviointi

trait VapaanSivistystyönKoulutuksenArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkovst")
  def arvosana: Koodistokoodiviite
  def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo match {
    case "H" => false
    case _ => true
  }
}
