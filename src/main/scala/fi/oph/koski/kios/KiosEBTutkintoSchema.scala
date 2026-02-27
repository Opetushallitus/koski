package fi.oph.koski.kios

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("EB-tutkinnon opiskeluoikeus")
case class KiosEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: KiosOpiskeluoikeudenTila,
  suoritukset: List[KiosEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends KiosKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[KiosOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): KiosKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: KiosEBTutkinnonPäätasonSuoritus => s }
    )
}

@Title("EB-tutkinnon päätason suoritus")
case class KiosEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: KiosEBTutkinnonKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class KiosEBTutkinnonKoulutusmoduuli(
  tunniste: KiosKoodistokoodiviite,
  curriculum: KiosKoodistokoodiviite,
  koulutustyyppi: Option[KiosKoodistokoodiviite]
) extends SuorituksenKooditettuKoulutusmoduuli
