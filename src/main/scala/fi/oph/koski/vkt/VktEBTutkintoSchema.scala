package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("EB-tutkinnon opiskeluoikeus")
case class VktEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: VktOpiskeluoikeudenTila,
  suoritukset: List[VktEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends VktKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[VktOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): VktKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VktEBTutkinnonPäätasonSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VktKoskeenTallennettavaOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

@Title("EB-tutkinnon päätason suoritus")
case class VktEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: VktEBTutkinnonKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class VktEBTutkinnonKoulutusmoduuli(
  tunniste: VktKoodistokoodiviite,
  curriculum: VktKoodistokoodiviite,
  koulutustyyppi: Option[VktKoodistokoodiviite]
) extends SuorituksenKooditettuKoulutusmoduuli
