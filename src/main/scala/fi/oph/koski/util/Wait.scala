package fi.oph.koski.util

object Wait {
  def until(predicate: => Boolean, timeoutMs: Long = 60000, retryIntervalMs: Long = 100) = {
    val started = System.currentTimeMillis()
    val timeoutAt = started + timeoutMs
    var ok = false
    while(!ok) {
      ok = predicate
      if (System.currentTimeMillis > timeoutAt) {
        throw new RuntimeException("Wait timed out at " + timeoutMs + " milliseconds")
      }
    }
  }
}
