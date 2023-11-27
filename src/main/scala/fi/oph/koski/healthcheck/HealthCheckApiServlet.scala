package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class HealthCheckApiServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  private val radiator: RadiatorService = new RadiatorService(application)
  private val radiatorApiKey = try { Some(application.config.getString("radiator.apiKey")) } catch { case _: Any => None }

  get("/") {
    renderStatus(application.healthCheck.healthcheckWithExternalSystems)
  }

  get("/internal") {
    renderStatus(application.healthCheck.internalHealthcheck)
  }

  get("/radiator") {
    // TODO: Kirjoita tämä uudelleenkäytettäväksi autentikointistrategiaksi, jos lisätään api-avain-tunnistautuminen joskus johonkin muuallekin
    renderEither((radiatorApiKey, request.headers.get("Authorization")) match {
      case (None, _) => Left(KoskiErrorCategory.unauthorized())
      case (_, None) => Left(KoskiErrorCategory.unauthorized())
      case (_, Some(key)) if key != s"Bearer ${radiatorApiKey.get}" => Left(KoskiErrorCategory.unauthorized())
      case _ => Right(radiator.getHealth)
    })
  }
}
