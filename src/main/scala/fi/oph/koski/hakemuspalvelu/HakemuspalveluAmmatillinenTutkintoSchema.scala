package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Ammatillinen opiskeluoikeus")
case class HakemuspalveluAmmatillinenTutkintoOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluAmmatillisenTutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluAmmatillisenTutkinnonSuoritus => s }
    )
}


@Title("Ammatillisen tutkinnon suoritus")
case class HakemuspalveluAmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: HakemuspalveluAmmatillinenTutkinto,
  toimipiste: Option[HakemuspalveluToimipiste],
  vahvistus: Option[HakemuspalveluVahvistus],
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluSuoritus

case class HakemuspalveluAmmatillinenTutkinto(
  tunniste: HakemuspalveluKoodistokoodiviite,
  koulutustyyppi: Option[schema.Koodistokoodiviite]
) extends HakemuspalveluSuorituksenKoulutusmoduuli
