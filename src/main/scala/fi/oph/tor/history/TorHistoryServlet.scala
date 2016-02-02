package fi.oph.tor.history

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.servlet.{NoCache, InvalidRequestException, ErrorHandlingServlet}
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient
import fi.oph.tor.log.Logging
import org.json4s.jackson.JsonMethods

class TorHistoryServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication with JsonMethods with NoCache {

  get("/:id") {
    renderOption(TorErrorCategory.notFound.notFoundOrNoPermission) {
      historyRepository.findByOpiskeluoikeusId(getIntegerParam("id"))
    }
  }

  get("/:id/:version") {
    val id = getIntegerParam("id")
    val version = getIntegerParam("version")

    renderEither(historyRepository.findVersion(id, version))
  }

  private def getIntegerParam(name: String): Int = {
    params.getAs[Int](name) match {
      case Some(id) if id > 0 => id
      case _ => throw new InvalidRequestException(TorErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }
}
