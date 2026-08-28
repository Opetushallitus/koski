package fi.oph.koski.sdg

import java.time.LocalDate
import fi.oph.koski.schema.annotation._
import fi.oph.koski.schema
import fi.oph.scalaschema.annotation.Title

@Title("International school opiskeluoikeus")
case class SdgInternationalSchoolOpiskeluoikeus(
  oid: Option[String] = None,
  oppilaitos: Option[schema.Oppilaitos] = None,
  koulutustoimija: Option[schema.Koulutustoimija] = None,
  tila: SdgOpiskeluoikeudenTila,
  suoritukset: List[SdgInternationalSchoolVuosiluokanSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SdgOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[SdgSuoritus]): SdgOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgInternationalSchoolVuosiluokanSuoritus => s }
    )
}

trait SdgInternationalSchoolVuosiluokanSuoritus extends SdgSuoritus

@Title("MYP vuosiluokan suoritus")
case class SdgMYPVuosiluokanSuoritus(
  koulutusmoduuli: schema.MYPLuokkaAste,
  @Tooltip("Vuosiluokan alkamispäivä")
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus], // vain päivä
  suorituskieli: schema.Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschoolmypvuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgMYPOppiaineenSuoritus]] = None // vain 10 koodiarvolla mukaan
) extends SdgInternationalSchoolVuosiluokanSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgMYPVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgMYPOppiaineenSuoritus => s
      })
    )
}

@Title("MYP oppiaineen suoritus")
case class SdgMYPOppiaineenSuoritus(
  koulutusmoduuli: schema.MYPOppiaine,
  arviointi: Option[List[schema.MYPArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolmypoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

@Title("Diploma vuosiluokan suoritus")
case class SdgDiplomaVuosiluokanSuoritus(
  koulutusmoduuli: schema.DiplomaLuokkaAste,
  @Tooltip("Vuosiluokan alkamispäivä")
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDiplomaIBOppiaineenSuoritus]] = None
) extends SdgInternationalSchoolVuosiluokanSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgDiplomaVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgDiplomaIBOppiaineenSuoritus => s
      })
    )
}

trait SdgDiplomaIBOppiaineenSuoritus extends Osasuoritus

@Title("Diploma oppiaineen suoritus")
case class SdgDiplomaOppiaineenSuoritus(
  koulutusmoduuli: schema.InternationalSchoolIBOppiaine,
  arviointi: Option[List[schema.DiplomaArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschooldiplomaoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends SdgDiplomaIBOppiaineenSuoritus

@Title("Diploma core requirements oppiaineen suoritus")
case class SdgDiplomaCoreRequirementsOppiaineenSuoritus(
  koulutusmoduuli: schema.DiplomaCoreRequirementsOppiaine,
  arviointi: Option[List[schema.InternationalSchoolCoreRequirementsArviointi]] = None, // deprekoitu predicted pois
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolcorerequirements")
  tyyppi: schema.Koodistokoodiviite
) extends SdgDiplomaIBOppiaineenSuoritus
