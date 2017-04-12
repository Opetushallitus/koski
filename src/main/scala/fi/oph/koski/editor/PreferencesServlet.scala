package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{KoskiDatabaseMethods, PreferenceRow, Tables}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import org.json4s.JValue

class PreferencesServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  private val service = new PreferencesService(application.database.db)

  put("/:organisaatioOid/:type") {
    withJsonBody({ body =>
      val organisaatioOid = params("organisaatioOid")
      val KeyValue(key, value) = Json.fromJValue[KeyValue](body)
      val `type` = params("type")
      service.put(organisaatioOid, `type`, key, value)(koskiSession)
    })()
    renderStatus(HttpStatus.ok)
  }

  get("/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    service.get(organisaatioOid, `type`)(koskiSession)
  }

  // TODO: add restrictions to allowed keys and data size
}

class PreferencesService(protected val db: DB) extends Logging with KoskiDatabaseMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  def put(organisaatioOid: String, `type`: String, key: String, value: JValue)(implicit session: KoskiSession) = {
    if (!session.hasWriteAccess(organisaatioOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())
    runDbSync(Tables.Preferences.insertOrUpdate(PreferenceRow(organisaatioOid, `type`, key, value)))
  }

  def get(organisaatioOid: String, `type`: String)(implicit session: KoskiSession): List[JValue] = {
    if (!session.hasWriteAccess(organisaatioOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())
    runDbSync(Tables.Preferences.filter(r => r.organisaatioOid === organisaatioOid).map(_.value).result).toList
  }
}

case class KeyValue(key: String, value: JValue)
