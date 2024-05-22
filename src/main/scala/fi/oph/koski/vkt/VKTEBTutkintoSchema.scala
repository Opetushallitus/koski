package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("EB-tutkinnon opiskeluoikeus")
case class VKTEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: VKTOpiskeluoikeudenTila,
  suoritukset: List[VKTEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends VKTKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[VKTOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): VKTKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VKTEBTutkinnonPäätasonSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VKTKoskeenTallennettavaOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

@Title("EB-tutkinnon päätason suoritus")
case class VKTEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: VKTEBTutkinnonKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class VKTEBTutkinnonKoulutusmoduuli(
  tunniste: VKTKoodistokoodiviite,
  curriculum: VKTKoodistokoodiviite,
  koulutustyyppi: Option[VKTKoodistokoodiviite]
) extends SuorituksenKooditettuKoulutusmoduuli
