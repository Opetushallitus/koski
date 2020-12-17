package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, MaxItems, Title}
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}

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

trait VapaanSivistystyönPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus

case class OppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenSuoritus(
  toimipiste: Toimipiste,
  // @KoodistoKoodiarvo("TODO")
  tyyppi: Koodistokoodiviite, // = Koodistokoodiviite(koodiarvo = "TODO", koodistoUri = "suorituksentyyppi")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  vahvistus: Option[Vahvistus],
  @Title("Osaamiskokonaisuudet")
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus]]
) extends VapaanSivistystyönPäätasonSuoritus with Arvioinniton

@Description("Vapaan sivistystyön oppivelvollisuuskoulutuksen tunnistetiedot")
case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(
  @KoodistoKoodiarvo("099999")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("099999", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with Laajuudeton

// TODO: tämän rinnalle valinnaiset suuntautumisopinnot
case class OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
  @Title("Osaamiskokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus,
  override val osasuoritukset: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus]],
  // @KoodistoKoodiarvo("TODO")
  tyyppi: Koodistokoodiviite // = Koodistokoodiviite(koodiarvo = "TODO", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Vahvistukseton with Arvioinniton with Välisuoritus

case class OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
  @KoodistoUri("opintokokonaisuusnimet")
  tunniste: Koodistokoodiviite,
  kuvaus: LocalizedString
) extends KoodistostaLöytyväKoulutusmoduuli

case class OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
  @Title("Opintokokonaisuus")
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  arviointi: Option[List[Arviointi]], // TODO: oma arviointiluokka
  // @KoodistoKoodiarvo("TODO")
  tyyppi: Koodistokoodiviite // = Koodistokoodiviite(koodiarvo = "TODO", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Vahvistukseton

case class OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
) extends PaikallinenKoulutusmoduuli

