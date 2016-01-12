package fi.oph.tor.history

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.toruser.{UserOrganisationsRepository, RequiresAuthentication}
import fi.vm.sade.security.ldap.DirectoryClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods

class TorHistoryServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication {

  get("/:id") {
    renderOption {
      historyRepository.findByOpiskeluoikeusId(params("id").toInt)
    }
  }

  get("/:id/:version") {
    renderOption {
      historyRepository.findByOpiskeluoikeusId(params("id").toInt) match {
        case Some(diffs) =>
          val version = diffs.take(params("version").toInt).foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
            val patch: JsonPatch = JsonPatch.fromJson(JsonMethods.asJsonNode(diff.muutos))
            patch.apply(current)
          }
          Some(JsonMethods.fromJsonNode(version))
        case None => None
      }
    }
  }
}
