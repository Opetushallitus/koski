package fi.oph.koski.db

import fi.oph.koski.executors.Pools

trait GlobalExecutionContext {
  implicit val executor = Pools.globalExecutor
}