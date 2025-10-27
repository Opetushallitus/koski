package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class DIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[DiaPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): DIAOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: DIATutkinnonSuoritus => s }
    )
}

trait DiaPäätasonSuoritus extends Suoritus

@Title("DIA-tutkinnon suoritus")
case class DIATutkinnonSuoritus(
  koulutusmoduuli: schema.DIATutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[DIAOppiaineenTutkintovaiheenSuoritus]],
  suorituskieli: schema.Koodistokoodiviite,
  kokonaispistemäärä: Option[Int],
  lukukausisuoritustenKokonaispistemäärä: Option[Int],
  tutkintoaineidenKokonaispistemäärä: Option[Int],
  kokonaispistemäärästäJohdettuKeskiarvo: Option[Double],
) extends DiaPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): DIATutkinnonSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: DIAOppiaineenTutkintovaiheenSuoritus => s
      })
    )
}

// DIAssa vain vahvistetut vipu: pitää löytyä vahvistettu DIA-tutkinnon suoritus, valmistavalla ei väliä
@Title("DIA valmistavan vaiheen suoritus")
case class DIAValmistavanVaiheenSuoritus(
  koulutusmoduuli: schema.DIAValmistavaVaihe,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[DIAOppiaineenValmistavanVaiheenSuoritus]],
  @KoodistoKoodiarvo("diavalmistavavaihe")
  tyyppi: schema.Koodistokoodiviite
) extends DiaPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): DIAValmistavanVaiheenSuoritus =
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
  osasuoritukset: Option[List[DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus]],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

@Title("DIA-oppiaineen valmistavan vaiheen lukukauden suoritus")
case class DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(
  koulutusmoduuli: schema.DIAOppiaineenValmistavanVaiheenLukukausi,
  arviointi: Option[List[schema.DIAOppiaineenValmistavanVaiheenLukukaudenArviointi]],
  @KoodistoKoodiarvo("diaoppiaineenvalmistavanvaiheenlukukaudensuoritus")
  tyyppi: schema.Koodistokoodiviite
)

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class DIAOppiaineenTutkintovaiheenSuoritus(
  koulutusmoduuli: schema.DIAOppiaine,
  suorituskieli: Option[schema.Koodistokoodiviite],
  koetuloksenNelinkertainenPistemäärä: Option[Int],
  vastaavuustodistuksenTiedot: Option[schema.DIAVastaavuustodistuksenTiedot],
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus]],
) extends Osasuoritus


@Title("DIA-oppiaineen tutkintovaiheen osasuorituksen suoritus")
case class DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  koulutusmoduuli: schema.DIAOppiaineenTutkintovaiheenOsasuoritus,
  arviointi: Option[List[schema.DIATutkintovaiheenArviointi]],
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus
