package fi.oph.koski.perftest

object RandomOpiskeluoikeusGetterScenario extends PerfTestScenario {
  val oids = RandomOppijaOid(Math.min(roundCount, 500000))
  oids.nextOid // To check before start
  def operation(x: Int) = List(Operation(uri = s"api/oppija/${oids.nextOid}", uriPattern=Some("api/oppija/_")))
}

object RandomOpiskeluoikeusGetter extends App {
  PerfTestRunner.executeTest(RandomOpiskeluoikeusGetterScenario)
}

