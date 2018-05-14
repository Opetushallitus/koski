package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class RaportointikantaServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ObservableSupport with ContentEncodingSupport {

  before() {
    if (request.getRemoteHost != "127.0.0.1") {
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
  }

  get("/clear") {
    logger.info("Clearing raportointikanta...")
    application.raportointiDatabase.dropAndCreateSchema
    renderObject(Map("ok" -> true))
  }

  get("/opiskeluoikeudet") {
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSession.systemUser

    val filters = OpiskeluoikeusQueryFilter.parse(params.filterKeys(!List().contains(_)).toList)(application.koodistoViitePalvelu, application.organisaatioRepository, systemUser)
    if (filters.isLeft) {
      haltWithStatus(filters.left.get)
    }
    val loadResults = OpiskeluoikeusLoader.loadOpiskeluoikeudet(application.opiskeluoikeusQueryRepository, filters.right.get, systemUser, application.raportointiDatabase)
    streamResponse[LoadResult](loadResults, systemUser)
  }

  get("/henkilot") {
    val count = HenkilöLoader.loadHenkilöt(application.raportointiDatabase, application.opintopolkuHenkilöFacade)
    renderObject(Map("count" -> count))
  }

  get("/organisaatiot") {
    val count = OrganisaatioLoader.loadOrganisaatiot(application.organisaatioRepository, application.raportointiDatabase)
    renderObject(Map("count" -> count))
  }

  get("/koodistot") {
    val count = KoodistoLoader.loadKoodistot(application.koodistoPalvelu, application.raportointiDatabase)
    renderObject(Map("count" -> count))
  }
}
