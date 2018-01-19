package fi.oph.koski.henkilo

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.util.Timing
import org.scalatra._

class HenkilötiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with ContentEncodingSupport with NoCache with Timing {
  private val henkilötiedotFacade = HenkilötiedotFacade(application.henkilöRepository, application.opiskeluoikeusRepository, application.possu)

  get("/search") {
    params.get("query") match {
      case Some(query) if query.length >= 3 =>
        henkilötiedotFacade.search(query.toUpperCase)(koskiSession)

      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }


  get("/hetu/:hetu") {
    renderEither(henkilötiedotFacade.findByHetu(params("hetu"))(koskiSession))
  }

  get("/oid/:oid") {
    renderEither(henkilötiedotFacade.findByOid(params("oid"))(koskiSession).right.map(_.map(_.copy(hetu = None)))) // poistetaan hetu tuloksista, sillä käytössä ei ole organisaatiorajausta
  }
}
