package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.DIAVastaavuustodistuksenTiedot
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class SdgDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: schema.DIAOpiskeluoikeudenTila,
  suoritukset: List[SdgDiaPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SdgKoskeenTallennettavaOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgDIATutkinnonSuoritus => s }
    )
}

trait SdgDiaPäätasonSuoritus extends Suoritus

@Title("DIA-tutkinnon suoritus")
case class SdgDIATutkinnonSuoritus(
  koulutusmoduuli: schema.DIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDIAOppiaineenTutkintovaiheenSuoritus]],
  suorituskieli: schema.Koodistokoodiviite,
  kokonaispistemäärä: Option[Int] = None,
  lukukausisuoritustenKokonaispistemäärä: Option[Int] = None,
  tutkintoaineidenKokonaispistemäärä: Option[Int] = None,
  kokonaispistemäärästäJohdettuKeskiarvo: Option[Double] = None,
) extends SdgDiaPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgDIATutkinnonSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgDIAOppiaineenTutkintovaiheenSuoritus => s
      })
    )
}

// DIAssa vain vahvistetut vipu: pitää löytyä vahvistettu DIA-tutkinnon suoritus, valmistavalla ei väliä
@Title("DIA valmistavan vaiheen suoritus")
case class SdgDIAValmistavanVaiheenSuoritus(
  koulutusmoduuli: schema.DIAValmistavaVaihe,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[DIAOppiaineenValmistavanVaiheenSuoritus]],
  @KoodistoKoodiarvo("diavalmistavavaihe")
  tyyppi: schema.Koodistokoodiviite
) extends SdgDiaPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgDIAValmistavanVaiheenSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: DIAOppiaineenValmistavanVaiheenSuoritus => s
      })
    )
}

@Title("DIA-oppiaineen valmistavan vaiheen suoritus")
case class DIAOppiaineenValmistavanVaiheenSuoritus(
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
  arviointi: Option[List[schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]] = None,
  @KoodistoKoodiarvo("diaoppiaineenvalmistavanvaiheenlukukaudensuoritus")
  tyyppi: schema.Koodistokoodiviite
)

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class SdgDIAOppiaineenTutkintovaiheenSuoritus(
  koulutusmoduuli: schema.DIAOppiaine,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  vastaavuustodistuksenTiedot: Option[DIAVastaavuustodistuksenTiedot] = None,
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus]],
) extends Osasuoritus


@Title("DIA-oppiaineen tutkintovaiheen osasuorituksen suoritus")
case class SdgDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  koulutusmoduuli: schema.DIAOppiaineenTutkintovaiheenOsasuoritus,
  arviointi: Option[List[schema.DIATutkintovaiheenArviointi]] = None,
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: schema.Koodistokoodiviite = schema.Koodistokoodiviite(koodiarvo = "diaoppiaineentutkintovaiheenosasuorituksensuoritus", koodistoUri = "suorituksentyyppi")
) extends Osasuoritus
