package fi.oph.tor.cache

import fi.oph.tor.config.TorApplication
import fi.oph.tor.log.Logging
import fi.oph.tor.servlet.ApiServlet
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class CacheServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, application: TorApplication) extends ApiServlet with RequiresAuthentication with Logging {
  get("/invalidate") { // TODO: require superuser privileges
    logger.info("Invalidating all caches")
    application.invalidateCaches
    "Caches invalidated"
  }
}
