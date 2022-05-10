package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

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
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[VstSuoritus],
  lisätiedot: Option[KelaVapaanSivistystyönOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def sisältyyOpiskeluoikeuteen: Option[Sisältäväopiskeluoikeus] = None
  def withEmptyArvosana: KelaVapaanSivistystyönOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
}

case class KelaVapaanSivistystyönOpiskeluoikeudenLisätiedot(
  maksuttomuus: Option[List[schema.Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[schema.OikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Vapaan sivistystyön suoritus")
case class KelaVapaanSivistystyönPäätasonSuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends VstSuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen suoritus")
case class KelaVapaanSivistystyönMaahanmuuttajienKototutumisenSuoritus(
  koulutusmoduuli: KelaVapaanSivistystyönSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaVapaanSivistystyönMaahanmuuttajienKototutumisenOsasuoritus]],
  @KoodistoKoodiarvo("vstmaahanmuuttajienkotoutumiskoulutus")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends VstSuoritus {
  def withEmptyArvosana: KelaVapaanSivistystyönMaahanmuuttajienKototutumisenSuoritus = copy(
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
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: KelaVSTOsasuorituksenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.VapaanSivistystyönKoulutuksenArviointi.hyväksytty)
  )
}

case class VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi (
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate],
  kuullunYmmärtämisenTaitotaso: Option[VSTKielenTaitotasonArviointi],
  puhumisenTaitotaso: Option[VSTKielenTaitotasonArviointi],
  luetunYmmärtämisenTaitotaso: Option[VSTKielenTaitotasonArviointi],
  kirjoittamisenTaitotaso: Option[VSTKielenTaitotasonArviointi]
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: VSTMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.VapaanSivistystyönKoulutuksenArviointi.hyväksytty)
  )
}

case class VSTKielenTaitotasonArviointi(
  taso: KelaKoodistokoodiviite
)
