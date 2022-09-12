package fi.oph.koski.perftest

import org.json4s.JField
import org.json4s.jackson.JsonMethods

object ValpasRandomPerusopetuksenOppijaGetter extends App {
  PerfTestRunner.executeTest(ValpasRandomPerusopetuksenOppijaGetterScenario)
}

object ValpasRandomPerusopetuksenOppijaGetterScenario extends PerfTestScenario {
  val oids = new RandomValpasOppijaOid()
  oids.next // To check before start
  def operation(x: Int) = List(Operation(uri = s"valpas/api/oppija/${oids.next}", uriPattern=Some("valpas/api/oppija/_")))
  override def bodyValidator: Boolean = {
    val bodyContainsError = this.body.contains("hakutilanneError")
    val bodyContainsValintaTila = JsonMethods.parse(this.body) findField {
      case JField("valintatila", _) => true
      case _ => false
    } match {
      case Some(_) => true
      case None => false
    }
    val hasError = bodyContainsError || !bodyContainsValintaTila

    if (hasError) {
      logger.error(this.body)
    }

    !hasError
  }
}


