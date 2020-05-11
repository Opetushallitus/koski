package fi.oph.koski.suoritusjako

import java.util.UUID.randomUUID

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

object SuoritusjakoSecret {
  def validate(secret: String): Either[HttpStatus, String] = {
    if (secret.matches("^[0-9a-f]{32}$")) {
      Right(secret)
    } else {
      Left(KoskiErrorCategory.notFound())
    }
  }

  def generateNew: String = {
    randomUUID.toString.replaceAll("-", "")
  }
}
