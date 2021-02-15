package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{PaginatedResponse, Pagination, PaginationSettings, SortOrder}

class TiedonsiirtoServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache with Pagination {

  get("/") {
    renderEither[PaginatedResponse[Tiedonsiirrot]](application.tiedonsiirtoService.haeTiedonsiirrot(parseQuery)(koskiSession))
  }

  get("/virheet") {
    renderEither[PaginatedResponse[Tiedonsiirrot]](application.tiedonsiirtoService.virheelliset(parseQuery)(koskiSession))
  }

  get[Seq[TiedonsiirtoYhteenveto]]("/yhteenveto") {
    application.tiedonsiirtoService.yhteenveto(koskiSession, SortOrder.parseSortOrder(params.get("sort"), Ascending("oppilaitos")))
  }

  post("/delete") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[TiedonsiirtoDeleteRequest](body)
      application.tiedonsiirtoService.delete(request.ids)(koskiSession)
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

case class TiedonsiirtoDeleteRequest(ids: List[String])
