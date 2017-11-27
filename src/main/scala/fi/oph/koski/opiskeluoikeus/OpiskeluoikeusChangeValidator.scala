package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus

object OpiskeluoikeusChangeValidator {
  def validateOpiskeluoikeusChange(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (oldState.tyyppi.koodiarvo != newState.tyyppi.koodiarvo) {
      KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ${oldState.tyyppi.koodiarvo}. Uusi tyyppi ${newState.tyyppi.koodiarvo}.")
    } else if (oldState.lähdejärjestelmänId.isDefined && newState.lähdejärjestelmänId.isEmpty) {
      KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi poistaa.")
    } else {
      HttpStatus.ok
    }
  }
}
