package fi.oph.koski.suostumus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.KoskiAuditLogMessageField.{apply => _}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JsonAST.{JBool, JObject, JString}
import org.json4s.{JField}

class SuostumuksenPeruutusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with Logging with NoCache {

  def session: KoskiSpecificSession = koskiSessionOption.get

  post("/:oid") {
    requireKansalainen
    renderStatus(
      application.suostumuksenPeruutusService.peruutaSuostumus(getStringParam("oid"))(session)
    )
  }

  post("/suoritusjakoTehty/:oid") {
    requireKansalainen
    if (application.suostumuksenPeruutusService.suoritusjakoTekemättäWithAccessCheck(getStringParam("oid"))(session).isOk) {
      renderObject(JObject("tehty" -> JBool(false)))
    } else {
      renderObject(JObject("tehty" -> JBool(true)))
    }
  }

  get("/") {
    if (session.hasGlobalReadAccess) {
      renderObject(
        application.suostumuksenPeruutusService.listaaPerututSuostumukset().map(peruttuOo =>
          JObject(
            JField("Opiskeluoikeuden oid", JString(peruttuOo.oid)),
            JField("Oppijan oid", JString(peruttuOo.oppijaOid)),
            JField("Opiskeluoikeuden päättymispäivä", JString(peruttuOo.päättymispäivä.getOrElse("").toString)),
            JField("Suostumus peruttu", JString(peruttuOo.aikaleima.toString)),
            JField("Oppilaitoksesn oid", JString(peruttuOo.oppilaitosOid.getOrElse(""))),
            JField("Oppilaitoksen nimi", JString(peruttuOo.oppilaitosNimi.getOrElse(""))),
          )
        )
      )
    } else {
      KoskiErrorCategory.forbidden.vainVirkailija()
    }
  }
}
