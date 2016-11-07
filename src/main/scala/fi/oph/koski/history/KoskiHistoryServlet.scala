package fi.oph.koski.history

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log._
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.jackson.JsonMethods

class KoskiHistoryServlet(val application: KoskiApplication)
  extends ApiServlet with RequiresAuthentication with JsonMethods with NoCache {

  get("/:id") {
    val id: Int = getIntegerParam("id")
    renderOption(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      val history = application.historyRepository.findByOpiskeluoikeusId(id)(koskiSession)
      history.foreach { _ => logHistoryView(id)}
      history
    }
  }

  get("/:id/:version") {
    val id = getIntegerParam("id")
    val version = getIntegerParam("version")

    val result: Either[HttpStatus, Opiskeluoikeus] = application.historyRepository.findVersion(id, version)(koskiSession)

    result.right.foreach { _ => logHistoryView(id)}

    renderEither(result)
  }

  private def logHistoryView(id: Int): Unit = {
    AuditLog.log(AuditLogMessage(KoskiOperation.MUUTOSHISTORIA_KATSOMINEN, koskiSession, Map(KoskiMessageField.opiskeluOikeusId -> id.toString)))
  }
}
