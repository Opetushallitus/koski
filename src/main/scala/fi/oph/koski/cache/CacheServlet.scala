package fi.oph.koski.cache

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class CacheServlet(val application: KoskiApplication) extends ApiServlet with Unauthenticated with Logging with NoCache {
  get("/invalidate") {
    application.cacheManager.invalidateAllCaches
    "Caches invalidated"
  }
}
