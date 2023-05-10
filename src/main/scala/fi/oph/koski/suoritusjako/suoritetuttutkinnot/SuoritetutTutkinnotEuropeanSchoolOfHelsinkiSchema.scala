package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDateTime

@Title("European School of Helsinki -opiskeluoikeus")
case class SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  suoritukset: List[SuoritetutTutkinnotEBTutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends SuoritetutTutkinnotOpiskeluoikeus {
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : SuoritetutTutkinnotEBTutkinnonSuoritus => s }
    )
}

@Title("EB-tutkinnon suoritus")
case class SuoritetutTutkinnotEBTutkinnonSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotEBTutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class SuoritetutTutkinnotEBTutkinto(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  koulutustyyppi: Option[SuoritetutTutkinnotKoodistokoodiviite],
  curriculum: SuoritetutTutkinnotKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli
