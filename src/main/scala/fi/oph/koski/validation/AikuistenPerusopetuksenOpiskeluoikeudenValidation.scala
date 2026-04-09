package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AikuistenPerusopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}

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

  def validateAikuistenPerusopetusAineopinnotVaihto(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = (oldState, newState) match {
    case (oldOo: AikuistenPerusopetuksenOpiskeluoikeus, newOo: AikuistenPerusopetuksenOpiskeluoikeus) =>
      val oldAineopinnot = oldOo.suoritukset.exists(_.tyyppi.koodiarvo == "perusopetuksenoppiaineenoppimaara")
      val newAineopinnot = newOo.suoritukset.exists(_.tyyppi.koodiarvo == "perusopetuksenoppiaineenoppimaara")
      if (oldAineopinnot && !newAineopinnot) {
        KoskiErrorCategory.badRequest.validation.rakenne.suorituksenTyyppiMuuttunut("Aikuisten perusopetuksen oppiaineen oppimäärän opiskeluoikeutta ei voi muuttaa oppimäärän opiskeluoikeudeksi")
      } else if (!oldAineopinnot && newAineopinnot) {
        KoskiErrorCategory.badRequest.validation.rakenne.suorituksenTyyppiMuuttunut("Aikuisten perusopetuksen oppimäärän opiskeluoikeutta ei voi muuttaa oppiaineen oppimäärän opiskeluoikeudeksi")
      } else {
        HttpStatus.ok
      }
    case _ => HttpStatus.ok
  }
}
