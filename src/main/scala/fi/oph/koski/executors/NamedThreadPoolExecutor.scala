package fi.oph.koski.executors

import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}

object NamedThreadPoolExecutor {
  def apply(name: String, minThreads: Int, maxThreads: Int,  queueSize: Int) = {
    val executor = new ThreadPoolExecutor(minThreads, maxThreads, 60, TimeUnit.SECONDS, new ArrayBlockingQueue[Runnable](queueSize), NamedThreadFactory(name), new AbortPolicy())
    ManagedThreadPoolExecutor.register(name, executor)
  }
}
