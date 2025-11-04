package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class SdgDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[DiaPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgDIAOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: DiaPäätasonSuoritus => s }
    )
}

trait DiaPäätasonSuoritus extends Suoritus

@Title("DIA-tutkinnon suoritus")
case class SdgDIATutkinnonSuoritus(
  koulutusmoduuli: schema.DIATutkinto,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDIAOppiaineenTutkintovaiheenSuoritus]],
  suorituskieli: schema.Koodistokoodiviite,
  kokonaispistemäärä: Option[Int],
  lukukausisuoritustenKokonaispistemäärä: Option[Int],
  tutkintoaineidenKokonaispistemäärä: Option[Int],
  kokonaispistemäärästäJohdettuKeskiarvo: Option[Double],
) extends DiaPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgDIATutkinnonSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgDIAOppiaineenTutkintovaiheenSuoritus => s
      })
    )
}

// TODO: DIAssa vain vahvistetut vipu: pitää löytyä vahvistettu DIA-tutkinnon suoritus, valmistavalla ei väliä
@Title("DIA valmistavan vaiheen suoritus")
case class SdgDIAValmistavanVaiheenSuoritus(
  koulutusmoduuli: schema.DIAValmistavaVaihe = schema.DIAValmistavaVaihe(),
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDIAOppiaineenValmistavanVaiheenSuoritus]],
  @KoodistoKoodiarvo("diavalmistavavaihe")
  tyyppi: schema.Koodistokoodiviite
) extends DiaPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgDIAValmistavanVaiheenSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgDIAOppiaineenValmistavanVaiheenSuoritus => s
      })
    )
}

@Title("DIA-oppiaineen valmistavan vaiheen suoritus")
case class SdgDIAOppiaineenValmistavanVaiheenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: schema.DIAOppiaine,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  osasuoritukset: Option[List[SdgDIAOppiaineenValmistavanVaiheenLukukaudenSuoritus]],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

@Title("DIA-oppiaineen valmistavan vaiheen lukukauden suoritus")
case class SdgDIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(
  koulutusmoduuli: schema.DIAOppiaineenValmistavanVaiheenLukukausi,
  arviointi: Option[List[schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]],
  @KoodistoKoodiarvo("diaoppiaineenvalmistavanvaiheenlukukaudensuoritus")
  tyyppi: schema.Koodistokoodiviite
)

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class SdgDIAOppiaineenTutkintovaiheenSuoritus(
  koulutusmoduuli: schema.DIAOppiaine,
  suorituskieli: Option[schema.Koodistokoodiviite],
  koetuloksenNelinkertainenPistemäärä: Option[Int],
  vastaavuustodistuksenTiedot: Option[schema.DIAVastaavuustodistuksenTiedot],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus]],
) extends Osasuoritus


@Title("DIA-oppiaineen tutkintovaiheen osasuorituksen suoritus")
case class SdgDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  koulutusmoduuli: schema.DIAOppiaineenTutkintovaiheenOsasuoritus,
  arviointi: Option[List[schema.DIATutkintovaiheenArviointi]],
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus
