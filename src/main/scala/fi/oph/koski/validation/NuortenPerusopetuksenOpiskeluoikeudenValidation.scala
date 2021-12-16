package fi.oph.koski.validation

import fi.oph.koski.documentation.OsaAikainenErityisopetusExampleData.tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, NuortenPerusopetuksenOppiaineenOppimääränSuoritus, NuortenPerusopetuksenOppimääränSuoritus, Opiskeluoikeus, PerusopetuksenOpiskeluoikeus}

object NuortenPerusopetuksenOpiskeluoikeusValidation {
  def validateNuortenPerusopetuksenOpiskeluoikeus(oo: Opiskeluoikeus) = {
    oo match {
      case s: PerusopetuksenOpiskeluoikeus => validateNuortenPerusopetuksenOpiskeluoikeudenTila(s)
      case _ => HttpStatus.ok
    }
  }

  private def validateNuortenPerusopetuksenOpiskeluoikeudenTila(oo: PerusopetuksenOpiskeluoikeus) = {
    if (oo.tila.opiskeluoikeusjaksot.last.tila.koodiarvo == "valmistunut") {
      if (oo.suoritukset.filter(_.isInstanceOf[NuortenPerusopetuksenOppimääränSuoritus]).exists(_.vahvistettu) ||
          oo.suoritukset.filter(_.isInstanceOf[NuortenPerusopetuksenOppiaineenOppimääränSuoritus]).nonEmpty) {
        HttpStatus.ok
      } else {
        KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenValmistunutTilaIlmanVahvistettuaPäättötodistusta()
      }
    } else {
      HttpStatus.ok
    }
  }

  def filterDeprekoidutKentät(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case perus: PerusopetuksenOpiskeluoikeus =>
        val filtteröityLisätieto = perus.lisätiedot.map(lisätieto => {
          lisätieto.copy(
            perusopetuksenAloittamistaLykätty = None,
            tehostetunTuenPäätökset = None
          )
        })
        perus.withLisätiedot(filtteröityLisätieto)
      case _ => oo
    }
  }
}

