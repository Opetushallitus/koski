package fi.oph.koski.preferences

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.KeyValue
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class PreferencesServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  private val service = PreferencesService(application.masterDatabase.db)

  put("/:organisaatioOid/:type") {
    withJsonBody({ body =>
      val organisaatioOid = params("organisaatioOid")
      val KeyValue(key, value) = Json.fromJValue[KeyValue](body)
      val `type` = params("type")

      renderStatus(service.put(organisaatioOid, `type`, key, value)(koskiSession))
    })()
  }

  get("/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    renderEither(service.get(organisaatioOid, `type`)(koskiSession))
  }
}
