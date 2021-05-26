package fi.oph.koski.perftest

object ValpasRandomPerusopetuksenOppilaitoksetGetterScenario extends PerfTestScenario {
  val oids = new RandomValpasPeruskouluOid()
  oids.next // To check before start
  def operation(x: Int) = List(Operation(uri = s"valpas/api/oppijat/${oids.next}", uriPattern=Some("valpas/api/oppijat/_")))
}

object ValpasRandomPerusopetuksenOppilaitoksetGetter extends App {
  PerfTestRunner.executeTest(ValpasRandomPerusopetuksenOppilaitoksetGetterScenario)
}

