package fi.oph.koski.henkilo

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

object HenkiloOid {
  def isValidHenkilöOid(oid: String) = {
    """1\.2\.246\.562\.24\.\d{11}""".r.findFirstIn(oid).isDefined
  }

  def validateHenkilöOid(oid: String): Either[HttpStatus, String] = {
    if (isValidHenkilöOid(oid)) {
      Right(oid)
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenOid("Virheellinen oid: " + oid))
    }
  }
}
