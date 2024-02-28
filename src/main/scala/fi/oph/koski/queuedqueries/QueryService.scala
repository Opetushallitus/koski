package fi.oph.koski.queuedqueries

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging

import java.net.InetAddress
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class QueryService(application: KoskiApplication) extends Logging {
  val workerId: String = InetAddress.getLocalHost.getHostName
  logger.info(s"Query worker id: $workerId")

  private val queries = new QueryRepository(
    db = application.masterDatabase.db,
    workerId = workerId,
    extractor = application.validatingAndResolvingExtractor,
  )
  private val results = new QueryResultsRepository(application.config)

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
      .filter(_.userOid == user.oid)
      .toRight(KoskiErrorCategory.notFound())

  def numberOfRunningQueries: Int = queries.numberOfRunningQueries

  def runNext(): Unit = {
    queries.takeNext.foreach { query =>
      query.getSession(application.käyttöoikeusRepository).fold {
        logger.error(s"Could not start query ${query.queryId} due to invalid session")
      } { session =>
        logger.info(s"Starting new ${query.name} as user ${query.userOid}")
        implicit val user: KoskiSpecificSession = session
        val writer = QueryResultWriter(UUID.fromString(query.queryId), results)
        try {
          query.query.run(application, writer).fold(
            { error =>
              logger.error(s"${query.name} failed: ${error}")
              queries.setFailed(query.queryId, error)
            },
            { _ =>
              logger.info(s"${query.name} completed with ${writer.files.size} result files")
              queries.setComplete(query.queryId, writer.files.toList)
            }
          )
        } catch {
          case t: Throwable =>
            logger.error(t)(s"${query.name} failed ungracefully")
            queries.setFailed(query.queryId, t.getMessage)
        }
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

  def cleanup(): Unit = {
    val timeout = application.config.getDuration("kyselyt.timeout")
    queries
      .setLongRunningQueriesFailed(timeout, "Timeout")
      .foreach(query => logger.error(s"${query.name} timeouted after $timeout"))
  }

  def cancelAllTasks(reason: String) = queries.setRunningTasksFailed(reason)
}
