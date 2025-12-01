package fi.oph.koski.history

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log._
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.jackson.JsonMethods

class KoskiHistoryServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with JsonMethods with NoCache {

  get("/:oid") {
    val oid: String = getStringParam("oid")
    renderOption[List[OpiskeluoikeusHistoryPatch]](KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia) {
      val history: Option[List[OpiskeluoikeusHistoryPatch]] = application.historyRepository.findByOpiskeluoikeusOid(oid)(session)
      history.foreach { _ => logHistoryView(oid)}
      history
    }
  }

  get("/:oid/:version") {
    val oid = getStringParam("oid")
    val version = getIntegerParam("version")

    val result: Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = application.historyRepository.findVersion(oid, version)(session)

    result.foreach { _ => logHistoryView(oid)}

    renderEither[KoskeenTallennettavaOpiskeluoikeus](result)
  }

  private def logHistoryView(oid: String): Unit = {
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.MUUTOSHISTORIA_KATSOMINEN, session, Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)))
  }
}
