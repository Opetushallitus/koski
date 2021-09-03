package fi.oph.koski.validation

import fi.oph.koski.documentation.ExampleData.{laajuusKursseissa, laajuusOpintopisteissä}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Koodistokoodiviite, Laajuus, LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa, LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019, LukioonValmistavanKoulutuksenSuoritus, Suoritus}

import java.time.LocalDate

object LukioonValmistavanKoulutuksenValidaatiot {
  def validateLukioonValmistava2019(suoritus: Suoritus) = {
    suoritus match {
      case s: LukioonValmistavanKoulutuksenSuoritus => HttpStatus.fold(List(
          validateOikeatKoodistotKäytössä(s),
          validateLaajuudenYksiköt(s)
        )
      )
      case _ => HttpStatus.ok
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

  private def validateLaajuudenYksiköt(suoritus: LukioonValmistavanKoulutuksenSuoritus) = {
    if (suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "OPH-4958-2020") {
      laajuudetRekursiivisesti(suoritus).exists(_.yksikkö.koodiarvo != laajuusOpintopisteissä.koodiarvo) match {
        case true => KoskiErrorCategory.badRequest.validation.laajuudet.lukioonValmistavallaKoulutuksellaVääräLaajuudenArvo()
        case false => HttpStatus.ok
      }
    } else if (suoritus.koulutusmoduuli.perusteenDiaarinumero.getOrElse("") == "56/011/2015") {
      laajuudetRekursiivisesti(suoritus).exists(_.yksikkö.koodiarvo != laajuusKursseissa.koodiarvo) match {
        case true => KoskiErrorCategory.badRequest.validation.laajuudet.lukioonValmistavallaKoulutuksellaVääräLaajuudenArvo(
          "Lukioon valmistavan koulutuksen suorituksella voi olla laajuuden koodiyksikkönä vain '4', jos suorituksen diaarinumero on '56/011/2015'")
        case false => HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }

  private def laajuudetRekursiivisesti(suoritus: Suoritus): List[Laajuus] = {
    List(suoritus.koulutusmoduuli.getLaajuus.toList).flatten  :::
      suoritus.osasuoritukset.toList.flatten.flatMap(
        laajuudetRekursiivisesti(_)
      )
  }
}

