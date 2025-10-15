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
  tila: schema.DIAOpiskeluoikeudenTila,
  suoritukset: List[SdgDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SdgKoskeenTallennettavaOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgDIATutkinnonSuoritus => s }
    )
}

// valmistava dia mukaan
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
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgDIATutkinnonSuoritus =
    this.copy(
      osasuoritukset = os.map(_.collect{
        case s: SdgDIAOppiaineenTutkintovaiheenSuoritus => s
      })
    )
}

case class SdgDIAOppiaineenTutkintovaiheenSuoritus(
  koulutusmoduuli: schema.DIAOppiaine,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus
