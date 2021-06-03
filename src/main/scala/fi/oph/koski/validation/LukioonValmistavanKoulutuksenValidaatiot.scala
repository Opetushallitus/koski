package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Koodistokoodiviite, LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa, LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019, LukioonValmistavanKoulutuksenSuoritus, Suoritus}

import java.time.LocalDate

object LukioonValmistavanKoulutuksenValidaatiot {
  def validateLukioonValmistava2019(suoritus: Suoritus) = {
    suoritus match {
      case s: LukioonValmistavanKoulutuksenSuoritus => HttpStatus.fold(List(
          validateLukioonValmistava2019Osasuoritukset(s),
          validateOikeatKoodistotKäytössä(s)
        )
      )
      case _ => HttpStatus.ok
    }
  }

  private def validateLukioonValmistava2019Osasuoritukset(suoritus: LukioonValmistavanKoulutuksenSuoritus) = {
    if (suoritus.osasuoritukset.getOrElse(List()).exists(_.isInstanceOf[LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019]) &&
      suoritus.osasuoritukset.getOrElse(List()).exists(_.isInstanceOf[LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa]))
    {
      KoskiErrorCategory.badRequest.validation.rakenne.lukioonValmistavassaEriLukioOpsienOsasuorituksia()
    }
    else {
      HttpStatus.ok
    }
  }

  private def validateOikeatKoodistotKäytössä(suoritus: LukioonValmistavanKoulutuksenSuoritus) = {
    val kurssiSuorituksetLuva2019Voimassa = suoritus.osasuoritukset.toList.flatten.flatMap(_.osasuoritusLista.filter(_.alkamispäivä.exists(_.isAfter(LocalDate.of(2021, 7, 31)))))

    val virheellisetSuoritukset = kurssiSuorituksetLuva2019Voimassa.filter(_.koulutusmoduuli.tunniste match {
      case viite: Koodistokoodiviite if viite.koodistoUri != "lukioonvalmistavankoulutuksenmoduulit2019" => {
        true
      }
      case _ => false
    })

    virheellisetSuoritukset.headOption match {
      case Some(suoritus) => {
        KoskiErrorCategory.badRequest.validation.rakenne.lukioonValmistavassaVanhanOpsinKurssiSuorituksia("Lukioon valmistavan koulutuksen kurssilla " + suoritus.koulutusmoduuli.tunniste + " on vanhan opetussuunniteleman mukainen koodi. 1.8.2021 jälkeen alkaneiden kurssien tulee käyttää vuoden 2021 opetussuunnitelmaa.")
      }
      case _ => HttpStatus.ok
    }
  }
}

