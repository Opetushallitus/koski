package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiCookieAndBasicAuthenticationSupport
import fi.oph.koski.perustiedot.OpiskeluoikeusTilasto
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class PulssiServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with KoskiCookieAndBasicAuthenticationSupport {
  get("/") {
    KoskiPulssiData(pulssi.opiskeluoikeusTilasto, pulssi.metriikka)
  }

  private def pulssi = application.koskiPulssi
}

case class KoskiPulssiData(opiskeluoikeudet: OpiskeluoikeusTilasto, metriikka: JulkinenMetriikka)
