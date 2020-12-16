package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, MaxItems}

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

case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
  toimipiste: Toimipiste,
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  arviointi: Option[List[Arviointi]],
  vahvistus: Option[Vahvistus],
  osaamiskokonaisuudet: Option[List[OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus]]
) extends VapaanSivistystyönPäätasonSuoritus

case class OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
  @KoodistoUri("opintokokonaisuusnimet")
  tunniste: Koodistokoodiviite,
  arviointi: Option[List[Arviointi]],
  osasuoritukset: Option[List[OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus]]
)

case class OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  arviointi: Option[List[Arviointi]]
) extends PaikallinenKoulutusmoduuli

@Description("Vapaan sivistystyön oppivelvollisuuskoulutuksen tunnistetiedot")
case class OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(
  @KoodistoKoodiarvo("099999")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("099999", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Tutkinto with Laajuudeton
