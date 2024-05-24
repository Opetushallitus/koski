package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class VKTDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: VKTOpiskeluoikeudenTila,
  suoritukset: List[VKTDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends VKTKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[VKTOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): VKTKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VKTPäätasonSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VKTKoskeenTallennettavaOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}


@Title("DIA-tutkinnon suoritus")
case class VKTDIATutkinnonSuoritus(
  koulutusmoduuli: VKTDIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VKTKoodistokoodiviite],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class VKTDIATutkinto(
  tunniste: VKTKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli
