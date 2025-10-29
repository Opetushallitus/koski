package fi.oph.koski.sdg

import fi.oph.koski.schema
import java.time.LocalDate
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

@Title("European School of Helsinki -opiskeluoikeus")
case class EuropeanSchoolOfHelsinkiOpiskeluoikeus(
  oid: Option[String] = None,
  oppilaitos: Option[schema.Oppilaitos] = None,
  koulutustoimija: Option[schema.Koulutustoimija] = None,
  tila: OpiskeluoikeudenTila,
  suoritukset: List[EuropeanSchoolOfHelsinkiPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: schema.Koodistokoodiviite = schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: EuropeanSchoolOfHelsinkiPäätasonSuoritus => s }
    )
}

trait EuropeanSchoolOfHelsinkiPäätasonSuoritus extends Suoritus

// VAIN S5 MUKAAN
@Title("Secondary Lower -vuosiluokan suoritus")
case class SecondaryLowerVuosiluokanSuoritus(
  koulutusmoduuli: schema.SecondaryLowerLuokkaAste,
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SecondaryLowerOppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SecondaryLowerVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SecondaryLowerOppiaineenSuoritus => s
      })
    )
}

@Title("Secondary Upper -vuosiluokan suoritus")
case class SecondaryUpperVuosiluokanSuoritus(
  koulutusmoduuli: schema.SecondaryUpperLuokkaAste,
  alkamispäivä: Option[LocalDate] = None,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SecondaryUpperOppiaineenSuoritus]] = None
) extends EuropeanSchoolOfHelsinkiPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SecondaryUpperVuosiluokanSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SecondaryUpperOppiaineenSuoritus => s
      })
    )
}

case class SecondaryLowerOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite
) extends Osasuoritus

trait SecondaryUpperOppiaineenSuoritus extends Osasuoritus

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S6")
case class SecondaryUpperOppiaineenSuoritusS6(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss6")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite,
) extends SecondaryUpperOppiaineenSuoritus

case class SecondaryNumericalMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkinumericalmark")
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate],
)

@OnlyWhen("../../koulutusmoduuli/tunniste/koodiarvo", "S7")
case class SecondaryUpperOppiaineenSuoritusS7(
  @Title("Oppiaine")
  koulutusmoduuli: schema.SecondaryOppiaine,
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss7")
  tyyppi: schema.Koodistokoodiviite,
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[S7OppiaineenAlaosasuoritus]] = None
) extends SecondaryUpperOppiaineenSuoritus

case class S7OppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: schema.S7OppiaineKomponentti,
  arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None,
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuorituss7")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

case class SecondaryS7PreliminaryMarkArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark")
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate],
)
