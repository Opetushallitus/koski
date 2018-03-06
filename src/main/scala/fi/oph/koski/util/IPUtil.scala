package fi.oph.koski.util

import java.net.InetAddress

import fi.oph.koski.log.Logging

object IPUtil extends Logging {
  def toInetAddress(host: String): Option[InetAddress] = try {
    Some(InetAddress.getByName(host))
  } catch {
    case e: Exception =>
      logger.error(e)(s"Error converting host $host to inet address")
      None
  }
}
