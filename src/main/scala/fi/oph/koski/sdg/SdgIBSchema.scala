package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{OnlyWhen, Title}

import java.time.LocalDate

@Title("IB-tutkinnon opiskeluoikeus")
case class SdgIBOpiskeluoikeus(
  oid: Option[String] = None,
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija] = None,
  tila: OpiskeluoikeudenTila,
  suoritukset: List[IBPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgIBOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: IBPäätasonSuoritus => s }
    )
}

trait IBPäätasonSuoritus extends Suoritus

@Title("IB-koulutuksen suoritus")
case class SdgIBTutkinnonSuoritus(
  koulutusmoduuli: schema.IBTutkinto = schema.IBTutkinto(),
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[IBTutkinnonOppiaineenSuoritus]],
  theoryOfKnowledge: Option[schema.IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[schema.IBExtendedEssaySuoritus],
  creativityActionService: Option[schema.IBCASSuoritus],
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends IBPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgIBTutkinnonSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: IBTutkinnonOppiaineenSuoritus => s
      })
    )
}

trait IBTutkinnonOppiaineenSuoritus extends Osasuoritus

@Title("IB-oppiaineen suoritus")
case class SdgIBOppiaineenSuoritus(
  koulutusmoduuli: schema.IBAineRyhmäOppiaine,
  arviointi: Option[List[SdgLukionArviointi]] = None, // TODO: predicted ja effort pois
  predictedArviointi: Option[List[schema.IBOppiaineenPredictedArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @Title("Kurssit")
  osasuoritukset: Option[List[SdgIBKurssinSuoritus]],
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends IBTutkinnonOppiaineenSuoritus

case class SdgIBKurssinSuoritus(
  koulutusmoduuli: schema.IBKurssi,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ibkurssi")
  tyyppi: schema.Koodistokoodiviite
)

@Title("IB-lukion DP Core -suoritus")
case class SdgIBDPCoreSuoritus(
  koulutusmoduuli: schema.IBDPCoreOppiaine,
  arviointi: Option[List[SdgLukionArviointi]] = None, // TODO: predicted ja effort pois
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  osasuoritukset: Option[List[SdgIBCoreKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("ibcore")
  tyyppi: schema.Koodistokoodiviite
) extends IBTutkinnonOppiaineenSuoritus

@Title("IB-Core-kurssin suoritus")
case class SdgIBCoreKurssinSuoritus(
  koulutusmoduuli: schema.IBCoreKurssi,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ibcorekurssi")
  tyyppi: schema.Koodistokoodiviite
)

case class SdgPreIBSuoritus2015(
  @Title("Koulutus")
  koulutusmoduuli: schema.PreIBKoulutusmoduuli2015,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[PreIBSuorituksenOsasuoritus2015]],
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: schema.Koodistokoodiviite
) extends IBPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgPreIBSuoritus2015 =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: PreIBSuorituksenOsasuoritus2015 => s
      })
    )
}

trait PreIBSuorituksenOsasuoritus2015 extends Osasuoritus

case class SdgPreIBOppiaineenSuoritus2015(
  @Title("Oppiaine")
  koulutusmoduuli: schema.PreIBOppiaine2015,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @Title("Kurssit")
  osasuoritukset: Option[List[SdgPreIBKurssinSuoritus2015]],
  @KoodistoKoodiarvo("preiboppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends PreIBSuorituksenOsasuoritus2015

case class SdgPreIBKurssinSuoritus2015(
  koulutusmoduuli: schema.PreIBKurssi2015,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("preibkurssi")
  tyyppi: schema.Koodistokoodiviite
) extends PreIBSuorituksenOsasuoritus2015

@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "preiboppimaara2019")
case class SdgPreIBSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: schema.PreIBKoulutusmoduuli2019,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  omanÄidinkielenOpinnot: Option[SdgLukionOmanÄidinkielenOpinnot] = None,
  puhviKoe: Option[SdgLukionArviointi],
  suullisenKielitaidonKokeet: Option[List[SdgSuullisenKielitaidonKoe2019]] = None,
  osasuoritukset: Option[List[PreIBSuorituksenOsasuoritus2019]],
  @KoodistoKoodiarvo("preiboppimaara")
  tyyppi: schema.Koodistokoodiviite,
)  extends IBPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgPreIBSuoritus2019 =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: PreIBSuorituksenOsasuoritus2019 => s
      })
    )
}

case class SdgLukionOmanÄidinkielenOpinnot(
  arvosana: schema.Koodistokoodiviite,
  arviointipäivä: Option[LocalDate],
  kieli: schema.Koodistokoodiviite,
  laajuus: schema.LaajuusOpintopisteissä,
  osasuoritukset: Option[List[SdgLukionOmanÄidinkielenOpintojenOsasuoritus]],
)

case class SdgLukionOmanÄidinkielenOpintojenOsasuoritus (
  tyyppi: schema.Koodistokoodiviite,
  @Title("Kurssi")
  koulutusmoduuli: schema.LukionOmanÄidinkielenOpinto,
  @KoodistoUri("kieli")
  suorituskieli: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[SdgLukionArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends WithTunnustettuBoolean

case class SdgSuullisenKielitaidonKoe2019(
  @KoodistoUri("kielivalikoima")
  kieli: schema.Koodistokoodiviite,
  arvosana: schema.Koodistokoodiviite,
  @KoodistoUri("arviointiasteikkokehittyvankielitaidontasot")
  taitotaso: schema.Koodistokoodiviite,
  päivä: LocalDate
)

trait PreIBSuorituksenOsasuoritus2019 extends Osasuoritus

case class SdgLukionOppiaineenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: schema.PreIBLukionOppiaine2019,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  osasuoritukset: Option[List[PreIBLukioOpintojenOsasuoritus]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends PreIBSuorituksenOsasuoritus2019

case class SdgMuidenLukioOpintojenPreIBSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: schema.PreIBMuutSuorituksetTaiVastaavat2019,
  osasuoritukset: Option[List[MuidenLukioOpintojenOsasuoritus2019]],
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: schema.Koodistokoodiviite
) extends PreIBSuorituksenOsasuoritus2019

trait PreIBLukioOpintojenOsasuoritus extends WithTunnustettuBoolean
trait PreIBMuidenLukioOpintojenOsasuoritus2019 extends WithTunnustettuBoolean

@Title("Pre-IB lukion moduulin suoritus oppiaineissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionoppiaine")
case class SdgPreIBLukionModuulinSuoritusOppiaineissa2019(
  @Title("Osasuoritus")
  koulutusmoduuli: schema.PreIBLukionModuuliOppiaineissa2019,
  arviointi: Option[List[SdgLukionArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends PreIBLukioOpintojenOsasuoritus

@Title("Pre-IB lukion paikallisen opintojakson suoritus 2019")
case class SdgPreIBLukionPaikallisenOpintojaksonSuoritus2019(
  koulutusmoduuli: schema.PreIBPaikallinenOpintojakso2019,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("lukionpaikallinenopintojakso")
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends PreIBLukioOpintojenOsasuoritus
  with PreIBMuidenLukioOpintojenOsasuoritus2019

@Title("Pre-IB lukion moduulin suoritus muissa opinnoissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionmuuopinto")
case class SdgPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(
  koulutusmoduuli: schema.PreIBLukionModuuliMuissaOpinnoissa2019,
  arviointi: Option[List[SdgLukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends PreIBMuidenLukioOpintojenOsasuoritus2019
