package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation.{Hidden, KoodistoKoodiarvo, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MaxItems, Title}

@Description("Perusopetukseen valmistavan opetuksen opiskeluoikeuden tiedot")
case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("oppijan oppimäärän päättymispäivä")
  päättymispäivä: Option[LocalDate],
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[PerusopetukseenValmistavanOpetuksenSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
  override def lisätiedot = None
}

@Description("Perusopetukseen valmistavan opetuksen suorituksen tiedot")
case class PerusopetukseenValmistavanOpetuksenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: PerusopetukseenValmistavaOpetus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PerusopetukseenValmistavanOpetuksenOsasuoritus]],
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi tieto oppilaan perusopetuksen aloittamisesta (luokkataso).")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", koodistoUri = "suorituksentyyppi")
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with MonikielinenSuoritus with Suorituskielellinen

trait PerusopetukseenValmistavanOpetuksenOsasuoritus extends Suoritus

@Description("Perusopetukseen valmistavan opetuksen oppiaineen suoritustiedot")
case class PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine,
  arviointi: Option[List[SanallinenPerusopetuksenOppiaineenArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavanopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavanopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends Vahvistukseton with MahdollisestiSuorituskielellinen with PerusopetukseenValmistavanOpetuksenOsasuoritus

@Description("Perusopetukseen valmistavan opetuksen tunnistetiedot")
case class PerusopetukseenValmistavaOpetus(
  @KoodistoKoodiarvo("999905")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999905", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus {
  def laajuus = None
}

@Description("Perusopetukseen valmistavan opetuksen oppiaineen tunnistetiedot")
case class PerusopetukseenValmistavanOpetuksenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[PerusopetukseenValmistavanKoulutuksenLaajuus],
  @Tooltip("Oppiaineen opetuksen sisältö.")
  opetuksenSisältö: Option[LocalizedString]
) extends PaikallinenKoulutusmoduuli with StorablePreference {
  def kuvaus: LocalizedString = opetuksenSisältö.getOrElse(LocalizedString.empty)
}

case class PerusopetukseenValmistavanKoulutuksenLaajuus(
  arvo: Float,
  yksikkö: Koodistokoodiviite
) extends Laajuus
