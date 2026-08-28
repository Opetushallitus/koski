package fi.oph.koski.sdg

import fi.oph.koski.schema
import java.time.LocalDate
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

@Title("European School of Helsinki -opiskeluoikeus")
case class SdgEuropeanSchoolOfHelsinkiOpiskeluoikeus(
  oid: Option[String] = None,
  oppilaitos: Option[schema.Oppilaitos] = None,
  koulutustoimija: Option[schema.Koulutustoimija] = None,
  tila: SdgOpiskeluoikeudenTila,
  suoritukset: List[SdgEuropeanSchoolOfHelsinkiPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: schema.Koodistokoodiviite = schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki,
) extends SdgOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[SdgSuoritus]): SdgOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgEuropeanSchoolOfHelsinkiPäätasonSuoritus => s }
    )
}

trait SdgEuropeanSchoolOfHelsinkiPäätasonSuoritus extends SdgSuoritus

// VAIN S5 MUKAAN
@Title("Secondary Lower -vuosiluokan suoritus")
case class SdgSecondaryLowerVuosiluokanSuoritus(
  koulutusmoduuli: schema.SecondaryLowerLuokkaAste,
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgSecondaryLowerOppiaineenSuoritus]] = None
) extends SdgEuropeanSchoolOfHelsinkiPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgSecondaryLowerVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgSecondaryLowerOppiaineenSuoritus => s
      })
    )
}

@Title("Secondary Upper -vuosiluokan suoritus")
case class SdgSecondaryUpperVuosiluokanSuoritus(
  koulutusmoduuli: schema.SecondaryUpperLuokkaAste,
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgSecondaryUpperOppiaineenSuoritus]] = None
) extends SdgEuropeanSchoolOfHelsinkiPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgSecondaryUpperVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgSecondaryUpperOppiaineenSuoritus => s
      })
    )
}

@Title("Secondary lower oppiaineen suoritus")
case class SdgSecondaryLowerOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  arviointi: Option[List[SdgSecondaryNumericalMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite
) extends Osasuoritus

trait SdgSecondaryUpperOppiaineenSuoritus extends Osasuoritus

@Title("Secondary upper oppiaineen suoritus S6")
@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S6")
case class SdgSecondaryUpperOppiaineenSuoritusS6(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  arviointi: Option[List[SdgSecondaryNumericalMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss6")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite,
) extends SdgSecondaryUpperOppiaineenSuoritus

@Title("Secondary numerical mark arviointi")
case class SdgSecondaryNumericalMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkinumericalmark")
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate],
)

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S7")
@Title("Secondary upper oppiaineen suoritus S7")
case class SdgSecondaryUpperOppiaineenSuoritusS7(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss7")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgS7OppiaineenAlaosasuoritus]] = None
) extends SdgSecondaryUpperOppiaineenSuoritus

@Title("S7 oppiaineen alaosasuoritus")
case class SdgS7OppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: schema.S7OppiaineKomponentti,
  arviointi: Option[List[SdgSecondaryS7PreliminaryMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuorituss7")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

@Title("Secondary S7 preliminary mark arviointi")
case class SdgSecondaryS7PreliminaryMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark")
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate],
)
