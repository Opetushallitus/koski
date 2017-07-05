package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{oppiaineidenSuoritukset2015, oppiaineidenSuoritukset2017}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

class OppijaValidationAikuistenPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  override def defaultOpiskeluoikeus = opiskeluoikeusWithPerusteenDiaarinumero(Some("19/011/2015"))

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = super.defaultOpiskeluoikeus.copy(
    suoritukset = List(
      aikuistenPerusopetuksenOppimääränSuoritus(diaari)
    ))

  private def aikuistenPerusopetuksenOppimääränSuoritus(diaari: Option[String] = Some("19/011/2015")) = {
    ExamplesAikuistenPerusopetus.aikuistenPerusopetukseOppimääränSuoritus(
      AikuistenPerusopetus(diaari),
      (if (diaari == Some("OPH-1280-2017")) { oppiaineidenSuoritukset2017 } else { oppiaineidenSuoritukset2015 })
    )
  }

  def eperusteistaLöytymätönValidiDiaarinumero: String = "OPH-1280-2017"

  "Kurssisuoritukset" - {
    "OPS 2015, mutta kurssisuorituksissa 2017 koodisto -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus(Some("19/011/2015")).copy(osasuoritukset = oppiaineidenSuoritukset2017)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräKurssikoodisto("Aikuisten perusopetuksessa 2015 käytetty väärää kurssikoodistoa aikuistenperusopetuksenpaattovaiheenkurssit2017 (käytettävä koodistoa aikuistenperusopetuksenkurssit2015)"))
      }
    }

    "OPS 2017, mutta kurssisuorituksissa 2015 koodisto -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus(Some("OPH-1280-2017")).copy(osasuoritukset = oppiaineidenSuoritukset2015)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräKurssikoodisto("Aikuisten perusopetuksessa 2017 käytetty väärää kurssikoodistoa aikuistenperusopetuksenkurssit2015 (käytettävä koodistoa aikuistenperusopetuksenpaattovaiheenkurssit2017)"))
      }
    }
  }
}