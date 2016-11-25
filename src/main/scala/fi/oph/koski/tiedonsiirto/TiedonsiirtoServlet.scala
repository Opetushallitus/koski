package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.schema.OrganisaatioOid
import fi.oph.koski.servlet.{ApiServlet, Cached, UserSessionCached}
import fi.oph.koski.util.Paging

import scala.concurrent.duration._

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with UserSessionCached with Paging {
  get() {
    renderEither(application.tiedonsiirtoService.haeTiedonsiirrot(parseQuery)(koskiSession))
  }

  get("/virheet") {
    renderEither(application.tiedonsiirtoService.virheelliset(parseQuery)(koskiSession))
  }

  get("/yhteenveto") {
    application.tiedonsiirtoService.yhteenveto(koskiSession)
  }

  private def parseQuery = {
    val oppilaitos: Option[String] = params.get("oppilaitos").map(oid => OrganisaatioOid.validateOrganisaatioOid(oid) match {
      case Right(oid) => oid
      case Left(status) => haltWithStatus(status)
    })
    TiedonsiirtoQuery(oppilaitos, pageInfo)
  }

  override def cacheDuration = 5 minutes
}
