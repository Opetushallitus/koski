package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.Description

@Description("Perusopetuksen opiskeluoikeus")
case class PeruskouluOpiskeluOikeus(
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
  tyyppi: KoodistoKoodiViite = KoodistoKoodiViite("peruskoulutus", Some("Peruskoulutus"), "opiskeluoikeudentyyppi", None)
) extends OpiskeluOikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
}

case class PeruskoulunPäättötodistus(
  @KoodistoKoodiarvo("peruskoulunpaattotodistus")
  tyyppi: KoodistoKoodiViite = KoodistoKoodiViite("peruskoulunpaattotodistus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Peruskoulutus,
  paikallinenId: Option[String],
  suorituskieli: Option[KoodistoKoodiViite],
  tila: KoodistoKoodiViite,
  alkamispäivä: Option[LocalDate],
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[Arviointi]] = None,
  vahvistus: Option[Vahvistus] = None
) extends Suoritus

case class PeruskoulunOppiaineSuoritus(
  @KoodistoKoodiarvo("peruskoulunoppiainesuoritus")
  tyyppi: KoodistoKoodiViite,
  koulutusmoduuli: Oppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[KoodistoKoodiViite],
  tila: KoodistoKoodiViite,
  alkamispäivä: Option[LocalDate],
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[Arviointi]] = None,
  vahvistus: Option[Vahvistus] = None
) extends Suoritus

@Description("Tutkintoon johtava koulutus")
case class Peruskoulutus(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("201100")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: KoodistoKoodiViite = KoodistoKoodiViite("201100", koodistoUri = "koulutus")
) extends Koulutusmoduuli

case class Oppiaine(
  @Description("Peruskoulutuksen oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  tunniste: KoodistoKoodiViite
) extends Koulutusmoduuli
