package fi.oph.koski.preferences

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.JValue

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

  delete("/:organisaatioOid/:type/:key") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    val key = params("key")
    renderStatus(service.delete(organisaatioOid, `type`, key)(koskiSession))
  }

  get("/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    renderEither(service.get(organisaatioOid, `type`)(koskiSession))
  }
}

case class KeyValue(key: String, value: JValue)