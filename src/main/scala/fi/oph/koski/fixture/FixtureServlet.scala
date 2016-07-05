package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.security.ldap.DirectoryClient

class FixtureServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  post("/reset") {
    application.resetFixtures
  }
}
