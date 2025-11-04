package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("EB-tutkinnon opiskeluoikeus")
case class SdgEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SdgEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgEBTutkinnonOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgEBTutkinnonPäätasonSuoritus => s }
    )
}

@Title("EB-tutkinnon suoritus")
case class SdgEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: schema.EBTutkinto,
  vahvistus: Option[SdgVahvistus],
  toimipiste: Option[SdgToimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgEBTutkinnonOsasuoritus]],
  yleisarvosana: Option[Double]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgEBTutkinnonPäätasonSuoritus =
    this.copy(osasuoritukset = os.map(_.collect { case s: SdgEBTutkinnonOsasuoritus => s }))
}

@Title("EB-tutkinnon osasuoritus")
case class SdgEBTutkinnonOsasuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgEBOppiaineenAlaosasuoritus]] = None
) extends Osasuoritus

@Title("EB-oppiaineen alaosasuoritus")
case class SdgEBOppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: schema.EBOppiaineKomponentti,
  arviointi: Option[List[SdgEBTutkintoFinalMarkArviointi]] = None,
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

@Title("EB-tutkinnon Final Mark -arviointi")
case class SdgEBTutkintoFinalMarkArviointi(
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate],
)
