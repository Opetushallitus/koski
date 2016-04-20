package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.Description
import fi.oph.tor.localization.LocalizedStringImplicits._

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  suoritukset: List[PerusopetuksenOppimääränSuoritus],
  opiskeluoikeudenTila: Option[YleissivistäväOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("perusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetus", Some("Perusopetus"), "opiskeluoikeudentyyppi", None)
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class PerusopetuksenOppimääränSuoritus(
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus] = None,
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppimaara", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Perusopetus,
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  override val osasuoritukset: Option[List[PerusopetuksenOppiaineenSuoritus]] = None
) extends Suoritus {
  def arviointi: Option[List[Arviointi]] = None
}

case class PerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @KoodistoKoodiarvo("perusopetuksenoppiainesuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiainesuoritus", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None
) extends Oppiaineensuoritus

@Description("Perusopetus")
case class Perusopetus(
 perusteenDiaarinumero: Option[String],
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("201100")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201100", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli
