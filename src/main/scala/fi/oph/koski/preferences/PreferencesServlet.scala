package fi.oph.koski.preferences

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.StorablePreference
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.JValue

class PreferencesServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  private val service = PreferencesService(application.masterDatabase.db)

  put("/:organisaatioOid/:type") {
    withJsonBody({ body =>
      val organisaatioOid = params("organisaatioOid")
      val KeyValue(key, value) = JsonSerializer.extract[KeyValue](body)
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
    renderEither[List[StorablePreference]](service.get(organisaatioOid, `type`)(koskiSession))
  }
}

case class KeyValue(key: String, value: JValue)
