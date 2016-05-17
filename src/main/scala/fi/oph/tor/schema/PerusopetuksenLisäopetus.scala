package fi.oph.tor.schema


import java.time.LocalDate
import fi.oph.scalaschema.annotation.{Description, MinItems, MaxItems}

case class PerusopetuksenLisäopetuksenOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[YleissivistäväOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[PerusopetuksenLisäopetuksenSuoritus],
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class PerusopetuksenLisäopetuksenSuoritus(
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus] = None,
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: PerusopetuksenLisäopetus
) extends PerusopetuksenPäätasonSuoritus {
  def arviointi: Option[List[Arviointi]] = None
}

case class PerusopetuksenLisäopetus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("020075")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("020075", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}