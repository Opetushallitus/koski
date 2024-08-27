package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Perusopetuksen opiskeluoikeus")
case class HakemuspalveluPerusopetuksenOppimääränOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluPerusopetuksenOppimääränSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluPerusopetuksenOppimääränSuoritus => s }
    )
}


@Title("Nuorten perusopetuksen oppimäärän suoritus")
case class HakemuspalveluPerusopetuksenOppimääränSuoritus(
  koulutusmoduuli: HakemuspalveluPerusopetuksenOppimäärä,
  toimipiste: Option[HakemuspalveluToimipiste],
  vahvistus: Option[HakemuspalveluVahvistus],
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluSuoritus

case class HakemuspalveluPerusopetuksenOppimäärä(
  tunniste: HakemuspalveluKoodistokoodiviite
) extends HakemuspalveluSuorituksenKoulutusmoduuli
