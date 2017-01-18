package fi.oph.koski.perftest

import fi.oph.koski.log.Logging

object RandomOpiskeluoikeusGetter extends KoskiPerfTester with Logging with App {
  val oids = RandomOppijaOid(Math.min(roundCount, 500000))
  def operation(x: Int) = List(Operation(uri = s"api/oppija/${oids.nextOid}"))

  executeTest
}

