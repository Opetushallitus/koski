package fi.oph.koski.executors

import fi.oph.koski.log.Logging

import java.util.concurrent.ForkJoinPool

object NamedForkJoinPoolExecutor extends Logging {
  def apply(name: String): ForkJoinPool = {
    val jdkProcessorCount = Runtime.getRuntime.availableProcessors
    val threadCount = Integer.max(jdkProcessorCount, 4)
    logger.info(s"JDK Processor count: ${jdkProcessorCount}, creating fork join pool of ${threadCount} threads")

    val executor = new ForkJoinPool(
      threadCount,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true
    )
    ManagedForkJoinPoolExecutor.register(name, executor)
  }
}
