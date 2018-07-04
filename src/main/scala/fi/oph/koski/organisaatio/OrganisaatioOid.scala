package fi.oph.koski.organisaatio

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.Organisaatio

object OrganisaatioOid {
  def validateOrganisaatioOid(oid: String): Either[HttpStatus, Organisaatio.Oid] = {
    if (Organisaatio.isValidOrganisaatioOid(oid)) {
      Right(oid)
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenOrganisaatioOid("Virheellinen oid: " + oid + ". Esimerkki oikeasta muodosta: 1.2.246.562.10.00000000001."))
    }
  }
}
