package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.perustiedot.OpiskeluoikeusTilasto
import fi.oph.koski.servlet.{ApiServlet, KoskiSpecificApiServlet, NoCache}

class PulssiServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with KoskiSpecificAuthenticationSupport {
  get("/") {
    KoskiPulssiData(pulssi.opiskeluoikeusTilasto, pulssi.metriikka, pulssi.oppilaitosMäärät)
  }

  private def pulssi = application.koskiPulssi
}

case class KoskiPulssiData(opiskeluoikeudet: OpiskeluoikeusTilasto, metriikka: JulkinenMetriikka, oppilaitosMäärät: OppilaitosMäärät)
