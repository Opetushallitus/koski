package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos}
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class SdgDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: SdgOpiskeluoikeudenTila,
  suoritukset: List[SdgDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SdgKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[SdgOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): SdgKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgDIATutkinnonSuoritus => s }
    )
}


@Title("DIA-tutkinnon suoritus")
case class SdgDIATutkinnonSuoritus(
  koulutusmoduuli: SdgDIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class SdgDIATutkinto(
  tunniste: Koodistokoodiviite
) extends SuorituksenKoulutusmoduuli
