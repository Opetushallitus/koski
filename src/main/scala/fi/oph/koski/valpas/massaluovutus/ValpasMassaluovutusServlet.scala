package fi.oph.koski.valpas.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.massaluovutus._
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.UuidUtils
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s.jackson.JsonMethods

object ValpasMassaluovutusServletUrls {
  def root(rootUrl: String): String = s"$rootUrl/valpas/api/massaluovutus"
  def query(rootUrl: String, queryId: String): String = s"${root(rootUrl)}/$queryId"
  def file(rootUrl: String, queryId: String, fileKey: String): String = s"${root(rootUrl)}/$queryId/$fileKey"
}

class ValpasMassaluovutusServlet(implicit val application: KoskiApplication)
  extends ValpasApiServlet with RequiresValpasSession with JsonMethods with NoCache
{
  val massaluovutukset: MassaluovutusService = application.massaluovutusService
  val rootUrl: String = application.config.getString("koski.root.url")

  post("/") {
    withJsonBody { body =>
      renderEither {
        application
          .validatingAndResolvingExtractor
          .extract[ValpasMassaluovutusQueryParameters](strictDeserialization)(body)
          .flatMap(massaluovutukset.add)
          .map(q => ValpasMassaluovutusQueryResponse(rootUrl, q, session))
      }
    } (parseErrorHandler = jsonErrorHandler)
  }

  get("/:id") {
    renderEither {
      UuidUtils.optionFromString(getStringParam("id"))
        .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
        .flatMap(massaluovutukset.get)
        .map(q => ValpasMassaluovutusQueryResponse(rootUrl, q, session))
    }
  }

  get("/:id/:file") {
    UuidUtils.optionFromString(getStringParam("id"))
      .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
      .flatMap(massaluovutukset.get)
      .flatMap {
        case q: QueryWithResultFiles =>
          massaluovutukset.getDownloadUrl(q, getStringParam("file"))
        case _ =>
          Left(KoskiErrorCategory.badRequest("Tulostiedostot eivät ole vielä ladattavissa"))
      }
      .fold(renderStatus, redirect)
  }

  private def jsonErrorHandler(status: HttpStatus) = {
    haltWithStatus(status)
  }
}

object ValpasMassaluovutusQueryResponse {
  import java.time.{LocalDateTime, OffsetDateTime}
  import java.util.TimeZone

  private def toOffsetDateTime(dt: LocalDateTime): OffsetDateTime =
    dt.atZone(TimeZone.getDefault.toZoneId).toOffsetDateTime

  def apply(rootUrl: String, query: Query, session: fi.oph.koski.valpas.valpasuser.ValpasSession): QueryResponse = query match {
    case q: PendingQuery => PendingQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = toOffsetDateTime(q.createdAt),
      resultsUrl = ValpasMassaluovutusServletUrls.query(rootUrl, q.queryId),
    )
    case q: RunningQuery => RunningQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = toOffsetDateTime(q.createdAt),
      startedAt = toOffsetDateTime(q.startedAt),
      files = q.resultFiles.map(f => ValpasMassaluovutusServletUrls.file(rootUrl, q.queryId, f)),
      resultsUrl = ValpasMassaluovutusServletUrls.query(rootUrl, q.queryId),
      progress = q.progress,
    )
    case q: FailedQuery => FailedQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = toOffsetDateTime(q.createdAt),
      startedAt = toOffsetDateTime(q.startedAt),
      finishedAt = toOffsetDateTime(q.finishedAt),
      files = q.resultFiles.map(f => ValpasMassaluovutusServletUrls.file(rootUrl, q.queryId, f)),
      hint = None,
      error = if (session.hasGlobalReadAccess) Some(q.error) else None,
    )
    case q: CompleteQuery => CompleteQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = toOffsetDateTime(q.createdAt),
      startedAt = toOffsetDateTime(q.startedAt),
      finishedAt = toOffsetDateTime(q.finishedAt),
      files = q.resultFiles.map(f => ValpasMassaluovutusServletUrls.file(rootUrl, q.queryId, f)),
      password = q.meta.flatMap(_.password),
      sourceDataUpdatedAt = q.meta.flatMap(_.raportointikantaGeneratedAt).map(toOffsetDateTime),
    )
  }
}
