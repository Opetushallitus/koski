package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoKoodiarvo}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, VSTKoto2022Peruste}
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
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  def withEmptyArvosana: KelaVapaanSivistystyönOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaVapaansivistystyönOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaVapaanSivistystyönOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KelaVapaanSivistystyönOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite,
  @KoodistoKoodiarvo("14")
  @KoodistoKoodiarvo("15")
  opintojenRahoitus: Option[KelaKoodistokoodiviite],
) extends Opiskeluoikeusjakso

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
  tila: Option[KelaKoodistokoodiviite],
) extends VstSuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
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
  tila: Option[KelaKoodistokoodiviite],
) extends VstSuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönMaahanmuuttajienKotoutumisenSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen suoritus")
case class KelaVapaanSivistystyönJotpaSuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönJotpaOsasuoritus]],
  @KoodistoKoodiarvo("vstjotpakoulutus")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends VstSuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönJotpaSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutuksen osasuoritus")
case class KelaVapaanSivistystyönJotpaOsasuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaVSTOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaVapaanSivistystyönJotpaOsasuoritus]],
  @KoodistoKoodiarvo("vstjotpakoulutuksenosasuoritus")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends Osasuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönJotpaOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

trait VstSuoritus extends Suoritus {
  def withEmptyArvosana: VstSuoritus
}

@Title("Vapaan sivistystyön osasuoritus")
case class KelaVapaanSivistystyönOsasuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaVSTOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaVapaanSivistystyönOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends Osasuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus")
case class KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi]],
  osasuoritukset: Option[List[KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends Osasuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
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
  def withEmptyArvosana: KelaVSTOsasuorituksenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.VapaanSivistystyönKoulutuksenArviointi.hyväksytty)
  )
}

case class VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi (
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate],
  @Deprecated("Poistettu palautettavien tietojen joukosta")
  kuullunYmmärtämisenTaitotaso: Option[VSTKielenTaitotasonArviointi],
  @Deprecated("Poistettu palautettavien tietojen joukosta")
  puhumisenTaitotaso: Option[VSTKielenTaitotasonArviointi],
  @Deprecated("Poistettu palautettavien tietojen joukosta")
  luetunYmmärtämisenTaitotaso: Option[VSTKielenTaitotasonArviointi],
  @Deprecated("Poistettu palautettavien tietojen joukosta")
  kirjoittamisenTaitotaso: Option[VSTKielenTaitotasonArviointi]
) extends OsasuorituksenArviointi {
  def withEmptyArvosana: VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.VapaanSivistystyönKoulutuksenArviointi.hyväksytty),
    kuullunYmmärtämisenTaitotaso = None,
    puhumisenTaitotaso = None,
    luetunYmmärtämisenTaitotaso = None,
    kirjoittamisenTaitotaso = None,
  )
}

case class VSTKielenTaitotasonArviointi(
  taso: KelaKoodistokoodiviite
)

@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", VSTKoto2022Peruste.diaarinumero)
case class KelaVSTKOTO2022Suoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli, // TODO:
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends VstSuoritus {
  override def withEmptyArvosana: VstSuoritus = this.copy(
    osasuoritukset = this.osasuoritukset.map(_.map(_.withEmptyArvosana)),
  )
}
