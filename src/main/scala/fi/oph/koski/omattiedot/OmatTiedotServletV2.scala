package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresKansalainen}
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServletV2(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresKansalainen with NoCache {
  private val huoltajaService = application.huoltajaService

  get("/oppija") {
    renderOmatTiedot
  }

  get("/oppija/:oid") {
    val oid = params("oid")
    if (oid == session.user.oid) {
      renderOmatTiedot
    } else {
      renderHuollettavanTiedot(oid)
    }
  }

  private def renderOmatTiedot: Unit = {
    val omatTiedotSession = KoskiSpecificSession.omatTiedotSession(session)
    renderEither[Oppija](
      huoltajaService
        .findUserOppijaAllowEmpty(omatTiedotSession)
        .map(_.getIgnoringWarnings),
      omatTiedotSession
    )
  }

  // Huollettavan tiedot näytetään kansalaisen omalla sessiolla, eli ilman omien tietojen
  // erityisiä henkilötietoja – kuten vanhassa käyttöliittymässä (OmatTiedotServlet).
  private def renderHuollettavanTiedot(oid: String): Unit = {
    if (!session.isUsersHuollettava(oid)) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }
    renderEither[Oppija](
      huoltajaService
        .findHuollettavaOppija(oid)(session)
        .map(_.getIgnoringWarnings),
      session
    )
  }
}
