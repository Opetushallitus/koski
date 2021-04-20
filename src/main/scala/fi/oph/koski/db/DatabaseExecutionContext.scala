package fi.oph.koski.db

import fi.oph.koski.executors.Pools

import scala.concurrent.ExecutionContextExecutor

trait DatabaseExecutionContext {
  protected final implicit val executor: ExecutionContextExecutor = Pools.databaseExecutor
}
