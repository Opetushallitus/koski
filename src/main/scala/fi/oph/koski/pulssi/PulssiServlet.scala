package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class PulssiServlet(val application: KoskiApplication) extends ApiServlet with NoCache with AuthenticationSupport {
  get("/") {
    Map(
      "opiskeluoikeudet" -> pulssi.opiskeluoikeusTilasto,
      "metriikka" -> pulssi.metriikka,
      "oppilaitosMäärät" -> pulssi.oppilaitosMäärät
    )
  }

  private def pulssi = application.koskiPulssi
}

