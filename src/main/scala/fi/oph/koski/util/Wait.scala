package fi.oph.koski.util

object Wait {
  def until(predicate: => Boolean, timeoutMs: Long = 60000, retryIntervalMs: Long = 100): Boolean = {
    val started = System.currentTimeMillis()
    val timeoutAt = started + timeoutMs
    while (!predicate && (System.currentTimeMillis <= timeoutAt)) {
      Thread.sleep(retryIntervalMs)
    }
    return predicate
  }
}
