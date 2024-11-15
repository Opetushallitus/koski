package fi.oph.koski.omadataoauth2

import fi.oph.koski.log.Logging

import java.math.BigInteger
import java.security.{MessageDigest, SecureRandom}
import java.util.Base64
import java.util.UUID.randomUUID

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

  def createChallengeAndVerifier(): ChallengeAndVerifier = {
    val sr = new SecureRandom()
    val code = new Array[Byte](32)
    sr.nextBytes(code)
    val verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code)

    ChallengeAndVerifier(
      challenge = challengeFromVerifier(verifier),
      verifier = verifier
    )
  }

  def challengeFromVerifier(codeVerifier: String): String = {
    val codeVerifierBytes = codeVerifier.getBytes("ASCII")
    val digest =
      MessageDigest.getInstance("SHA-256").digest(codeVerifierBytes)
    Base64.getUrlEncoder.withoutPadding().encodeToString(digest)
  }

  def generateSecret: String = {
    randomUUID.toString.replaceAll("-", "")
  }
}

case class ChallengeAndVerifier(
  challenge: String,
  verifier: String
)
