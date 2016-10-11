package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.schema.OrganisaatioOid
import fi.oph.koski.servlet.{ApiServlet, CacheControlSupport}
import scala.concurrent.duration._

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with CacheControlSupport {
  get() {
    cacheMaxAge(5 minutes)
    renderEither(application.tiedonsiirtoService.haeTiedonsiirrot(parseQuery)(koskiUser))
  }

  get("/virheet") {
    cacheMaxAge(5 minutes)
    renderEither(application.tiedonsiirtoService.virheelliset(parseQuery)(koskiUser))
  }

  get("/yhteenveto") {
    cacheMaxAge(5 minutes)
    application.tiedonsiirtoService.yhteenveto(koskiUser)
  }

  private def parseQuery = TiedonsiirtoQuery(params.get("oppilaitos").map(oid => OrganisaatioOid.validateOrganisaatioOid(oid) match {
    case Right(oid) => oid
    case Left(status) => haltWithStatus(status)
  }))
}
