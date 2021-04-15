package fi.oph.koski.executors

trait GlobalExecutionContext {
  implicit val executor = Pools.globalExecutor
}
