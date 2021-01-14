package fi.oph.koski.henkilo

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.common.json.JsonSerializer
import fi.oph.common.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.common.log.Logging
import fi.oph.koski.schema.HenkilötiedotJaOid
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.util.Timing
import org.scalatra._

class HenkilötiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with ContentEncodingSupport with NoCache with Timing {
  private val henkilötiedotSearchFacade = HenkilötiedotSearchFacade(application.henkilöRepository, application.opiskeluoikeusRepository, application.possu, application.hetu)

  // note: Koski UI uses the POST version, but this is part of our public API (and apparently used)
  get[HenkilötiedotSearchResponse]("/search") {
    params.get("query") match {
      case Some(query) if query.length >= 3 =>
        henkilötiedotSearchFacade.searchOrPossiblyCreateIfInYtrOrVirta(query.toUpperCase)(koskiSession)
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }

  // uses POST to avoid having potentially sensitive data in URLs
  post[HenkilötiedotSearchResponse]("/search") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[HenkilötiedotSearchRequest](body)
      request.query match {
        case query: String if query.length >= 3 =>
          henkilötiedotSearchFacade.searchOrPossiblyCreateIfInYtrOrVirta(query.toUpperCase)(koskiSession)
        case _ =>
          throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
      }
    })()
  }

  // note: Koski UI uses the POST version, but this is part of our public API (and apparently used)
  get("/hetu/:hetu") {
    renderEither[List[HenkilötiedotJaOid]](henkilötiedotSearchFacade.findByHetuOrCreateIfInYtrOrVirta(params("hetu"))(koskiSession))
  }

  // uses POST to avoid having sensitive data in URLs
  post("/hetu") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[HenkilötiedotHetuRequest](body)
      renderEither[List[HenkilötiedotJaOid]](henkilötiedotSearchFacade.findByHetuOrCreateIfInYtrOrVirta(request.hetu)(koskiSession))
    })()
  }

  get("/oid/:oid") {
    renderEither[List[HenkilötiedotJaOid]](henkilötiedotSearchFacade.findByOid(params("oid"))(koskiSession).right.map(_.map(_.copy(hetu = None)))) // poistetaan hetu tuloksista, sillä käytössä ei ole organisaatiorajausta
  }
}

case class HenkilötiedotSearchRequest(query: String)

case class HenkilötiedotHetuRequest(hetu: String)
