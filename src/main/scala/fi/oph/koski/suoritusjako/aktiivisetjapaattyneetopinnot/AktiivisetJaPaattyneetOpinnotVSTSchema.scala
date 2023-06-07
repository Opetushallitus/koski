package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("Vapaan sivistystyön opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {

  override def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus => s }
    )
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = this
}

@Title("Vapaan sivistystyön päätason suoritus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  @KoodistoKoodiarvo("vstoppivelvollisillesuunnattukoulutus")
  @KoodistoKoodiarvo("vstjotpakoulutus")
  @KoodistoKoodiarvo("vstlukutaitokoulutus")
  tyyppi: schema.Koodistokoodiviite
) extends Suoritus

@Title("Vapaan sivistyön koulutus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  laajuus: Option[AktiivisetJaPäättyneetOpinnotLaajuus],
  opintokokonaisuus: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
) extends SuorituksenKooditettuKoulutusmoduuli
