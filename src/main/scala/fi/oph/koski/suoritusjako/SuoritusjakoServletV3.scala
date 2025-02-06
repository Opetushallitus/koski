package fi.oph.koski.suoritusjako

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

class SuoritusjakoServletV3(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with KoskiSpecificAuthenticationSupport
    with Logging
    with NoCache {

  post("/") {
    withJsonBody { (json: JValue) => {
      val body = JsonSerializer.extract[SuoritusjakoRequest](json)
      (for {
        row <- application.suoritusjakoRepository.get(body.secret)
        session <- getSessionFromRow(row, application.config)
        result <- application.suoritusjakoService.getFromRow(row, session)
      } yield (result, session)) match {
        case Left(status) => haltWithStatus(status)
        case Right((result, session)) => renderObject[OppijaJakolinkillä](result.getIgnoringWarnings, session)
      }
    }
    }()
  }
}
