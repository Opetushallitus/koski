package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class LocalizationServlet (val application: KoskiApplication) extends ApiServlet with Unauthenticated with NoCache {
  get("/") {
    application.localizationRepository.localizations()
  }

  put("/") {
    withJsonBody { body =>
      val req = Json.fromJValue[List[LocalizationRequest]](body)
      application.localizationRepository.createOrUpdate(req.map(_.toUpdateLocalization))
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


