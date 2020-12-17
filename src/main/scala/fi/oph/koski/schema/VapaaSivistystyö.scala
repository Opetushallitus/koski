package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{DefaultValue, Description, MaxItems, Title}
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
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
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
}

case class VapaanSivistystyönOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[VapaanSivistystyönOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class VapaanSivistystyönOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso

case class VapaanSivistystyönOpiskeluoikeudenLisätiedot() extends OpiskeluoikeudenLisätiedot

trait VapaanSivistystyönPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Suorituskielellinen with Todistus with Arvioinniton

case class OppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenSuoritus(
  toimipiste: Toimipiste,
  // @KoodistoKoodiarvo("TODO")
  tyyppi: Koodistokoodiviite, // = Koodistokoodiviite(koodiarvo = "TODO", koodistoUri = "suorituksentyyppi")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @Description("Koulutuksen opetuskieli")
  @Title("Opetuskieli")
  suorituskieli: Koodistokoodiviite,
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus]],
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

// TODO: tämän rinnalle valinnaiset suuntautumisopinnot
@Title("Osaamiskokonaisuuden suoritus")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
  @Title("Osaamiskokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnatunVapaanSivistystyönKoulutus,
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus]],
  // @KoodistoKoodiarvo("TODO")
  tyyppi: Koodistokoodiviite // = Koodistokoodiviite(koodiarvo = "TODO", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Vahvistukseton with Arvioinniton with Välisuoritus

trait OppivelvollisilleSuunnatunVapaanSivistystyönKoulutus extends KoulutusmoduuliValinnainenLaajuus {
  @Description("Laajuus lasketaan yhteen opintokokonaisuuksien laajuuksista automaattisesti tietoja siirrettäessä") // TODO: Toteuta tämä
  def laajuus: Option[LaajuusOpintopisteissä]
}

@Title("Valinnaiset suuntautumisopinnot")
case class OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(
  @Hidden
  tunniste: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenKoodi = OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenKoodi(),
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönKoulutus {
  override def nimi: LocalizedString = LocalizedString.empty
}

case class OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenKoodi(
  @Description("Käytä aina merkkijonoa vstsuuntautumisopinnot")
  @DefaultValue("vstsuuntautumisopinnot")
  koodiarvo: String = "vstsuuntautumisopinnot"
) extends PaikallinenKoodiviite {
  override def nimi: LocalizedString = LocalizedString.empty
}

@Title("Osaamiskokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
  @KoodistoUri("opintokokonaisuusnimet")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends OppivelvollisilleSuunnatunVapaanSivistystyönKoulutus with KoodistostaLöytyväKoulutusmoduuli

@Title("Opintokokonaisuuden suoritus")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
  @Title("Opintokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  arviointi: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi]],
  // @KoodistoKoodiarvo("TODO")
  tyyppi: Koodistokoodiviite // = Koodistokoodiviite(koodiarvo = "TODO", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Vahvistukseton

@Title("Opintokokonaisuus")
case class OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: LaajuusOpintopisteissä
) extends PaikallinenKoulutusmoduuliPakollinenLaajuus

@Title("Arviointi")
case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
  @KoodistoKoodiarvo("H")
  @KoodistoKoodiarvo("S")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"), // TODO: oma koodisto?
  päivä: LocalDate
) extends ArviointiPäivämäärällä with YleissivistävänKoulutuksenArviointi
