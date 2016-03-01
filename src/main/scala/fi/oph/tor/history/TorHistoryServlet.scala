package fi.oph.tor.history

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.log.Logging
import fi.oph.tor.schema.OpiskeluOikeus
import fi.oph.tor.servlet.{ErrorHandlingServlet, NoCache}
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient
import org.json4s.jackson.JsonMethods

class TorHistoryServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication with JsonMethods with NoCache {

  get("/:id") {
    renderOption(TorErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      historyRepository.findByOpiskeluoikeusId(getIntegerParam("id"))(torUser)
    }
  }

  get("/:id/:version") {
    val id = getIntegerParam("id")
    val version = getIntegerParam("version")

    val result: Either[HttpStatus, OpiskeluOikeus] = historyRepository.findVersion(id, version)(torUser)

    renderEither(result)
  }
}
