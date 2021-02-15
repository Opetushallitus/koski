package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{ApiServlet, KoskiSpecificApiServlet, NoCache}

import scala.util.{Failure, Success, Try}

class KoskiSpecificLocalizationServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with NoCache {
  get("/") {
    application.koskiLocalizationRepository.localizations
  }

  put("/") {
    requireVirkailijaOrPalvelukäyttäjä
    if (koskiSessionOption.filter(_.hasLocalizationWriteAccess).isEmpty) {
      haltWithStatus(KoskiErrorCategory.forbidden("Ei oikeuksia muokata tekstejä"))
    }

    withJsonBody { body =>
      Try(JsonSerializer.extract[List[LocalizationRequest]](body)) match {
        case Success(req) =>
          application.koskiLocalizationRepository.createOrUpdate(req.map(_.toUpdateLocalization))
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
  def toUpdateLocalization = UpdateLocalization(locale, key, value, "koski")
}
