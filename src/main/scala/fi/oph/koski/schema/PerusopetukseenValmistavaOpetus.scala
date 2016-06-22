package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{MaxItems, MinItems}

case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[PerusopetuksenOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
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
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: PerusopetukseenValmistavaOpetus
) extends Suoritus with Toimipisteellinen {
  def arviointi = None
}

case class PerusopetukseenValmistavaOpetus(
  @KoodistoKoodiarvo("039997")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("039997", koodistoUri = "koulutus")
) extends Koulutus {
  def laajuus = None
}
