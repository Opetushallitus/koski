package fi.oph.koski.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{OnlyWhen, Title}

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
) extends AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus => s }
    )
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus = this
}

trait AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus extends Suoritus {
  def suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

@Title("Oppivelvollisille suunnattu maahanmuuttajien kotoutusmiskoulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: schema.Koodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus

@Title("Oppivelvollisille suunnattu vapaan sivistyön koulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("vstoppivelvollisillesuunnattukoulutus")
  tyyppi: schema.Koodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus

@Title("Vapaan sivistystyön JOTPA-koulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("vstjotpakoulutus")
  tyyppi: schema.Koodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus

@Title("Vapaan sivistystyön lukutaitokoulutuksen suoritus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus,
  vahvistus: Option[Vahvistus],
  suorituskieli: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("vstlukutaitokoulutus")
  tyyppi: schema.Koodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus

@Title("Vapaan sivistystyön maahanmuuttajien kotoutusmiskoulutus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli

@Title("Oppivelvollisille suunnattu vapaan sivistystyön koulutus")
case class AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli

@Title("Vapaan sivistystyön JOTPA-koulutus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli

@Title("Vapaan sivistystyön lukutaitokoulutus")
case class AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutus(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli
