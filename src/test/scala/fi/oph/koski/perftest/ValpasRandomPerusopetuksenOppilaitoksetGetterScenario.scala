package fi.oph.koski.perftest

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.valpas.Oppijalista

import java.nio.charset.Charset

object ValpasRandomPerusopetuksenOppilaitoksetGetterScenario extends PerfTestScenario {
  val HAKUTIEDOT_FETCH_LIST_SIZE = 100

  val oids = new RandomValpasPeruskouluOid()
  oids.next // To check before start
  def operation(x: Int) = {
    val data = oids.next
    List(Operation(uri = s"valpas/api/oppijat/${data.oppilaitos}", uriPattern = Some("valpas/api/oppijat/_"))) ++
      data.oppijat
        .grouped(HAKUTIEDOT_FETCH_LIST_SIZE)
        .map(oppijaOids => Operation(
          method = "POST",
          uri = s"valpas/api/oppijat/${data.oppilaitos}/hakutiedot",
          body = JsonSerializer.writeWithRoot(Oppijalista(oppijaOids)).getBytes(Charset.forName("UTF-8")),
          uriPattern = Some("valpas/api/oppijat/_/hakutiedot"),
        ))
  }

  override def bodyValidator: Boolean = {
    val bodyContainsError = this.body.contains("hakutilanneError")
    if (bodyContainsError) {
      logger.error(this.body)
    }
    !bodyContainsError
  }
}

object ValpasRandomPerusopetuksenOppilaitoksetGetter extends App {
  PerfTestRunner.executeTest(ValpasRandomPerusopetuksenOppilaitoksetGetterScenario)
}

