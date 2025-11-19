package fi.oph.koski.cache

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class CacheServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with Unauthenticated with Logging with NoCache {
  get("/invalidate", request.getRemoteHost == "127.0.0.1") {
    application.cacheManager.invalidateAllCaches()
    contentType = "text/plain"
    response.writer.print("Caches invalidated")
  }
}
