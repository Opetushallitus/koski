package fi.oph.koski.db

import fi.oph.koski.executors.Pools

trait BackgroundExecutionContext {
  implicit val executor = Pools.backgroundExecutor
}
