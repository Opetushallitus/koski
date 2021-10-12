package fi.oph.koski.validation

import fi.oph.koski.schema.{_}

object KoodistopoikkeustenKonversiot {
  def konvertoiKoodit(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    lukionKonversiot(oo)
  }

  private def lukionKonversiot(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo.withSuoritukset(
      oo.suoritukset.map {
        case oppi: LukionOppimääränSuoritus2015 => lukionOppimääränKonversiot(oppi)
        case aine: LukionOppiaineenOppimääränSuoritus2015 => lukionOppiaineenOppimääränKonversiot(aine)
        case s: Any => s
      }
    )
  }

  private def lukionOppimääränKonversiot(suoritus: LukionOppimääränSuoritus2015) = {
    suoritus.withOsasuoritukset(
      suoritus.osasuoritukset match {
        case Some(osasuoritukset) => Some(osasuoritukset.map {
            case mlos: MuidenLukioOpintojenSuoritus2015 if suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "70/011/2015"
              => mlos.copy(osasuoritukset = lukionHistoriankurssienKonversio(mlos.osasuoritukset))
            case loos: LukionOppiaineenSuoritus2015  if suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "70/011/2015"
              => loos.copy(osasuoritukset = lukionHistoriankurssienKonversio(loos.osasuoritukset))
            case any: Any => any
          })
        case _ => None
      }
    )
  }

  private def lukionOppiaineenOppimääränKonversiot(suoritus: LukionOppiaineenOppimääränSuoritus2015) = {
    suoritus.koulutusmoduuli match {
      case valtakunnallinen: LukionMuuValtakunnallinenOppiaine2015 if valtakunnallinen.perusteenDiaarinumero.getOrElse("") == "70/011/2015" =>
        suoritus.withOsasuoritukset(
          lukionHistoriankurssienKonversio(suoritus.osasuoritukset)
        )
      case _ => suoritus
    }
  }

  private def lukionHistoriankurssienKonversio(suoritukset: Option[List[LukionKurssinSuoritus2015]]) = {
    suoritukset.map(suoritukset =>
      suoritukset.map(suoritus => {
        suoritus.koulutusmoduuli match {
          case valtakunnallinen: ValtakunnallinenLukionKurssi2015 if suoritus.koulutusmoduuli.tunniste.koodiarvo == "HI2" => suoritus.copy(
              koulutusmoduuli = valtakunnallinen.copy(
                tunniste = valtakunnallinen.tunniste.copy(nimi = Some(LocalizedString.finnish("Itsenäisen Suomen historia")))
              )
            )
          case valtakunnallinen: ValtakunnallinenLukionKurssi2015 if suoritus.koulutusmoduuli.tunniste.koodiarvo == "HI3" => suoritus.copy(
            koulutusmoduuli = valtakunnallinen.copy(
              tunniste = valtakunnallinen.tunniste.copy(nimi = Some(LocalizedString.finnish("Kansainväliset suhteet")))
            )
          )
          case _ => suoritus
        }
      })
    )
  }
}
