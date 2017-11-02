package fi.oph.koski.db

import fi.oph.koski.util.Pools

trait BackgroundExecutionContext {
  implicit val executor = Pools.backgroundExecutor
}
