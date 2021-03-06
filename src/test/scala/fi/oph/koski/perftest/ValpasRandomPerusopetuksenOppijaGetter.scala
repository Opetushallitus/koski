package fi.oph.koski.perftest

object ValpasRandomPerusopetuksenOppijaGetter extends App {
  PerfTestRunner.executeTest(ValpasRandomPerusopetuksenOppijaGetterScenario)
}

object ValpasRandomPerusopetuksenOppijaGetterScenario extends PerfTestScenario {
  val oids = new RandomValpasOppijaOid()
  oids.next // To check before start
  def operation(x: Int) = List(Operation(uri = s"valpas/api/oppija/${oids.next}", uriPattern=Some("valpas/api/oppija/_")))
}


