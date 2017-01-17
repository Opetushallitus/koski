package fi.oph.koski.perftest

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.log.Logging

object RandomOpiskeluoikeusGetter extends KoskidevHttpSpecification with Logging with App {
  PerfTestExecutor(
    threadCount = 1,
    operation = { x => List(Operation(uri = s"api/oppija/${RandomOppijaOid.nextOid}", headers = (authHeaders() ++ jsonContent ++ Map("Cookie" -> s"SERVERID=koski-app${x % 2 + 1}"))))}
  ).executeTest
}

