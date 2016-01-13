package fi.oph.tor.history

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.tor.http.HttpStatus
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

    renderEither {
      historyRepository.findByOpiskeluoikeusId(id, version) match {
        case Some(diffs) =>
          if (diffs.length < version) {
            Left(HttpStatus.notFound("Version: " + version + " not found for opiskeluoikeus: " + id))
          } else {
            val oikeusVersion = diffs.foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
              val patch = JsonPatch.fromJson(asJsonNode(diff.muutos))
              patch.apply(current)
            }
            Right(fromJsonNode(oikeusVersion))
          }
        case None => Left(HttpStatus.notFound("Opiskeluoikeus not found with id: " + id))
      }
    }
  }

  private def getIntegerParam(name: String): Int = {
    params.getAs[Int](name) match {
      case Some(id) if id > 0 => id
      case _ => throw new InvalidRequestException("Invalid " + name + " : " + params(name))
    }
  }
}
