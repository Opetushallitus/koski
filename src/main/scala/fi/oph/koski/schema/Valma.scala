package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.scalaschema.annotation.{MaxItems, MinItems, Description}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenKoulutukseenValmentavanKoulutuksenOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[AmmatillinenOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[AmmatilliseenKoulutukseenValmentavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenKoulutukseenValmentavanKoulutuksenSuoritus(
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Valma
) extends Suoritus {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class Valma(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}