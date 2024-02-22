package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.UuidUtils
import org.json4s.jackson.JsonMethods

import java.time.LocalDateTime

class KyselyServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with JsonMethods with NoCache
{
  val kyselyt: KyselyService = application.kyselyService
  val rootUrl: String = application.config.getString("koski.root.url")

  post("/") {
    withJsonBody { body =>
      renderEither {
        application
          .validatingAndResolvingExtractor
          .extract[QueryParameters](strictDeserialization)(body)
          .flatMap(kyselyt.add)
          .map(q => QueryResponse(rootUrl, q).withResponseUrl(rootUrl))
      }
    } (parseErrorHandler = jsonErrorHandler)
  }

  get("/:id") {
    renderEither {
      UuidUtils.optionFromString(getStringParam("id"))
        .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
        .flatMap(kyselyt.get)
        .map(q => QueryResponse(rootUrl, q))
    }
  }

  get("/:id/:file") {
    UuidUtils.optionFromString(getStringParam("id"))
      .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
      .flatMap(kyselyt.get)
      .flatMap {
        case q: CompleteQuery =>
          kyselyt.getDownloadUrl(q, getStringParam("file"))
            .toRight(KoskiErrorCategory.badRequest("Tiedostoa ei löydy tai tapahtui virhe sen jakamisessa"))
        case _ =>
          Left(KoskiErrorCategory.badRequest("Tulostiedostot eivät ole vielä ladattavissa"))
      }
      .fold(renderStatus, redirect)
  }

  private def jsonErrorHandler(status: HttpStatus) = {
    haltWithStatus(status)
  }
}

case class QueryResponse(
  queryId: String,
  @EnumValues(QueryState.*)
  status: String,
  requestedBy: String,
  query: QueryParameters,
  creationTime: LocalDateTime,
  workStartTime: Option[LocalDateTime] = None,
  endTime: Option[LocalDateTime] = None,
  resultsApi: Option[String] = None,
  files: Option[List[String]] = None,
) {
  def withResponseUrl(rootUrl: String): QueryResponse = copy(
    resultsApi = Some(s"$rootUrl/api/kyselyt/${queryId}")
  )
  def withFiles(rootUrl: String, query: CompleteQuery): QueryResponse = copy(
    files = Some(query.resultFiles.map(name => s"$rootUrl/api/kyselyt/${query.queryId}/$name"))
  )
}

object QueryResponse {
  def apply(rootUrl: String, query: Query): QueryResponse = query match {
    case q: PendingQuery => QueryResponse(q.queryId, QueryState.pending, q.userOid, q.query, q.createdAt)
    case q: RunningQuery => QueryResponse(q.queryId, QueryState.running, q.userOid, q.query, q.createdAt, Some(q.startedAt))
    case q: FailedQuery => QueryResponse(q.queryId, QueryState.failed, q.userOid, q.query, q.createdAt, Some(q.startedAt), Some(q.finishedAt))
    case q: CompleteQuery => QueryResponse(q.queryId, QueryState.complete, q.userOid, q.query, q.createdAt, Some(q.startedAt), Some(q.finishedAt)).withFiles(rootUrl, q)
  }
}
