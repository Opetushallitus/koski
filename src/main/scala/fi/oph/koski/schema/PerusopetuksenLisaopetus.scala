package fi.oph.koski.schema


import java.time.LocalDate
import fi.oph.scalaschema.annotation.{Description, MinItems, MaxItems}

case class PerusopetuksenLisäopetuksenOpiskeluoikeus(
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
  suoritukset: List[PerusopetuksenLisäopetuksenSuoritus],
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class PerusopetuksenLisäopetuksenSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("perusopetuksenlisaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenlisaopetus", koodistoUri = "suorituksentyyppi"),
  override val osasuoritukset: Option[List[PerusopetuksenLisäopetuksenOppiaineenSuoritus]],
  koulutusmoduuli: PerusopetuksenLisäopetus
) extends Suoritus {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
}

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen lisäopetusta")
case class PerusopetuksenLisäopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @KoodistoKoodiarvo("perusopetuksenlisaopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenlisaopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  @Description("Onko kyseessä perusopetuksen oppiaineen arvosanan korotus")
  korotus: Boolean,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None
) extends OppiaineenSuoritus

case class PerusopetuksenLisäopetus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("020075")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("020075", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}