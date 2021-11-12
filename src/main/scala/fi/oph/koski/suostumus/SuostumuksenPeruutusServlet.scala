package fi.oph.koski.suostumus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.KoskiAuditLogMessageField.{apply => _}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JsonAST.JString
import org.json4s.{JField, JObject}

class SuostumuksenPeruutusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with Logging with NoCache {

  def session: KoskiSpecificSession = koskiSessionOption.get

  post("/:oid") {
    requireKansalainen
    renderStatus(
      application.suostumuksenPeruutusService.peruutaSuostumus(getStringParam("oid"))(session)
    )
  }

  post("/suoritusjakoTekemättä/:oid") {
    requireKansalainen
    renderStatus(
      application.suostumuksenPeruutusService.suoritusjakoTekemättäWithAccessCheck(getStringParam("oid"))(session)
    )
  }

  get("/") {
    if (session.hasGlobalReadAccess) {
      renderObject(
        application.suostumuksenPeruutusService.listaaPerututSuostumukset().map(peruttuOo =>
          JObject(
            JField("Opiskeluoikeuden oid", JString(peruttuOo.oid)),
            JField("Opiskeluoikeuden päättymispäivä", JString(peruttuOo.päättymispäivä.getOrElse("").toString)),
            JField("Suostumus peruttu", JString(peruttuOo.aikaleima.toString))
          )
        )
      )
    } else {
      KoskiErrorCategory.forbidden.vainVirkailija()
    }
  }
}
