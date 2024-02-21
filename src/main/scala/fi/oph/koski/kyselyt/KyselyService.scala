package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.kyselyt.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudet
import fi.oph.koski.log.Logging

import java.net.InetAddress
import java.util.UUID

class KyselyService(application: KoskiApplication) extends Logging {
  val workerId: String = InetAddress.getLocalHost.getHostName
  logger.info(s"Query worker id: $workerId")

  private val queries = new KyselyRepository(
    db = application.masterDatabase.db,
    workerId = workerId,
    extractor = application.validatingAndResolvingExtractor,
  )
  private val results = new KyselyTulosRepository(application.config)

  def add(query: QueryParameters)(implicit user: KoskiSpecificSession): Either[HttpStatus, Query] = {
    query.withDefaults.flatMap { query =>
      queries.getExisting(query).fold {
        if (query.queryAllowed(application)) {
          Right[HttpStatus, Query](queries.add(query))
        } else {
          Left(KoskiErrorCategory.unauthorized())
        }
      }(Right.apply)
    }
  }

  def get(id: UUID)(implicit user: KoskiSpecificSession): Either[HttpStatus, Query] =
    queries.get(id)
      .filter(_.requestedBy == user.oid)
      .toRight(KoskiErrorCategory.notFound())

  def numberOfRunningQueries: Int = queries.numberOfRunningQueries

  def runNext(): Unit = {
    queries.takeNext.foreach { query =>
      logger.info(s"Starting new query: ${query.queryId} ${query.query.getClass.getName}")
      query.query.run(application) match {
        case Right(streams) =>
          streams.par.map(result => results.putStream(
            queryId = UUID.fromString(query.queryId),
            name = result.name,
            inputStream = new StringInputStream(result.stream),
            contentLength = result.length,
          ))
          queries.setComplete(query.queryId, streams.map(_.name))
        case Left(error) =>
          queries.setFailed(query.queryId, error)
      }
    }
  }

  def getDownloadUrl(query: CompleteQuery, name: String): Option[String] = {
    val id = UUID.fromString(query.queryId)
    try {
      Some(results.getPresignedDownloadUrl(id, name))
    } catch {
      case t: Throwable => None
    }
  }

  private def putResults(id: String, queryResults: List[QueryResult]) = {
    try {
      val files = queryResults.par.map { result =>
        results.putStream(
          queryId = UUID.fromString(id),
          name = result.name,
          inputStream = new StringInputStream(result.content),
          contentLength = result.content.length,
        )
        result.name
      }
      queries.setComplete(id, files.toList)
    } catch {
      case t: Throwable =>
        logger.error(t)(s"Query failed: ${t.getMessage}")
        queries.setFailed(id, t.getMessage)
    }
  }

  def cancelAllTasks(reason: String) = queries.setRunningTasksFailed(reason)
}
