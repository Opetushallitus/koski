package fi.oph.tor.henkilo

import fi.oph.tor.http.{TorErrorCategory, HttpStatus}

object HenkiloOid {
  def isValidHenkilöOid(oid: String) = {
    """1\.2\.246\.562\.24\.\d{11}""".r.findFirstIn(oid).isDefined
  }

  def validateHenkilöOid(oid: String): Either[HttpStatus, String] = {
    if (isValidHenkilöOid(oid)) {
      Right(oid)
    } else {
      Left(TorErrorCategory.badRequest.validation.henkilötiedot.virheellinenOid("Virheellinen oid: " + oid))
    }
  }
}
