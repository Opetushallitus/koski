package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("EB-tutkinnon opiskeluoikeus")
case class HakemuspalveluEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluEBTutkinnonPäätasonSuoritus => s }
    )
}

@Title("EB-tutkinnon päätason suoritus")
case class HakemuspalveluEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: HakemuspalveluEBTutkinnonKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class HakemuspalveluEBTutkinnonKoulutusmoduuli(
  tunniste: HakemuspalveluKoodistokoodiviite,
  curriculum: HakemuspalveluKoodistokoodiviite,
  koulutustyyppi: Option[HakemuspalveluKoodistokoodiviite]
) extends SuorituksenKooditettuKoulutusmoduuli
