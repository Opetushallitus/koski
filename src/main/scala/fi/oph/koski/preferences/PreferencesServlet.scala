package fi.oph.koski.preferences

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.StorablePreference
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

class PreferencesServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  private val service = PreferencesService(application.masterDatabase.db)

  put("/:organisaatioOid/:type") {
    withJsonBody({ body =>
      val KeyValue(key, value) = JsonSerializer.extract[KeyValue](body)
      renderStatus(service.put(organisaatioOid, koulutustoimijaOid, `type`, key, value)(session))
    })()
  }

  delete("/:organisaatioOid/:type/:key") {
    renderStatus(service.delete(organisaatioOid, koulutustoimijaOid, `type`, params("key"))(session))
  }

  get("/:organisaatioOid/:type") {
    renderEither[Seq[StorablePreference]](service.get(organisaatioOid, koulutustoimijaOid, `type`)(session))
  }

  private def organisaatioOid = params("organisaatioOid")
  private def `type` = params("type")
  private def koulutustoimijaOid = params.get("koulutustoimijaOid")
}

case class KeyValue(key: String, value: JValue)
