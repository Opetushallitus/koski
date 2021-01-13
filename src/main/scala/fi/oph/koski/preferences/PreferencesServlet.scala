package fi.oph.koski.preferences

import fi.oph.koski.config.KoskiApplication
import fi.oph.common.json.JsonSerializer
import fi.oph.common.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.StorablePreference
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.JValue

class PreferencesServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  private val service = PreferencesService(application.masterDatabase.db)

  put("/:organisaatioOid/:type") {
    withJsonBody({ body =>
      val KeyValue(key, value) = JsonSerializer.extract[KeyValue](body)
      renderStatus(service.put(organisaatioOid, koulutustoimijaOid, `type`, key, value)(koskiSession))
    })()
  }

  delete("/:organisaatioOid/:type/:key") {
    renderStatus(service.delete(organisaatioOid, koulutustoimijaOid, `type`, params("key"))(koskiSession))
  }

  get("/:organisaatioOid/:type") {
    renderEither[List[StorablePreference]](service.get(organisaatioOid, koulutustoimijaOid, `type`)(koskiSession))
  }

  private def organisaatioOid = params("organisaatioOid")
  private def `type` = params("type")
  private def koulutustoimijaOid = params.get("koulutustoimijaOid")
}

case class KeyValue(key: String, value: JValue)
