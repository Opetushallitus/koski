package fi.oph.koski.validation

import fi.oph.koski.schema.{_}

object KoodistopoikkeustenKonversiot {
  def konvertoiKoodit(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    lukionKonversiot(oo)
  }

  private def lukionKonversiot(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo.withSuoritukset(
      oo.suoritukset.map {
        // 2015
        case oppi: LukionOppimääränSuoritus2015 => lukionOppimääränKonversiot2015(oppi)
        case aine: LukionOppiaineenOppimääränSuoritus2015 => lukionOppiaineenOppimääränKonversiot2015(aine)
        // 2019
        case oppi: LukionOppimääränSuoritus2019 => lukionOppimääränKonversiot2019(oppi)
        case aine: LukionOppiaineidenOppimäärienSuoritus2019 if aine.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "OPH-2267-2019" => aine.withOsasuoritukset(lukionHistoriankurssienKonversio2019(aine.osasuoritukset))
        case s: Any => s
      }
    )
  }

  private def lukionOppimääränKonversiot2015(suoritus: LukionOppimääränSuoritus2015) = {
    suoritus.withOsasuoritukset(
      suoritus.osasuoritukset match {
        case Some(osasuoritukset) => Some(osasuoritukset.map {
          case mlos: MuidenLukioOpintojenSuoritus2015 if suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "70/011/2015"
          => mlos.copy(osasuoritukset = lukionHistoriankurssienKonversio(mlos.osasuoritukset))
          case loos: LukionOppiaineenSuoritus2015 if suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "70/011/2015"
          => loos.copy(osasuoritukset = lukionHistoriankurssienKonversio(loos.osasuoritukset))
          case any: Any => any
        })
        case _ => None
      }
    )
  }

  private def lukionOppiaineenOppimääränKonversiot2015(suoritus: LukionOppiaineenOppimääränSuoritus2015) = {
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
              tunniste = valtakunnallinen.tunniste.copy(nimi =
                Some(Finnish("Itsenäisen Suomen historia", Some("Det självständiga Finlands historia"))
                ))
            )
          )
          case valtakunnallinen: ValtakunnallinenLukionKurssi2015 if suoritus.koulutusmoduuli.tunniste.koodiarvo == "HI3" => suoritus.copy(
            koulutusmoduuli = valtakunnallinen.copy(
              tunniste = valtakunnallinen.tunniste.copy(nimi =
                Some(Finnish("Kansainväliset suhteet", Some("Internationella relationer"))
                ))
            )
          )
          case _ => suoritus
        }
      })
    )
  }

  private def lukionOppimääränKonversiot2019(suoritus: LukionOppimääränSuoritus2019): PäätasonSuoritus = {
    suoritus.withOsasuoritukset(
      suoritus.osasuoritukset match {
        case Some(osasuoritukset) => Some(osasuoritukset.map {
          case loos: LukionOppiaineenSuoritus2019 if suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "OPH-2267-2019"
          => loos.copy(osasuoritukset = lukionHistoriankurssienOsasuoritustenKonversio2019Paikallinen(loos.osasuoritukset))
          case moos: MuidenLukioOpintojenSuoritus2019 if suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "OPH-2267-2019"
          => moos.copy(osasuoritukset = lukionHistoriankurssienOsasuorisutenKonversio2019MuissaOpinnoissa(moos.osasuoritukset))
          case a: Any => a
        })
        case None => None
      }
    )
  }

  private val korjattuHistoria2Nimi = Finnish("Itsenäisen Suomen historia", Some("Det självständiga Finlands historia"))
  private val korjattuHistoria3Nimi = Finnish("Kansainväliset suhteet", Some("Internationella relationer"))

  private def lukionHistoriankurssienKonversio2019(suoritukset: Option[List[LukionOppiaineenSuoritus2019]]): Option[List[LukionOppiaineenSuoritus2019]] = {
    suoritukset.map(suoritukset =>
      suoritukset.map(suoritus => {
        suoritus.koulutusmoduuli match {
          case _: PaikallinenLukionOppiaine2019 if suoritus.koulutusmoduuli.tunniste.koodiarvo == "HI" => suoritus.copy(osasuoritukset = lukionHistoriankurssienOsasuoritustenKonversio2019Paikallinen(suoritus.osasuoritukset))
          case _: PaikallinenLukionOppiaine2019 if suoritus.koulutusmoduuli.tunniste.koodiarvo == "HI" => suoritus.copy(osasuoritukset = lukionHistoriankurssienOsasuoritustenKonversio2019Paikallinen(suoritus.osasuoritukset))
          case _: LukionMuuValtakunnallinenOppiaine2019 if suoritus.koulutusmoduuli.tunniste.koodiarvo == "HI" => suoritus.copy(osasuoritukset = lukionHistoriankurssienOsasuoritustenKonversio2019Paikallinen(suoritus.osasuoritukset))
          case _ => suoritus
        }
      })
    )
  }

  private def lukionHistoriankurssienOsasuoritustenKonversio2019Paikallinen(osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019]]) =
    osasuoritukset.map(osasuoritukset =>
      osasuoritukset.map(osasuoritus => {
        (osasuoritus, osasuoritus.koulutusmoduuli) match {
          case (lukionModuulinSuoritus: LukionModuulinSuoritusOppiaineissa2019, koulutusmoduuli: LukionMuuModuuliOppiaineissa2019)
            if koulutusmoduuli.tunniste.koodiarvo == "HI2" =>
            lukionModuulinSuoritus.copy(koulutusmoduuli = koulutusmoduuli.copy(tunniste = koulutusmoduuli.tunniste.copy(nimi = Some(korjattuHistoria2Nimi))))
          case (lukionModuulinSuoritus: LukionModuulinSuoritusOppiaineissa2019, koulutusmoduuli: LukionMuuModuuliOppiaineissa2019)
            if koulutusmoduuli.tunniste.koodiarvo == "HI3" =>
            lukionModuulinSuoritus.copy(koulutusmoduuli = koulutusmoduuli.copy(tunniste = koulutusmoduuli.tunniste.copy(nimi = Some(korjattuHistoria3Nimi))))
          case (paikallinenSuoritus: LukionPaikallisenOpintojaksonSuoritus2019, koulutusmoduuli: LukionPaikallinenOpintojakso2019)
            if osasuoritus.koulutusmoduuli.tunniste.koodiarvo == "HI2" =>
            paikallinenSuoritus.copy(koulutusmoduuli = koulutusmoduuli.copy(tunniste = koulutusmoduuli.tunniste.copy(nimi = korjattuHistoria2Nimi)))
          case (paikallinenSuoritus: LukionPaikallisenOpintojaksonSuoritus2019, koulutusmoduuli: LukionPaikallinenOpintojakso2019)
            if osasuoritus.koulutusmoduuli.tunniste.koodiarvo == "HI3" =>
            paikallinenSuoritus.copy(koulutusmoduuli = koulutusmoduuli.copy(tunniste = koulutusmoduuli.tunniste.copy(nimi = korjattuHistoria3Nimi)))
          case _: Any => osasuoritus
        }
      })
    )

  private def lukionHistoriankurssienOsasuorisutenKonversio2019MuissaOpinnoissa(osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019]]) = {
    osasuoritukset.map(osasuoritukset =>
      osasuoritukset.map(osasuoritus =>
        (osasuoritus, osasuoritus.koulutusmoduuli) match {
          case (muissaOpinnoissa: LukionModuulinSuoritusMuissaOpinnoissa2019, koulutusmoduuli: LukionMuuModuuliMuissaOpinnoissa2019)
            if koulutusmoduuli.tunniste.koodiarvo == "HI2" =>
            muissaOpinnoissa.copy(koulutusmoduuli = koulutusmoduuli.copy(tunniste = koulutusmoduuli.tunniste.copy(nimi = Some(korjattuHistoria2Nimi))))
          case (muissaOpinnoissa: LukionModuulinSuoritusMuissaOpinnoissa2019, koulutusmoduuli: LukionMuuModuuliMuissaOpinnoissa2019)
            if koulutusmoduuli.tunniste.koodiarvo == "HI3" =>
            muissaOpinnoissa.copy(koulutusmoduuli = koulutusmoduuli.copy(tunniste = koulutusmoduuli.tunniste.copy(nimi = Some(korjattuHistoria3Nimi))))
          case _: Any => osasuoritus
        }
      )
    )
  }
}
