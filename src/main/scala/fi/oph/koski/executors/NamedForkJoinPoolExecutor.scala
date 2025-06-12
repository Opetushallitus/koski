package fi.oph.koski.executors

import java.util.concurrent.ForkJoinPool

object NamedForkJoinPoolExecutor {
  def apply(name: String): ForkJoinPool = {
    val executor = new ForkJoinPool(
      Runtime.getRuntime.availableProcessors,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true
    )
    ManagedForkJoinPoolExecutor.register(name, executor)
  }
}
