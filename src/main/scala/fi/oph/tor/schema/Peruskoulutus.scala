package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.{MaxItems, MinItems, Description}

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  suoritukset: List[PeruskoulunPäättötodistus],
  opiskeluoikeudenTila: Option[OpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("peruskoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("peruskoulutus", Some("Peruskoulutus"), "opiskeluoikeudentyyppi", None)
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
}

case class PeruskoulunPäättötodistus(
  @KoodistoKoodiarvo("peruskoulunpaattotodistus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("peruskoulunpaattotodistus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Peruskoulutus,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus] = None,
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  override val osasuoritukset: Option[List[PeruskoulunOppiaineenSuoritus]]
) extends Suoritus {
  def arviointi: Option[List[Arviointi]] = None
}

case class PeruskoulunOppiaineenSuoritus(
  @KoodistoKoodiarvo("peruskoulunoppiainesuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "peruskoulunoppiainesuoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: YleissivistavaOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None
) extends Suoritus

@Description("Peruskoulutus")
case class Peruskoulutus(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("201100")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201100", koodistoUri = "koulutus")
) extends Koulutusmoduuli
