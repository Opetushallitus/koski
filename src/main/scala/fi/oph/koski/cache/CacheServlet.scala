package fi.oph.koski.cache

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.security.ldap.DirectoryClient

class CacheServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging {
  get("/invalidate") {
    if (!koskiUser.isMaintenance) {
      halt(403)
    }
    logger.info("Invalidating all caches")
    application.invalidateCaches
    "Caches invalidated"
  }
}
