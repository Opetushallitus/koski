package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotPäätasonSuoritus],
  lisätiedot: Option[AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.tuva.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  järjestämislupa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}
