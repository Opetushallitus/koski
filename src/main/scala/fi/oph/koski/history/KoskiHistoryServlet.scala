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

  get("/:oid") {
    val oid: String = getStringParam("oid")
    renderOption(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      val history = application.historyRepository.findByOpiskeluoikeusOid(oid)(koskiSession)
      history.foreach { _ => logHistoryView(oid)}
      history
    }
  }

  get("/:oid/:version") {
    val oid = getStringParam("oid")
    val version = getIntegerParam("version")

    val result: Either[HttpStatus, Opiskeluoikeus] = application.historyRepository.findVersion(oid, version)(koskiSession)

    result.right.foreach { _ => logHistoryView(oid)}

    renderEither(result)
  }

  private def logHistoryView(oid: String): Unit = {
    AuditLog.log(AuditLogMessage(KoskiOperation.MUUTOSHISTORIA_KATSOMINEN, koskiSession, Map(KoskiMessageField.opiskeluoikeusOid -> oid)))
  }
}
