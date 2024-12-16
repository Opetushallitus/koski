package fi.oph.koski.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Muun kuin säännellyn koulutuksen opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

@Title("Muun kuin säännellyn koulutuksen päätason suoritus")
case class AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  tyyppi: schema.Koodistokoodiviite
) extends Suoritus

@Title("Muu kuin säännelty koulutus")
case class AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli
