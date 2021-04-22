package fi.oph.koski.executors

import scala.concurrent.ExecutionContextExecutor

trait GlobalExecutionContext {
  protected final implicit val executor: ExecutionContextExecutor = Pools.globalExecutor
}
