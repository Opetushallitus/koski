package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("EB-tutkinnon opiskeluoikeus")
case class HakemuspalveluEBTutkinnonOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluEBTutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluEBTutkinnonPäätasonSuoritus => s }
    )
}

@Title("EB-tutkinnon päätason suoritus")
case class HakemuspalveluEBTutkinnonPäätasonSuoritus(
  koulutusmoduuli: HakemuspalveluEBTutkinnonKoulutusmoduuli,
  vahvistus: Option[HakemuspalveluVahvistus],
  toimipiste: Option[HakemuspalveluToimipiste],
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluSuoritus

case class HakemuspalveluEBTutkinnonKoulutusmoduuli(
  tunniste: HakemuspalveluKoodistokoodiviite,
  curriculum: HakemuspalveluKoodistokoodiviite,
  koulutustyyppi: Option[HakemuspalveluKoodistokoodiviite]
) extends HakemuspalveluSuorituksenKooditettuKoulutusmoduuli
