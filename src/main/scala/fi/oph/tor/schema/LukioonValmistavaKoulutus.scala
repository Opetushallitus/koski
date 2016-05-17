package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.scalaschema.annotation.{MaxItems, MinItems, Description}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenOpiskeluoikeus(
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
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenSuoritus(
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus] = None,
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukioonValmistavaKoulutus
) extends Suoritus {
  def arviointi: Option[List[Arviointi]] = None
}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavaKoulutus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("039997")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("039997", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}