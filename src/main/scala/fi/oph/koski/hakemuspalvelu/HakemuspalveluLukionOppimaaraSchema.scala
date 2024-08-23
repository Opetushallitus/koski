package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Lukion oppimäärän opiskeluoikeus")
case class HakemuspalveluLukionOppimääränOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluLukionOppimääränSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluLukionOppimääränSuoritus => s }
    )
}


@Title("Lukion oppimäärän suoritus")
case class HakemuspalveluLukionOppimääränSuoritus(
  koulutusmoduuli: HakemuspalveluLukionOppimäärä,
  toimipiste: Option[HakemuspalveluToimipiste],
  vahvistus: Option[HakemuspalveluVahvistus],
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluSuoritus

case class HakemuspalveluLukionOppimäärä(
  tunniste: HakemuspalveluKoodistokoodiviite
) extends HakemuspalveluSuorituksenKoulutusmoduuli
