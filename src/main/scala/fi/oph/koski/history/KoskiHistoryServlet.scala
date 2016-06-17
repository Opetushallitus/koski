package fi.oph.koski.history

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.koski.log._
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.vm.sade.security.ldap.DirectoryClient
import org.json4s.jackson.JsonMethods

class KoskiHistoryServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ApiServlet with RequiresAuthentication with JsonMethods with NoCache {

  get("/:id") {
    val id: Int = getIntegerParam("id")
    renderOption(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      val history = historyRepository.findByOpiskeluoikeusId(id)(koskiUser)
      history.foreach { _ => logHistoryView(id)}
      history
    }
  }

  get("/:id/:version") {
    val id = getIntegerParam("id")
    val version = getIntegerParam("version")

    val result: Either[HttpStatus, Opiskeluoikeus] = historyRepository.findVersion(id, version)(koskiUser)

    result.right.foreach { _ => logHistoryView(id)}

    renderEither(result)
  }

  private def logHistoryView(id: Int): Unit = {
    AuditLog.log(AuditLogMessage(KoskiOperation.MUUTOSHISTORIA_KATSOMINEN, koskiUser, Map(KoskiMessageField.opiskeluOikeusId -> id.toString)))
  }
}
