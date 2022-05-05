package fi.oph.koski.util

import scala.util.Random

object Cryptographic {
  def nonce: String = Random.alphanumeric.take(32).mkString
}
