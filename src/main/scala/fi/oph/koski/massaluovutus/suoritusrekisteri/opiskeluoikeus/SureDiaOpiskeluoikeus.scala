package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

object SureDiaOpiskeluoikeus {
  def apply(oo: DIAOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    when (isValmistunut(oo)) {
      SureOpiskeluoikeus(
        oo,
        oo.suoritukset.collect {
          case tutkinnonSuoritus: DIATutkinnonSuoritus if tutkinnonSuoritus.valmis =>
            SureDefaultPäätasonSuoritus(
              tutkinnonSuoritus,
              tutkinnonSuoritus.osasuoritukset.map(_.filter(_.valmis).map(SureDIAOppiaine.apply))
            )
        }
      )
    }
}

@Title("DIA-oppiaine")
case class SureDIAOppiaine(
  koulutusmoduuli: DIAOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  vastaavuustodistuksenTiedot: Option[DIAVastaavuustodistuksenTiedot] = None,
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  tyyppi: Koodistokoodiviite,
  osasuoritukset: Option[List[SureOsasuoritus]],
) extends SureOsasuoritus

object SureDIAOppiaine {
  def apply(s: DIAOppiaineenTutkintovaiheenSuoritus): SureDIAOppiaine =
    SureDIAOppiaine(
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      vastaavuustodistuksenTiedot = s.vastaavuustodistuksenTiedot,
      koetuloksenNelinkertainenPistemäärä = s.koetuloksenNelinkertainenPistemäärä,
      tyyppi = s.tyyppi,
      osasuoritukset = s.osasuoritukset.map { _.collect {
        case pt: DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus if pt.koulutusmoduuli.isInstanceOf[DIAPäättökoe] => SureDefaultOsasuoritus(pt)
      }},
    )
}
