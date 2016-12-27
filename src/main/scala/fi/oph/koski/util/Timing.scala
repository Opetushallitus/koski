package fi.oph.koski.util

import fi.oph.koski.log.Logging
import io.prometheus.client.Summary
import org.log4s.getLogger
import rx.lang.scala.Observable

object Timing {
  def timed[R](blockname: String, thresholdMs: Int = 50, clazz: Class[_])(block: => R): R = {
    val timer = new Timer(blockname, thresholdMs, clazz)
    timer.complete(block)
  }
}

trait Timing extends Logging {
  def timed[R](blockname: String, thresholdMs: Int = 50)(block: => R): R = {
    Timing.timed(blockname, thresholdMs, getClass)(block)
  }

  /**
   * Logs the time spent from creation of the Observable to first value
   */
  def timedObservable[R](blockname: String, thresholdMs: Int = 0)(observable: => Observable[R]): Observable[R] = {
    val t0: Long = System.nanoTime()
    val timer = new Timer(blockname, thresholdMs, getClass)
    observable.doOnNext { x =>
      timer.complete(x)
    }
  }
}

class Timer(blockname: String, thresholdMs: Int, clazz: Class[_]) {
  private val t0 = System.nanoTime()
  private var completed = false

  def complete[T](result: T): T = {
    if (!completed) {
      completed = true
      val t1 = System.nanoTime()
      val time: Long = (t1 - t0) / 1000000
      TimerMonitoring.record(clazz.getSimpleName, blockname, time)
      if (time >= thresholdMs) Timer.logger.info(s"${clazz.getSimpleName} - $blockname" + s" took $time ms")
    }
    result
  }

}

object TimerMonitoring {
  private val durationDummary = Summary.build().name("fi_oph_koski_util_Timing_duration").help("Koski timed block duration").labelNames("classname", "blockname").register()

  def record[T](className: String, blockname: String, time: Long): Any = {
    durationDummary.labels(className, blockname).observe(time.toDouble / 1000)
  }
}

object Timer {
  val logger = getLogger(classOf[Timer])
}