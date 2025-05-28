package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.massaluovutus.suorituspalvelu.SupaUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("International school opiskeluoikeus")
case class SupaInternationalSchoolOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: InternationalSchoolOpiskeluoikeudenTila,
  suoritukset: List[SupaDiplomaVuosiluokanSuoritus],
) extends SupaOpiskeluoikeus

object SupaInternationalSchoolOpiskeluoikeus {
  def apply(oo: InternationalSchoolOpiskeluoikeus): Option[SupaOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SupaInternationalSchoolOpiskeluoikeus(
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        suoritukset = oo.suoritukset.flatMap {
          case s: DiplomaVuosiluokanSuoritus => SupaDiplomaVuosiluokanSuoritus(s)
          case _ => None
        }
      )
    }
}

@Title("Diploma vuosiluokan suoritus")
case class SupaDiplomaVuosiluokanSuoritus(
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: DiplomaLuokkaAste,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SupaDiplomaIBOppiaineenSuoritus],
) extends SupaSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaDiplomaVuosiluokanSuoritus {
  def apply(s: DiplomaVuosiluokanSuoritus): Option[SupaDiplomaVuosiluokanSuoritus] =
    when (s.valmis && s.koulutusmoduuli.tunniste.koodiarvo == "12") {
      SupaDiplomaVuosiluokanSuoritus(
        tyyppi = s.tyyppi,
        alkamispäivä = s.alkamispäivä,
        vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
        koulutusmoduuli = s.koulutusmoduuli,
        suorituskieli = s.suorituskieli,
        osasuoritukset = s.osasuoritukset.toList.flatten.map(SupaDiplomaIBOppiaineenSuoritus.apply),
      )
    }
}

@Title("Diploma-IB-oppiaineen suoritus")
case class SupaDiplomaIBOppiaineenSuoritus(
  @KoodistoKoodiarvo("internationalschooldiplomaoppiaine")
  @KoodistoKoodiarvo("internationalschoolcorerequirements")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: Koulutusmoduuli,
  // TODO TOR-2154: Lisää predicted-arvosana, kunhan se on ensin toteutettu Diploma IB:n tietomalliin
) extends SupaSuoritus

object SupaDiplomaIBOppiaineenSuoritus {
  def apply(s: DiplomaIBOppiaineenSuoritus): SupaDiplomaIBOppiaineenSuoritus =
    SupaDiplomaIBOppiaineenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}
