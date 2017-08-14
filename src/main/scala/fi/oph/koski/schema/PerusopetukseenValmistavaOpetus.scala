package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, Title}

@Description("Perusopetukseen valmistavan opetuksen opiskeluoikeuden tiedot")
case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Description("oppijan oppimäärän alkamispäivä")
  alkamispäivä: Option[LocalDate],
  @Description("oppijan oppimäärän päättymispäivä")
  päättymispäivä: Option[LocalDate],
  tila: PerusopetuksenOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[PerusopetukseenValmistavanOpetuksenSuoritus],
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], oid: Option[String], versionumero: Option[Int]) = this.copy(id = id, oid = oid, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[PerusopetukseenValmistavanOpetuksenSuoritus]])
  override def arvioituPäättymispäivä = None
  override def lisätiedot = None
}

@Description("Perusopetukseen valmistavan opetuksen suorituksen tiedot")
case class PerusopetukseenValmistavanOpetuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PerusopetukseenValmistavaOpetus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with MonikielinenSuoritus with Suorituskielellinen

@Description("Perusopetukseen valmistavan opetuksen oppiaineen suoritustiedot")
case class PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[SanallinenPerusopetuksenOppiaineenArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavanopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavanopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus with MahdollisestiSuorituskielellinen

@Description("Perusopetukseen valmistavan opetuksen tunnistetiedot")
case class PerusopetukseenValmistavaOpetus(
  @KoodistoKoodiarvo("999905")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999905", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String]
) extends DiaarinumerollinenKoulutus {
  def laajuus = None
}

@Description("Perusopetukseen valmistavan opetuksen oppiaineen tunnistetiedot")
case class PerusopetukseenValmistavanOpetuksenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[PerusopetukseenValmistavanKoulutuksenLaajuus],
  opetuksenSisältö: Option[LocalizedString]
) extends PaikallinenKoulutusmoduuli

case class PerusopetukseenValmistavanKoulutuksenLaajuus(
  arvo: Float,
  yksikkö: Koodistokoodiviite
) extends Laajuus
