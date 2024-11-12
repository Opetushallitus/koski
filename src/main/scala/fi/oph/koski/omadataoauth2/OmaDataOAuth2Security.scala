package fi.oph.koski.omadataoauth2

import fi.oph.koski.log.Logging

import java.math.BigInteger
import java.security.MessageDigest

object OmaDataOAuth2Security extends Logging {
  def sha256(str: String): String = {

    val result = String.format(
      "%064x",
      new BigInteger(
        1,
        MessageDigest.getInstance("SHA-256").digest(str.getBytes("UTF-8")))
    )

    logger.info(str + "=>" + result)
    result
  }
}
