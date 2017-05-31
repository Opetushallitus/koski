package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}

import scala.util.{Failure, Success, Try}

class LocalizationServlet (val application: KoskiApplication) extends ApiServlet with Unauthenticated with NoCache {
  get("/") {
    application.localizationRepository.localizations()
  }

  put("/") {
    withJsonBody { body =>
      Try(Json.fromJValue[List[LocalizationRequest]](body)) match {
        case Success(req) =>
          application.localizationRepository.createOrUpdate(req.map(_.toUpdateLocalization))
        case Failure(e) =>
          haltWithStatus(
            KoskiErrorCategory.badRequest("Localization request is in wrong format. Example of a valid request: " + Json.write(List(LocalizationRequest("fi", "my.localization.key", "My localized message"))))
          )
      }
    } ()
  }
}

case class LocalizationRequest(
  locale: String,
  key: String,
  value: String
) {
  def toUpdateLocalization = UpdateLocalization(locale, key, value)
}


