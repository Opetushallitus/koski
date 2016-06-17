package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid] = None,
  tila: Option[AmmatillinenOpiskeluoikeudenTila] = None,
  läsnäolotiedot: Option[Läsnäolotiedot] = None,
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osasuoritukset")
  override val osasuoritukset: Option[List[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: AmmatilliseenPeruskoulutukseenValmentavaKoulutus
) extends Suoritus {
  def arviointi = None
}

case class AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsanSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("valmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valmakoulutuksenosa", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa,
  arviointi: Option[List[AmmatillinenArviointi]]
) extends Suoritus {
  override def osasuoritukset = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPeruskoulutukseenValmentavaKoulutus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus"),
  laajuus: Option[Laajuus] = None
) extends KoodistostaLöytyväKoulutusmoduuli

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osa")
case class AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusOsaamispisteissä],
  @Description("Onko pakollinen osa tutkinnossa")
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli
