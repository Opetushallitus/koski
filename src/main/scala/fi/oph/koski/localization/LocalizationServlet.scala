package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.common.json.JsonSerializer
import fi.oph.common.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{ApiServlet, NoCache}

import scala.util.{Failure, Success, Try}

class LocalizationServlet(implicit val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache {
  get("/") {
    application.localizationRepository.localizations
  }

  put("/") {
    requireVirkailijaOrPalvelukäyttäjä
    if (koskiSessionOption.filter(_.hasLocalizationWriteAccess).isEmpty) {
      haltWithStatus(KoskiErrorCategory.forbidden("Ei oikeuksia muokata tekstejä"))
    }

    withJsonBody { body =>
      Try(JsonSerializer.extract[List[LocalizationRequest]](body)) match {
        case Success(req) =>
          application.localizationRepository.createOrUpdate(req.map(_.toUpdateLocalization))
          logger.info("Lokalisoitujen tekstien muutos: " + JsonSerializer.writeWithRoot(req))
        case Failure(e) =>
          haltWithStatus(
            KoskiErrorCategory.badRequest("Localization request is in wrong format. Example of a valid request: " + JsonSerializer.writeWithRoot(List(LocalizationRequest("fi", "my.localization.key", "My localized message"))))
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


