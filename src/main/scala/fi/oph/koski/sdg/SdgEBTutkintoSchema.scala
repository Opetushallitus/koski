package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("EB-tutkinnon opiskeluoikeus")
case class SdgEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: schema.EBOpiskeluoikeudenTila,
  suoritukset: List[SdgEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SdgKoskeenTallennettavaOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgEBTutkinnonPäätasonSuoritus => s }
    )
}

@Title("EB-tutkinnon päätason suoritus")
case class SdgEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: schema.EBTutkinto,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[Osasuoritus]]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgEBTutkinnonPäätasonSuoritus =
    this.copy(osasuoritukset = os)
}
