package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus

object OpiskeluoikeusChangeValidator {
  def validateOpiskeluoikeusChange(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (oldState.oppilaitos.oid != newState.oppilaitos.oid) {
      KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden oppilaitosta ei voi vaihtaa. Vanha oid ${oldState.oppilaitos.oid}. Uusi oid ${newState.oppilaitos.oid}.")
    } else if (oldState.koulutustoimija.map(_.oid) != newState.koulutustoimija.map(_.oid)) {
      KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden koulutustoimijaa ei voi vaihtaa. Vanha oid ${oldState.koulutustoimija.map(_.oid).getOrElse("null")}. Uusi oid ${newState.koulutustoimija.map(_.oid).getOrElse("null")}.")
    } else if (oldState.tyyppi.koodiarvo != newState.tyyppi.koodiarvo) {
      KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ${oldState.tyyppi.koodiarvo}. Uusi tyyppi ${newState.tyyppi.koodiarvo}.")
    } else if (oldState.lähdejärjestelmänId.isDefined && newState.lähdejärjestelmänId != oldState.lähdejärjestelmänId) {
      KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi muuttaa.")
    } else {
      HttpStatus.ok
    }
  }
}
