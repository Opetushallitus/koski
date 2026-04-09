package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AikuistenPerusopetuksenOpiskeluoikeus, Opiskeluoikeus}

object AikuistenPerusopetuksenOpiskeluoikeudenValidation {

  def validateAikuistenPerusopetuksenOpiskeluoikeus(
    oo: Opiskeluoikeus
  ): HttpStatus = {
    oo match {
      case aipeOo: AikuistenPerusopetuksenOpiskeluoikeus => validateAikuistenPerusopetusOppimääränJaAineopintojenSuoritusSamaanAikaan(aipeOo)
      case _ => HttpStatus.ok
    }
  }

  def validateAikuistenPerusopetusOppimääränJaAineopintojenSuoritusSamaanAikaan(oo: AikuistenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    val sisältääAineopintoja = oo.suoritukset.exists(_.tyyppi.koodiarvo == "perusopetuksenoppiaineenoppimaara")
    val sisältääMuitaKuinAineopintoja = oo.suoritukset.exists(_.tyyppi.koodiarvo != "perusopetuksenoppiaineenoppimaara")

    HttpStatus.validate(!(sisältääAineopintoja && sisältääMuitaKuinAineopintoja))(
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Aikuisten perusopetuksen opiskeluoikeudella ei voi olla sekä oppimäärän että oppiaineen oppimäärän suorituksia")
    )
  }
}
