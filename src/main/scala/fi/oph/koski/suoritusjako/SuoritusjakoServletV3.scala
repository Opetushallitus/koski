package fi.oph.koski.suoritusjako

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

class SuoritusjakoServletV3(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with KoskiSpecificAuthenticationSupport
    with Logging
    with NoCache {

  before() {
    noRemoteCalls()
  }

  post("/") {
    withJsonBody { (json: JValue) => {
      implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
      val jako = application
        .validatingAndResolvingExtractor
        .extract[SuoritusjakoRequest](strictDeserialization)(json)
        .flatMap(r => application.suoritusjakoService.get(r.secret)(suoritusjakoUser))
        .map(_.getIgnoringWarnings)
      renderEither[Oppija](jako)
    } } (parseErrorHandler = handleUnparseableJson)
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    haltWithStatus(status)
  }
}
