package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.{Koodistokoodiviite, Koulutustoimija, Oppilaitos, DIAOpiskeluoikeudenTila}
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

@Title("DIA-tutkinnon opiskeluoikeus")
case class SdgDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: DIAOpiskeluoikeudenTila,
  suoritukset: List[SdgDIATutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SdgKoskeenTallennettavaOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgKoskeenTallennettavaOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgDIATutkinnonSuoritus => s }
    )
}


@Title("DIA-tutkinnon suoritus")
case class SdgDIATutkinnonSuoritus(
  koulutusmoduuli: schema.Koulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[SdgDIAOppiaineenTutkintovaiheenSuoritus]]
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
  suorituskieli: Option[Koodistokoodiviite] = None,
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite
) extends Osasuoritus
