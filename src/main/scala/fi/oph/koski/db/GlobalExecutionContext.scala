package fi.oph.koski.db

import fi.oph.koski.util.Pools

trait GlobalExecutionContext {
  implicit val executor = Pools.globalExecutor
}