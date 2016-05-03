package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.localization.LocalizedString._
import fi.oph.tor.localization.LocalizedStringImplicits._

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

case class KorkeakoulunOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
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
  koulutusmoduuli: KorkeakouluTutkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi"),
  paikallinenId: Option[String],
  arviointi: Option[List[Arviointi]],
  tila: Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]]
) extends Suoritus {
  override def tarvitseeVahvistuksen = false
}

@Description("Tutkintoon johtava koulutus")
case class KorkeakouluTutkinto(
  @Description("Tutkinnon 6-numeroinen tutkintokoodi")
  @KoodistoUri("koulutus")
  @OksaUri("tmpOKSAID560", "tutkinto")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli  {
  override def laajuus = None
}

case class KorkeakoulunOpintojaksonSuoritus(
  koulutusmoduuli: KorkeakoulunOpintojakso,
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi"),
  paikallinenId: Option[String],
  arviointi: Option[List[KorkeakoulunArviointi]],
  tila: Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None
) extends Suoritus {
  override def tarvitseeVahvistuksen = false
}

@Description("Opintojakson suoritus")
case class KorkeakoulunOpintojakso(
  tunniste: Paikallinenkoodi,
  nimi: LocalizedString,
  laajuus: Option[LaajuusOpintopisteissä]
) extends Koulutusmoduuli

case class KorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso


case class KorkeakoulunArviointi(
  @KoodistoUri("virtaarvosana")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate]
) extends Arviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class LaajuusOpintopisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("2")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("2", Some(finnish("opintopistettä")), "opintojenlaajuusyksikko")
) extends Laajuus