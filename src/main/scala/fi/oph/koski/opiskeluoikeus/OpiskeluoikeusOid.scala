package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.Opiskeluoikeus

object OpiskeluoikeusOid {

  def validateOpiskeluoikeusOid(oid: String): Either[HttpStatus, Opiskeluoikeus.Oid] = {
    if (Opiskeluoikeus.isValidOpiskeluoikeusOid(oid)) {
      Right(oid)
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid("Virheellinen oid: " + oid + ". Esimerkki oikeasta muodosta: 1.2.246.562.15.00000000001."))
    }
  }
}
