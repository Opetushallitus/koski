package fi.oph.tor.history

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.log._
import fi.oph.tor.schema.Opiskeluoikeus
import fi.oph.tor.servlet.{ApiServlet, NoCache}
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient
import org.json4s.jackson.JsonMethods

class TorHistoryServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ApiServlet with RequiresAuthentication with JsonMethods with NoCache {

  get("/:id") {
    val id: Int = getIntegerParam("id")
    renderOption(TorErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      val history = historyRepository.findByOpiskeluoikeusId(id)(torUser)
      history.foreach { _ => logHistoryView(id)}
      history
    }
  }

  get("/:id/:version") {
    val id = getIntegerParam("id")
    val version = getIntegerParam("version")

    val result: Either[HttpStatus, Opiskeluoikeus] = historyRepository.findVersion(id, version)(torUser)

    result.right.foreach { _ => logHistoryView(id)}

    renderEither(result)
  }

  private def logHistoryView(id: Int): Unit = {
    AuditLog.log(AuditLogMessage(TorOperation.MUUTOSHISTORIA_KATSOMINEN, torUser, Map(TorMessageField.opiskeluOikeusId -> id.toString)))
  }
}
