package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("Internationl School -opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {

  override def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

@Title("Internationl School -vuosiluokan suoritus")
case class AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("internationalschoolmypvuosiluokka")
  @KoodistoKoodiarvo("internationalschooldiplomavuosiluokka")
  tyyppi: schema.Koodistokoodiviite
) extends Suoritus

case class AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  diplomaType: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli
