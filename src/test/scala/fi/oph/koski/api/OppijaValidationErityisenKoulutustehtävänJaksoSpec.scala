package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema.{ErityisenKoulutustehtävänJakso, Koodistokoodiviite, LukionOpiskeluoikeudenLisätiedot}
import org.scalatest.FreeSpec

import java.time.LocalDate

class OppijaValidationErityisenKoulutustehtävänJaksoSpec extends FreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio2015 {
  "Erityisen koulutustehtävän jakso" - {
    "Vanhentuneella koodilla" in {
      val ekj = ErityisenKoulutustehtävänJakso(LocalDate.now, None, Koodistokoodiviite("taide", "erityinenkoulutustehtava"))
      val lisätiedot = LukionOpiskeluoikeudenLisätiedot(erityisenKoulutustehtävänJaksot = Some(List(ekj)))
      val oo = defaultOpiskeluoikeus.copy(lisätiedot = Some(lisätiedot))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(
          "Koodiarvo 'taide' ei ole sallittu erityisen koulutustehtävän jaksolle"
        ))
      }
    }

    "Muulla koodilla" in {
      val ekj = ErityisenKoulutustehtävänJakso(LocalDate.now, None, Koodistokoodiviite("103", "erityinenkoulutustehtava"))
      val lisätiedot = LukionOpiskeluoikeudenLisätiedot(erityisenKoulutustehtävänJaksot = Some(List(ekj)))
      val oo = defaultOpiskeluoikeus.copy(lisätiedot = Some(lisätiedot))

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }
}
