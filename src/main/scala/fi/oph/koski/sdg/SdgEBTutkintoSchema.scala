package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("EB-tutkinnon opiskeluoikeus")
case class EBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[EBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): EBTutkinnonOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: EBTutkinnonPäätasonSuoritus => s }
    )
}

@Title("EB-tutkinnon suoritus")
case class EBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: schema.EBTutkinto,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[EBTutkinnonOsasuoritus]],
  yleisarvosana: Option[Double]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): EBTutkinnonPäätasonSuoritus =
    this.copy(osasuoritukset = os.map(_.collect { case s: EBTutkinnonOsasuoritus => s }))
}

@Title("EB-tutkinnon osasuoritus")
case class EBTutkinnonOsasuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[EBOppiaineenAlaosasuoritus]] = None
) extends Osasuoritus

@Title("EB-oppiaineen alaosasuoritus")
case class EBOppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: schema.EBOppiaineKomponentti,
  arviointi: Option[List[EBTutkintoFinalMarkArviointi]] = None,
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

@Title("EB-tutkinnon Final Mark -arviointi")
case class EBTutkintoFinalMarkArviointi(
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate],
)
