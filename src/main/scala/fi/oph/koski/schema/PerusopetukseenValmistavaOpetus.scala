package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{MaxItems, MinItems}

case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid] = None,
  tila: Option[PerusopetuksenOpiskeluoikeudenTila] = None,
  läsnäolotiedot: Option[Läsnäolotiedot] = None,
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[PerusopetukseenValmistavanOpetuksenSuoritus],
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class PerusopetukseenValmistavanOpetuksenSuoritus(
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: PerusopetukseenValmistavaOpetus = PerusopetukseenValmistavaOpetus(),
  override val osasuoritukset: Option[List[PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus]]
) extends Suoritus with Toimipisteellinen {
  def arviointi = None
}

case class PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine,
  tila: Koodistokoodiviite,
  suorituskieli: Option[Koodistokoodiviite] = None,
  arviointi: Option[List[SanallinenPerusopetuksenOppiaineenArviointi]],
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavanopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends Suoritus {
  def vahvistus = None
}

case class PerusopetukseenValmistavaOpetus(
  @KoodistoKoodiarvo("koski1") // TODO: odotetaan virallista koodia
  tunniste: Koodistokoodiviite = Koodistokoodiviite("koski1", koodistoUri = "koulutus")
) extends Koulutus {
  def laajuus = None
}

case class PerusopetukseenValmistavanOpetuksenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa],
  opetuksenSisältö: Option[LocalizedString]
) extends PaikallinenKoulutusmoduuli