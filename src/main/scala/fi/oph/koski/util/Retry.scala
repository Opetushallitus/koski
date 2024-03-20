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

  def onStateChangeDuringCall[T, S](getState: => S, n: Int = 1)(fn: => T): T = {
    val initialState = getState
    val result = fn
    val finalState = getState
    if (finalState != initialState) {
      if (n > 0) {
        logger.info(s"System state change detected: $initialState --> $finalState. Retrying...")
        onStateChangeDuringCall(getState, n - 1)(fn)
      } else {
        throw new RuntimeException(s"System state did not stay stabile during execution: $initialState --> $finalState")
      }
    } else {
      result
    }
  }
}
