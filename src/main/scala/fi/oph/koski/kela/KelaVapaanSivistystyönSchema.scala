package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Vapaan sivistystyön opiskeluoikeus")
@Description("Vapaan sivistystyön koulutuksen opiskeluoikeus")
case class KelaVapaanSivistystyönOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaVapaansivistystyönOpiskeluoikeudenTila,
  suoritukset: List[VstSuoritus],
  lisätiedot: Option[KelaVapaanSivistystyönOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaVapaansivistystyönOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaOpiskeluoikeusjaksoRahoituksella]
) extends OpiskeluoikeudenTila

case class KelaVapaanSivistystyönOpiskeluoikeudenLisätiedot(
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Vapaan sivistystyön suoritus")
case class KelaVapaanSivistystyönPäätasonSuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönOsasuoritus]],
  @KoodistoKoodiarvo("vstoppivelvollisillesuunnattukoulutus")
  @KoodistoKoodiarvo("vstlukutaitokoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends VstSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen suoritus")
case class KelaVapaanSivistystyönMaahanmuuttajienKotoutumisenSuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends VstSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönMaahanmuuttajienKotoutumisenSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen suoritus")
case class KelaVapaanSivistystyönJotpaSuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönJotpaKoulutus,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönJotpaOsasuoritus]],
  @KoodistoKoodiarvo("vstjotpakoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends VstSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönJotpaSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaVapaanSivistystyönJotpaKoulutus(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  opintokokonaisuus: KelaKoodistokoodiviite
) extends SuorituksenKoulutusmoduuli

@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen osasuoritus")
case class KelaVapaanSivistystyönJotpaOsasuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaVSTOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaVapaanSivistystyönJotpaOsasuoritus]],
  @KoodistoKoodiarvo("vstjotpakoulutuksenosasuoritus")
  tyyppi: schema.Koodistokoodiviite,
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönJotpaOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

trait VstSuoritus extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: VstSuoritus
}

@Title("Vapaan sivistystyön osasuoritus")
case class KelaVapaanSivistystyönOsasuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaVSTOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaVapaanSivistystyönOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus")
case class KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi]],
  osasuoritukset: Option[List[KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaVapaanSivistystyönSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
) extends OsasuorituksenKoulutusmoduuli


case class KelaVSTOsasuorituksenArviointi (
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaVSTOsasuorituksenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.VapaanSivistystyönKoulutuksenArviointi.hyväksytty)
  )
}

case class VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi (
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate],
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.VapaanSivistystyönKoulutuksenArviointi.hyväksytty),
  )
}

case class VSTKielenTaitotasonArviointi(
  taso: KelaKoodistokoodiviite
)

@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", schema.VSTKoto2022Peruste.diaarinumero)
case class KelaVSTKOTO2022Suoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends VstSuoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: VstSuoritus = this.copy(
    osasuoritukset = this.osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
  )
}
