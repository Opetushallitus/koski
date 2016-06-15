package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{MaxItems, MinItems, Description}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPerustutkintoonValmentavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: Option[AmmatillinenOpiskeluoikeudenTila] = None,
  läsnäolotiedot: Option[Läsnäolotiedot],
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[AmmatilliseenPerustutkintoonValmentavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPerustutkintoonValmentavanKoulutuksenSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  override val osasuoritukset: Option[List[Suoritus]],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: AmmatilliseenPerustutkintoonValmentavaKoulutus
) extends Suoritus {
  def arviointi: Option[List[AmmatillinenArviointi]] = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPerustutkintoonValmentavaKoulutus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus"),
  laajuus: Option[Laajuus] = None
) extends KoodistostaLöytyväKoulutusmoduuli

