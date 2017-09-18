package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.perustiedot.OpiskeluoikeusTilasto
import fi.oph.koski.servlet.{ApiServletWithSchemaBasedSerialization, NoCache}

class PulssiServlet(implicit val application: KoskiApplication) extends ApiServletWithSchemaBasedSerialization with NoCache with AuthenticationSupport {
  get("/") {
    KoskiPulssiData(pulssi.opiskeluoikeusTilasto, pulssi.metriikka, pulssi.oppilaitosMäärät)
  }

  private def pulssi = application.koskiPulssi
}

case class KoskiPulssiData(opiskeluoikeudet: OpiskeluoikeusTilasto, metriikka: JulkinenMetriikka, oppilaitosMäärät: OppilaitosMäärät)