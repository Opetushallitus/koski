package fi.oph.koski.sdg

import java.time.LocalDate

import fi.oph.koski.schema.annotation._
import fi.oph.koski.schema

case class InternationalSchoolOpiskeluoikeus(
  oid: Option[String] = None,
  oppilaitos: Option[schema.Oppilaitos] = None,
  koulutustoimija: Option[schema.Koulutustoimija] = None,
  tila: OpiskeluoikeudenTila,
  suoritukset: List[InternationalSchoolVuosiluokanSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: InternationalSchoolVuosiluokanSuoritus => s }
    )
}

trait InternationalSchoolVuosiluokanSuoritus extends Suoritus

case class MYPVuosiluokanSuoritus(
  koulutusmoduuli: schema.MYPLuokkaAste,
  @Tooltip("Vuosiluokan alkamispäivä")
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus], // vain päivä
  suorituskieli: schema.Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschoolmypvuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[MYPOppiaineenSuoritus]] = None // vain 10 koodiarvolla mukaan
) extends InternationalSchoolVuosiluokanSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): MYPVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: MYPOppiaineenSuoritus => s
      })
    )
}

case class MYPOppiaineenSuoritus(
  koulutusmoduuli: schema.MYPOppiaine,
  arviointi: Option[List[schema.MYPArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolmypoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

case class DiplomaVuosiluokanSuoritus(
  koulutusmoduuli: schema.DiplomaLuokkaAste,
  @Tooltip("Vuosiluokan alkamispäivä")
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[DiplomaIBOppiaineenSuoritus]] = None
) extends InternationalSchoolVuosiluokanSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): DiplomaVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: DiplomaIBOppiaineenSuoritus => s
      })
    )
}

trait DiplomaIBOppiaineenSuoritus extends Osasuoritus

case class DiplomaOppiaineenSuoritus(
  koulutusmoduuli: schema.InternationalSchoolIBOppiaine,
  arviointi: Option[List[schema.DiplomaArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschooldiplomaoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends DiplomaIBOppiaineenSuoritus

case class DiplomaCoreRequirementsOppiaineenSuoritus(
  koulutusmoduuli: schema.DiplomaCoreRequirementsOppiaine,
  arviointi: Option[List[schema.InternationalSchoolCoreRequirementsArviointi]] = None, // deprekoitu predicted pois
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("internationalschoolcorerequirements")
  tyyppi: schema.Koodistokoodiviite
) extends DiplomaIBOppiaineenSuoritus
