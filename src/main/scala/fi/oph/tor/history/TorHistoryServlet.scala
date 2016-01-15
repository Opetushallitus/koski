package fi.oph.tor.history

import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.security.ldap.DirectoryClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.JsonMethods

class TorHistoryServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication with JsonMethods {

  get("/:id") {
    renderOption {
      historyRepository.findByOpiskeluoikeusId(params("id").toInt)
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
      case _ => throw new InvalidRequestException("Invalid " + name + " : " + params(name))
    }
  }
}
