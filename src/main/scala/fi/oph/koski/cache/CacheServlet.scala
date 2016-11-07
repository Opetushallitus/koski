package fi.oph.koski.cache

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class CacheServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with NoCache {
  get("/invalidate") {
    if (!koskiSession.isMaintenance) {
      halt(403)
    }
    application.cacheManager.invalidateAllCaches
    "Caches invalidated"
  }
}
