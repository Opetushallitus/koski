package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Aikuisten perusopetuksen oppimäärän opiskeluoikeus")
case class HakemuspalveluAikuistenPerusopetuksenOppimääränOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluAikuistenPerusopetuksenOppimääränSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluAikuistenPerusopetuksenOppimääränSuoritus => s }
    )
}


@Title("Aikuisten perusopetuksen oppimäärän suoritus")
case class HakemuspalveluAikuistenPerusopetuksenOppimääränSuoritus(
  koulutusmoduuli: HakemuspalveluAikuistenPerusopetuksenOppimäärä,
  toimipiste: Option[HakemuspalveluToimipiste],
  vahvistus: Option[HakemuspalveluVahvistus],
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluSuoritus

case class HakemuspalveluAikuistenPerusopetuksenOppimäärä(
  tunniste: HakemuspalveluKoodistokoodiviite
) extends HakemuspalveluSuorituksenKoulutusmoduuli
