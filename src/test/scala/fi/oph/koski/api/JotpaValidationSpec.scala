package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{jatkuvanOppimisenRahoitus, jatkuvanOppimisenUudistuksenRahoitus, muutaKauttaRahoitettu, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.{AmmattitutkintoExample, ExampleData, ExamplesAmmatillinen, ExamplesVapaaSivistystyö}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeusjakso, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus}
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.validation.JotpaValidation
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class JotpaValidationSpec extends AnyFreeSpec with Matchers {
  "JOTPA-rahoituksen yhtenäisyys" - {
    val rajapäivä: LocalDate = LocalDate.of(2000, 1, 1)

    "Hyväksyy erilaisia ei-jotpa-rahoitusmuotoja sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen, muutaKauttaRahoitettu))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, rajapäivä) should equal(HttpStatus.ok)
    }

    "Hyväksyy pelkän jatkuvan oppimisen rahoituksen" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus, jatkuvanOppimisenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, rajapäivä) should equal(HttpStatus.ok)
    }

    "Hyväksyy pelkän jatkuvan oppimisen uudistuksen rahoituksen" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenUudistuksenRahoitus, jatkuvanOppimisenUudistuksenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, rajapäivä) should equal(HttpStatus.ok)
    }

    "Ei hyväksy jotpa- ja ei-jotpa-rahoitusta sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen, jatkuvanOppimisenRahoitus))
      val error = KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys()
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, rajapäivä) should equal(error)
    }

    "Ei hyväksy jotpa-rahoitusmuotoja sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenUudistuksenRahoitus, jatkuvanOppimisenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, rajapäivä) should equal(KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys())
    }
  }

  "JOTPA-rahoituksen alkamispäivä" in {
    val rajapäivä: LocalDate = LocalDate.of(2300, 1, 1)
    val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus))
    JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, rajapäivä) should equal(
      KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Jatkuvaan oppimiseen suunnatun koulutuksen opiskeluoikeuden jakso ei voi alkaa ennen ${finnishDateFormat.format(rajapäivä)}")
    )
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
