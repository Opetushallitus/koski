package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.OrganisaatioOid
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{Pagination, PaginationSettings, SortOrder}

class TiedonsiirtoServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache with Pagination {

  get("/") {
    renderEither(application.tiedonsiirtoService.haeTiedonsiirrot(parseQuery)(koskiSession))
  }

  get("/virheet") {
    renderEither(application.tiedonsiirtoService.virheelliset(parseQuery)(koskiSession))
  }

  get("/yhteenveto") {
    application.tiedonsiirtoService.yhteenveto(koskiSession, SortOrder.parseSortOrder(params.get("sort"), Ascending("oppilaitos")))
  }

  post("/delete") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[TiedonsiirtoDeleteRequest](body)
      application.tiedonsiirtoService.delete(request.id)(koskiSession)
    })()
  }

  private def parseQuery = {
    val oppilaitos: Option[String] = params.get("oppilaitos").map(oid => OrganisaatioOid.validateOrganisaatioOid(oid) match {
      case Right(oid) => oid
      case Left(status) => haltWithStatus(status)
    })
    TiedonsiirtoQuery(oppilaitos, Some(paginationSettings.getOrElse(PaginationSettings(0, 100))))
  }
}

case class TiedonsiirtoDeleteRequest(id: String)
