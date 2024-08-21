package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Ammatillisen tutkinnon opiskeluoikeus")
case class HakemuspalveluAmmatillinenTutkintoOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluAmmatillisenTutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluAmmatillisenTutkinnonSuoritus => s }
    )
}


@Title("Ammatillisen oppimäärän suoritus")
case class HakemuspalveluAmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: HakemuspalveluAmmatillinenTutkinto,
  toimipiste: Option[HakemuspalveluToimipiste],
  vahvistus: Option[HakemuspalveluVahvistus],
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluSuoritus

case class HakemuspalveluAmmatillinenTutkinto(
  tunniste: HakemuspalveluKoodistokoodiviite
) extends HakemuspalveluSuorituksenKoulutusmoduuli
