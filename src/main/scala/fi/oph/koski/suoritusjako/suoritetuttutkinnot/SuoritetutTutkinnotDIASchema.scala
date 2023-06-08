package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDateTime

@Title("DIA-tutkinnon opiskeluoikeus")
case class SuoritetutTutkinnotDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  suoritukset: List[SuoritetutTutkinnotDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : SuoritetutTutkinnotDIATutkinnonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: SuoritetutTutkinnotOpiskeluoikeus = this.copy(sisältyyOpiskeluoikeuteen = None)
}

@Title("DIA-tutkinnon suoritus")
case class SuoritetutTutkinnotDIATutkinnonSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotDIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[SuoritetutTutkinnotKoodistokoodiviite],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class SuoritetutTutkinnotDIATutkinto(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli
