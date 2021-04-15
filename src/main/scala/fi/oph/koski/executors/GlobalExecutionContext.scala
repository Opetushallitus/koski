package fi.oph.koski.executors

import scala.concurrent.ExecutionContextExecutor

trait GlobalExecutionContext {
  implicit val executor: ExecutionContextExecutor = Pools.globalExecutor
}
