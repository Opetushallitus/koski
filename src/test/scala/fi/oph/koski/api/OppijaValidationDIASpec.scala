package fi.oph.koski.api

import fi.oph.koski.api.OpiskeluoikeusTestMethodsDIA.tutkintoSuoritus
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.DIAExampleData._
import fi.oph.koski.documentation.ExampleData.{helsinki, vahvistusPaikkakunnalla}
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema.{LukionOpiskeluoikeudenTila, LukionOpiskeluoikeusjakso}
import org.scalatest.FreeSpec
import java.time.LocalDate.{of => date}

class OppijaValidationDIASpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsDIA {
  "Laajuudet" - {
    """Lukukauden laajuusyksikkö muu kuin "vuosiviikkotuntia" -> HTTP 400""" in {
      val laajuudenYksikköKurssia = "4"
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2", laajuus(8)), Some(List(
          (diaTutkintoLukukausi("3", laajuus(8f, laajuudenYksikköKurssia)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
      }
    }

    "Suoritus kesken, lukukausien laajuuksien summa ei täsmää -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2", laajuus(2)), Some(List(
          (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen oppiaineetdia/MA osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 2.0"))
      }
    }

    "Suoritus valmis, lukukausien laajuuksien summa ei täsmää -> HTTP 400" in {
      val oo = valmisOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2", laajuus(2)), Some(List(
          (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen oppiaineetdia/MA osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 2.0"))
      }
    }

    "Suoritus kesken, laajuudet täsmää pyöristyksillä -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2", laajuus(1)), Some(List(
          (diaTutkintoLukukausi("3", laajuus(0.33333f)), "2"),
          (diaTutkintoLukukausi("4", laajuus(0.33333f)), "2"),
          (diaTutkintoLukukausi("5", laajuus(0.33333f)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Suoritus valmis, laajuudet täsmää pyöristyksillä -> HTTP 200" in {
      val oo = valmisOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2", laajuus(1)), Some(List(
          (diaTutkintoLukukausi("3", laajuus(0.33333f)), "2"),
          (diaTutkintoLukukausi("4", laajuus(0.33333f)), "2"),
          (diaTutkintoLukukausi("5", laajuus(0.33333f)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Suoritus kesken, oppiaineelta puuttuu laajuus -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2"), Some(List(
          (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Suoritus valmis, oppiaineelta puuttuu laajuus -> HTTP 400" in {
      val oo = valmisOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2"), Some(List(
          (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
        )))))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Suoritus koulutus/301103 on merkitty valmiiksi, mutta se sisältää oppiaineen, jolta puuttuu laajuus"))
      }
    }
  }

  "Kaksi äidinkieltä" - {
    "Samalla kielivalinnalla -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(
          diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus(1)), Some(List(
            (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
          ))),
          diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus(1)), Some(List(
            (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
          )))
        ))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (oppiaineetdia/AI,oppiainediaaidinkieli/FI) esiintyy useammin kuin kerran"))
      }
    }

    "Eri kielivalinnalla -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(
          diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus(1)), Some(List(
            (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
          ))),
          diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("DE", laajuus(1)), Some(List(
            (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
          )))
        ))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  "Opintojen rahoitus" - {
    "lasna -tilalta vaaditaan opintojen rahoitus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta lasna puuttuu opintojen rahoitus"))
      }
    }
    "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
      val tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(date(2016, 6, 8), opiskeluoikeusValmistunut))
      )
      val suoritus = tutkintoSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
        osasuoritukset = Some(List(diaTutkintoAineSuoritus(diaOppiaineMuu("MA", osaAlue = "2", laajuus(1)), Some(List(
          (diaTutkintoLukukausi("3", laajuus(1.0f)), "2")
        )))))
      )
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila, suoritukset = List(suoritus))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta valmistunut puuttuu opintojen rahoitus"))
      }
    }
  }
}
