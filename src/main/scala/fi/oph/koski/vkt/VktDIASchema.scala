package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class VktDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: VktOpiskeluoikeudenTila,
  suoritukset: List[VktDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends VktKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[VktOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): VktKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VktDIATutkinnonSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VktKoskeenTallennettavaOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}


@Title("DIA-tutkinnon suoritus")
case class VktDIATutkinnonSuoritus(
  koulutusmoduuli: VktDIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VktKoodistokoodiviite],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class VktDIATutkinto(
  tunniste: VktKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli
