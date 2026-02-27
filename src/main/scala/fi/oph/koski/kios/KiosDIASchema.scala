package fi.oph.koski.kios

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class KiosDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: KiosOpiskeluoikeudenTila,
  suoritukset: List[KiosDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends KiosKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[KiosOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): KiosKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: KiosDIATutkinnonSuoritus => s }
    )
}


@Title("DIA-tutkinnon suoritus")
case class KiosDIATutkinnonSuoritus(
  koulutusmoduuli: KiosDIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class KiosDIATutkinto(
  tunniste: KiosKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli
