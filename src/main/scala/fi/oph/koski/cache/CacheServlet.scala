package fi.oph.koski.cache

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.ApiServlet

class CacheServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging {
  get("/invalidate") {
    if (!koskiUser.isMaintenance) {
      halt(403)
    }
    application.caches.invalidateAllCaches
    "Caches invalidated"
  }
}
