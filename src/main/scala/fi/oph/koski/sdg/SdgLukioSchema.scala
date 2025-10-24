package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{NotWhen, OnlyWhen, SkipSerialization, Title}

import java.time.LocalDate

@Title("Lukion opiskeluoikeus")
case class LukioOpiskeluoikeus(
  oid: Option[String] = None,
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SdgLukionPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  oppimääräSuoritettu: Option[Boolean] = None,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgLukionPäätasonSuoritus => s }
    )
}

trait SdgLukionPäätasonSuoritus extends Suoritus

@Title("Lukion oppimäärä 2015")
@NotWhen("koulutusmoduuli/perusteenDiaarinumero", List("OPH-2263-2019", "OPH-2267-2019"))
case class LukionOppimääränSuoritus2015(
  koulutusmoduuli: schema.LukionOppimäärä,
  @KoodistoUri("lukionoppimaara")
  @Title("Opetussuunnitelma")
  oppimäärä: schema.Koodistokoodiviite,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus] = None,
  suorituskieli: schema.Koodistokoodiviite,
  omanÄidinkielenOpinnot: Option[LukionOmanÄidinkielenOpinnot],
  @Title("Oppiaineet")
  osasuoritukset: Option[List[LukionOppimääränOsasuoritus2015]],
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: schema.Koodistokoodiviite
) extends SdgLukionPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): LukionOppimääränSuoritus2015 =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: LukionOppimääränOsasuoritus2015 => s
      })
    )
}

trait LukionOppimääränOsasuoritus2015 extends Osasuoritus

case class LukionOppiaineenSuoritus2015(
  koulutusmoduuli: schema.LukionOppiaine2015,
  arviointi: Option[List[LukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite],
  @Title("Kurssit")
  osasuoritukset: Option[List[LukionOsasuoritus2015]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends LukionOppimääränOsasuoritus2015

case class MuidenLukioOpintojenSuoritus2015(
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: schema.Koodistokoodiviite,
  arviointi: Option[List[LukionArviointi]] = None,
  koulutusmoduuli: schema.MuuLukioOpinto2015,
  @Title("Kurssit")
  osasuoritukset: Option[List[LukionOsasuoritus2015]]
) extends LukionOppimääränOsasuoritus2015

trait LukionOsasuoritus2015 extends WithTunnustettuBoolean

@Title("Lukion kurssin suoritus")
case class LukionKurssinSuoritus2015(
  koulutusmoduuli: schema.LukionKurssi2015,
  arviointi: Option[List[LukionArviointi]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionkurssi")
  tyyppi: schema.Koodistokoodiviite,
  suoritettuLukiodiplomina: Option[Boolean] = None,
  suoritettuSuullisenaKielikokeena: Option[Boolean] = None,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends LukionOsasuoritus2015

@Title("Lukion oppimäärän suoritus 2019")
@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", "OPH-2263-2019")
@OnlyWhen("koulutusmoduuli/perusteenDiaarinumero", "OPH-2267-2019")
case class LukionOppimääränSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: schema.LukionOppimäärä,
  oppimäärä: schema.Koodistokoodiviite,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @Title("Opetuskieli")
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Lukion oppimäärää täydentävät oman äidinkielen opinnot")
  omanÄidinkielenOpinnot: Option[schema.LukionOmanÄidinkielenOpinnot],
  puhviKoe: Option[LukionArviointi],
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]],
  lukiodiplomit2019: Option[List[LukionArviointi]],
  osasuoritukset: Option[List[LukionOppimääränOsasuoritus2019]],
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: schema.Koodistokoodiviite,
) extends SdgLukionPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): LukionOppimääränSuoritus2019 =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: LukionOppimääränOsasuoritus2019 => s
      })
    )
}

trait LukionOppimääränOsasuoritus2019 extends Osasuoritus

@Title("Lukion oppiaine 2019")
case class LukionOppiaineenSuoritus2019(
  koulutusmoduuli: schema.LukionOppiaine2019,
  arviointi: Option[List[LukionArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[LukionOppiaineenOsasuoritus2019]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends LukionOppimääränOsasuoritus2019

trait LukionOppiaineenOsasuoritus2019 extends WithTunnustettuBoolean

@Title("Muiden lukion opintojen suoritus 2019")
case class MuidenLukioOpintojenSuoritus2019(
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: schema.MuutSuorituksetTaiVastaavat2019,
  osasuoritukset: Option[List[MuidenLukioOpintojenOsasuoritus2019]]
) extends LukionOppimääränOsasuoritus2019

trait MuidenLukioOpintojenOsasuoritus2019 extends WithTunnustettuBoolean

@Title("Lukion moduulin suoritus oppiaineissa 2019")
@OnlyWhen("../../tyyppi/koodiarvo", "lukionoppiaine")
@OnlyWhen("../../tyyppi/koodiarvo", "luvalukionoppiaine2019")
case class LukionModuulinSuoritusOppiaineissa2019(
  koulutusmoduuli: schema.LukionModuuliOppiaineissa2019,
  arviointi: Option[List[LukionArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionvaltakunnallinenmoduuli")
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends LukionOppiaineenOsasuoritus2019

@Title("Lukion moduulin suoritus muissa opinnoissa 2019")
case class LukionModuulinSuoritusMuissaOpinnoissa2019(
  koulutusmoduuli: schema.LukionModuuliMuissaOpinnoissa2019,
  arviointi: Option[List[LukionArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends MuidenLukioOpintojenOsasuoritus2019

@Title("Lukion paikallisen opintojakson suoritus 2019")
case class LukionPaikallisenOpintojaksonSuoritus2019(
  @Title("Paikallinen opintojakso")
  koulutusmoduuli: schema.LukionPaikallinenOpintojakso2019,
  arviointi: Option[List[LukionArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionpaikallinenopintojakso")
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[schema.OsaamisenTunnustaminen]
) extends LukionOppiaineenOsasuoritus2019
  with MuidenLukioOpintojenOsasuoritus2019

case class LukionArviointi (
  arvosana: schema.Koodistokoodiviite,
  päivä: Option[LocalDate]
)
