package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("European School of Helsingin opiskeluoikeus")
case class KelaESHOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTilaRahoitustiedoilla,
  suoritukset: List[KelaESHPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  lisätiedot: Option[KelaESHOpiskeluoikeudenLisätiedot],
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
) extends KelaOpiskeluoikeus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaESHOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

@Title("European School of Helsingin päätason suoritus")
trait KelaESHPäätasonSuoritus extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHPäätasonSuoritus
}

@Title("Secondary lower vuosiluokan suoritus")
case class KelaESHSecondaryLowerVuosiluokanSuoritus(
  alkamispäivä: Option[LocalDate] = None,
  koulutusmoduuli: KelaESHSecondaryLuokkaAste,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  tyyppi: schema.Koodistokoodiviite,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  jääLuokalle: Option[Boolean],
  osasuoritukset: Option[List[KelaESHSecondaryLowerOppiaineenSuoritus]],
) extends KelaESHPäätasonSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHSecondaryLowerVuosiluokanSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
  )
}

@Title("Secondary lower oppiaineen suoritus")
case class KelaESHSecondaryLowerOppiaineenSuoritus(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Boolean,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: schema.Koodistokoodiviite,
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHSecondaryLowerOppiaineenSuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Secondary upper vuosiluokan suoritus")
case class KelaESHSecondaryUpperVuosiluokanSuoritus(
  alkamispäivä: Option[LocalDate] = None,
  koulutusmoduuli: KelaESHSecondaryLuokkaAste,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  tyyppi: schema.Koodistokoodiviite,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  jääLuokalle: Option[Boolean],
  osasuoritukset: Option[List[KelaESHSecondaryUpperOppiaineenSuoritus]],
) extends KelaESHPäätasonSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHSecondaryUpperVuosiluokanSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
  )
}

@Title("Secondary luokka-aste")
case class KelaESHSecondaryLuokkaAste(
  tunniste: KelaKoodistokoodiviite,
  curriculum: KelaKoodistokoodiviite,
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

@Title("Secondary upper oppiaineen suoritus")
trait KelaESHSecondaryUpperOppiaineenSuoritus extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHSecondaryUpperOppiaineenSuoritus
}

@Title("Secondary grade -oppiaine")
case class KelaESHSecondaryUpperOppiaineenSuoritusS6(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Boolean,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss6")
  tyyppi: schema.Koodistokoodiviite,
) extends KelaESHSecondaryUpperOppiaineenSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHSecondaryUpperOppiaineenSuoritusS6 = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Secondary upper vuosiluokan suoritus")
case class KelaESHSecondaryUpperOppiaineenSuoritusS7(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Boolean,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss7")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[KelaESHS7OppiaineenAlaosasuoritus]],
) extends KelaESHSecondaryUpperOppiaineenSuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHSecondaryUpperOppiaineenSuoritusS7 = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
  )
}

@Title("Secondary grade oppiaine")
trait KelaESHSecondaryGradeOppiaine extends SuorituksenKoulutusmoduuli

@Title("European School of Helsingin kielioppiaine")
case class KelaESHKielioppiaine(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  kieli: KelaKoodistokoodiviite,
) extends KelaESHSecondaryGradeOppiaine

@Title("European School of Helsingin muu oppiaine")
case class KelaESHMuuOppiaine(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
) extends KelaESHSecondaryGradeOppiaine

@Title("European School of Helsingin S7-luokan oppiaineen alaosasuoritus")
case class KelaESHS7OppiaineenAlaosasuoritus(
  koulutusmoduuli: KelaESHS7Oppiainekomponentti,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuorituss7")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus {
  def osasuoritukset: Option[List[Osasuoritus]] = None
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHS7OppiaineenAlaosasuoritus = this.copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("European School of Helsingin S7-luokan oppiainekomponentti")
case class KelaESHS7Oppiainekomponentti(
  tunniste: KelaKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

case class KelaESHArviointi(
  arvosana: Option[schema.Koodistokoodiviite],
  päivä: Option[LocalDate],
  hyväksytty: Option[Boolean],
) extends OsasuorituksenArviointi {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaESHArviointi = this.copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.EuropeanSchoolOfHelsinkiArviointi.hyväksytty)
  )
}

@Title("European School of Helsingin lisätiedot")
case class KelaESHOpiskeluoikeudenLisätiedot(
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]],
) extends OpiskeluoikeudenLisätiedot
