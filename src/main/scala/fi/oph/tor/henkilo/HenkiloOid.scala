package fi.oph.tor.henkilo

import fi.oph.tor.http.{TorErrorCode, HttpStatus}

object HenkiloOid {
  def isValidHenkilöOid(oid: String) = {
    """1\.2\.246\.562\.24\.\d{11}""".r.findFirstIn(oid).isDefined
  }

  def validateHenkilöOid(oid: String): Either[HttpStatus, String] = {
    if (isValidHenkilöOid(oid)) {
      Right(oid)
    } else {
      Left(HttpStatus.badRequest(TorErrorCode.Validation.henkilöOid, "Invalid henkilö oid: " + oid))
    }
  }
}
