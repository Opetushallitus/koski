package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{jatkuvanOppimisenRahoitus, jatkuvanOppimisenUudistuksenRahoitus, muutaKauttaRahoitettu, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.{AmmattitutkintoExample, ExampleData, ExamplesAmmatillinen, ExamplesVapaaSivistystyö}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeusjakso, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus}
import fi.oph.koski.validation.JotpaValidation
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class JotpaValidationSpec extends AnyFreeSpec with Matchers {
  "JOTPA-rahoituksen yhtenäisyys" - {
    "Hyväksyy erilaisia ei-jotpa-rahoitusmuotoja sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen, muutaKauttaRahoitettu))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus) should equal(HttpStatus.ok)
    }

    "Hyväksyy pelkän jatkuvan oppimisen rahoituksen" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus, jatkuvanOppimisenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus) should equal(HttpStatus.ok)
    }

    "Hyväksyy pelkän jatkuvan oppimisen uudistuksen rahoituksen" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenUudistuksenRahoitus, jatkuvanOppimisenUudistuksenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus) should equal(HttpStatus.ok)
    }

    "Ei hyväksy jotpa- ja ei-jotpa-rahoitusta sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen, jatkuvanOppimisenRahoitus))
      val error = KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys("Opiskeluoikeudella, jolla on jatkuvan oppimisen rahoitusta, ei voi olla muita rahoitusmuotoja")
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus) should equal(error)
    }

    "Ei hyväksy jotpa-rahoitusmuotoja sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenUudistuksenRahoitus, jatkuvanOppimisenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus) should equal(KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys())
    }
  }

  def getOpiskeluoikeus(rahoitukset: List[Koodistokoodiviite]): KoskeenTallennettavaOpiskeluoikeus = {
    AmmattitutkintoExample.opiskeluoikeus.copy(
      tila = AmmatillinenOpiskeluoikeudenTila(rahoitukset.zipWithIndex.map {
        case (rahoitus, index) => AmmatillinenOpiskeluoikeusjakso(
          LocalDate.of(2012, 9, 1).plusYears(index),
          if (rahoitus == rahoitukset.last) opiskeluoikeusValmistunut else opiskeluoikeusLäsnä,
          Some(rahoitus),
        )
      })
    )
  }
}