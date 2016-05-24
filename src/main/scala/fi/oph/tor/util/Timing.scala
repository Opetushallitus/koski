package fi.oph.tor.util

import fi.oph.tor.log.{LoggerWithContext, Logging}
import org.log4s._
import rx.lang.scala.Observable

object Timing {
  def timed[R](blockname: String, thresholdMs: Int = 50, logger: LoggerWithContext)(block: => R): R = {
    val timer = new Timer(logger, blockname, thresholdMs)
    timer.complete(block)
  }
}

trait Timing extends Logging {
  def timed[R](blockname: String, thresholdMs: Int = 50)(block: => R): R = {
    Timing.timed(blockname, thresholdMs, this.logger)(block)
  }

  /**
   * Logs the time spent from creation of the Observable to first value
   */
  def timedObservable[R](blockname: String, thresholdMs: Int = 0)(observable: => Observable[R]): Observable[R] = {
    val t0: Long = System.nanoTime()
    val timer = new Timer(logger, blockname, thresholdMs)
    observable.doOnNext { x =>
      timer.complete(x)
    }
  }
}

class Timer(logger: LoggerWithContext, blockname: String, thresholdMs: Int) {
  private val t0 = System.nanoTime()
  private var completed = false
  def complete[T](result: T): T = {
    if (!completed) {
      completed = true
      val t1 = System.nanoTime()
      val time: Long = (t1 - t0) / 1000000
      if (time >= thresholdMs) logger.info(blockname + " took " + time + " ms")
    }
    result
  }
}

