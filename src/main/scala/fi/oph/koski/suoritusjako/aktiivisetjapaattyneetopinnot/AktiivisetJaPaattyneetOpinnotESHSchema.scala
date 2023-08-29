package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("European School of Helsinki -opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

@Title("European School of Helsinki päätason suoritus")
case class AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]
) extends SuorituksenKooditettuKoulutusmoduuli
