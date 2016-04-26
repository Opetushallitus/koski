package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.localization.LocalizedStringImplicits._

import fi.oph.scalaschema.annotation.{MaxItems, MinItems}

case class KorkeakoulunOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  @MinItems(1) @MaxItems(1)
  suoritukset: List[KorkeakouluTutkinnonSuoritus],
  tila: Option[KorkeakoulunOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("korkeakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulutus", Some("Korkeakoulutus"), "opiskeluoikeudentyyppi", None)
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class KorkeakouluTutkinnonSuoritus(
  koulutusmoduuli: Koulutusmoduuli,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi"),
  paikallinenId: Option[String],
  arviointi: Option[List[Arviointi]],
  tila: Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  override val osasuoritukset: Option[List[KorkeakoulunOpintosuoritus]]
) extends Suoritus

case class KorkeakoulunOpintosuoritus(
  koulutusmoduuli: Koulutusmoduuli,
  @KoodistoKoodiarvo("korkeakoulunopintosuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintosuoritus", koodistoUri = "suorituksentyyppi"),
  paikallinenId: Option[String],
  arviointi: Option[List[Arviointi]],
  tila: Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[Koodistokoodiviite]
) extends Suoritus

case class KorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso