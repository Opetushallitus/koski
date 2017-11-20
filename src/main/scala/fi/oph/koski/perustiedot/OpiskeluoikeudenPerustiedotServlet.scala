package fi.oph.koski.perustiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{PaginatedResponse, Pagination, PaginationSettings, SortOrder}

class OpiskeluoikeudenPerustiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Pagination with NoCache {
  // TODO: Pitäisikö näistäkin katseluista tehdä auditlog-merkintä?
  get("/") {
    renderEither({
      val sort = SortOrder.parseSortOrder(params.get("sort"), Ascending("nimi"))

      val thing: Either[HttpStatus, PaginatedResponse[List[OpiskeluoikeudenPerustiedot]]] = OpiskeluoikeusQueryFilter.parse(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
        case Right(filters) =>
          val pagination: PaginationSettings = paginationSettings.getOrElse(PaginationSettings(0, 100))
          val result: List[OpiskeluoikeudenPerustiedot] = application.perustiedotRepository.find(filters, sort, pagination)(koskiSession)
          Right(PaginatedResponse(Some(pagination), result, result.length))
        case Left(HttpStatus(404, _)) =>
          Right(PaginatedResponse(None, List[OpiskeluoikeudenPerustiedot](), 0))
        case Left(err) => Left(err)
      }
      thing
    })
  }
}
