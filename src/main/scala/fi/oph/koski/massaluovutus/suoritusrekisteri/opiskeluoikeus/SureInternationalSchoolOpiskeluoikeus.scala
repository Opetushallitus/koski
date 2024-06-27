package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("International School")
case class SureInternationalSchoolOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SureDiplomaVuosiluokanSuoritus],
) extends SureOpiskeluoikeus

object SureInternationalSchoolOpiskeluoikeus {
  def apply(oo: InternationalSchoolOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SureInternationalSchoolOpiskeluoikeus(
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        suoritukset = oo.suoritukset.flatMap {
          case s: DiplomaVuosiluokanSuoritus => SureDiplomaVuosiluokanSuoritus(s)
          case _ => None
        }
      )
    }
}

@Title("Diploma-vuosiluokan suoritus")
case class SureDiplomaVuosiluokanSuoritus(
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: DiplomaLuokkaAste,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SureDiplomaIBOppiaineenSuoritus],
) extends SureSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureDiplomaVuosiluokanSuoritus {
  def apply(s: DiplomaVuosiluokanSuoritus): Option[SureDiplomaVuosiluokanSuoritus] =
    when (s.valmis && s.koulutusmoduuli.tunniste.koodiarvo == "12") {
      SureDiplomaVuosiluokanSuoritus(
        tyyppi = s.tyyppi,
        alkamispäivä = s.alkamispäivä,
        vahvistuspäivä = s.vahvistus.map(_.päivä),
        koulutusmoduuli = s.koulutusmoduuli,
        suorituskieli = s.suorituskieli,
        osasuoritukset = s.osasuoritukset.toList.flatten.map(SureDiplomaIBOppiaineenSuoritus.apply),
      )
    }
}

@Title("Diploma-IB-oppiaineen suoritus")
case class SureDiplomaIBOppiaineenSuoritus(
  @KoodistoKoodiarvo("internationalschooldiplomaoppiaine")
  @KoodistoKoodiarvo("internationalschoolcorerequirements")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: Koulutusmoduuli,
  // TODO TOR-2154: Lisää predicted-arvosana, kunhan se on ensin toteutettu Diploma IB:n tietomalliin
) extends SureSuoritus

object SureDiplomaIBOppiaineenSuoritus {
  def apply(s: DiplomaIBOppiaineenSuoritus): SureDiplomaIBOppiaineenSuoritus =
    SureDiplomaIBOppiaineenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}
