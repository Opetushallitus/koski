package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  tila: PerusopetuksenOpiskeluoikeudenTila,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[PerusopetukseenValmistavanOpetuksenSuoritus],
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[PerusopetukseenValmistavanOpetuksenSuoritus]])
  override def arvioituPäättymispäivä = None
}

case class PerusopetukseenValmistavanOpetuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: PerusopetukseenValmistavaOpetus = PerusopetukseenValmistavaOpetus(),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus]],
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with Toimipisteellinen {
  def arviointi = None
}

case class PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[SanallinenPerusopetuksenOppiaineenArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavanopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus

@Description("Perusopetukseen valmistavan opetuksen tunnistetiedot")
case class PerusopetukseenValmistavaOpetus(
  @KoodistoKoodiarvo("999905")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999905", koodistoUri = "koulutus")
) extends Koulutus {
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