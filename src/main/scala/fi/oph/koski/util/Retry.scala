package fi.oph.koski.util

import fi.oph.koski.log.Logging

object Retry extends Logging {
  def retryWithInterval[T](n: Int, intervalMs: Long)(fn: => T): T = {
    try {
      fn
    } catch {
      case e if n > 1 =>
        logger.warn(e)(s"${e.getMessage}. Retrying. Retries left: ${n - 1}")
        Thread.sleep(intervalMs)
        retryWithInterval(n - 1, intervalMs)(fn)
      case e: Throwable =>
        logger.error(e)(s"${e.getMessage}. No retries left.")
        throw e
    }
  }
}
