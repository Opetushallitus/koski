package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.ApiServlet

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  get() {
    application.tiedonsiirtoService.kaikkiTiedonsiirrot(koskiUser)
  }

  get("/virheet") {
    application.tiedonsiirtoService.virheelliset(koskiUser)
  }
}
