package fi.oph.koski.db

import fi.oph.koski.executors.Pools

trait DatabaseExecutionContext {
  implicit val executor = Pools.databaseExecutor
}
