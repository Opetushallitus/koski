package fi.oph.koski.queuedqueries

import fi.oph.koski.cache.GlobalCacheManager._
import fi.oph.koski.cache.{RefreshingCache, SingleValueCache}
import fi.oph.koski.cloudwatch.CloudWatchMetricsService
import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.util.{Timeout, TryWithLogging}

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{DurationInt, FiniteDuration}


class QueryService(application: KoskiApplication) extends Logging {
  val workerId: String = application.ecsMetadata.taskARN.getOrElse("local")
  val metrics: CloudWatchMetricsService = CloudWatchMetricsService(application.config)
  private val maxAllowedDatabaseReplayLag: Duration = application.config.getDuration("kyselyt.backpressureLimits.maxDatabaseReplayLag")
  private val readDatabaseId = QueryUtils.readDatabaseId(application.config)
  private val databaseLoadLimiter = new DatabaseLoadLimiter(application, metrics, readDatabaseId)
  private val queryMaxRunningTime = FiniteDuration(application.config.getDuration("kyselyt.timeout").toSeconds, TimeUnit.SECONDS)

  private val queries = new QueryRepository(
    db = application.masterDatabase.db,
    workerId = workerId,
    extractor = application.validatingAndResolvingExtractor,
  )
  private val results = new QueryResultsRepository(application.config)

  def add(query: QueryParameters)(implicit user: KoskiSpecificSession): Either[HttpStatus, Query] = {
    query.fillAndValidate.flatMap { query =>
      queries.getExisting(query).fold {
        if (query.queryAllowed(application)) {
          Right[HttpStatus, Query](queries.add(query))
        } else {
          Left(KoskiErrorCategory.forbidden())
        }
      }(Right.apply)
    }
  }

  def addRaw(query: Query): Query = queries.addRaw(query)

  def get(id: UUID)(implicit user: KoskiSpecificSession): Either[HttpStatus, Query] =
    queries.get(id)
      .filter(_.userOid == user.oid)
      .toRight(KoskiErrorCategory.notFound())

  def numberOfRunningQueries: Int = queries.numberOfRunningQueries

  def hasNext: Boolean = queries.numberOfPendingQueries > 0

  def runNext(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    queries.takeNext.foreach { query =>
      query.getSession(application.käyttöoikeusRepository).fold {
        logger.error(s"Could not start query ${query.queryId} due to invalid session")
      } { session =>
        logStart(query)
        implicit val user: KoskiSpecificSession = session
        val writer = QueryResultWriter(UUID.fromString(query.queryId), queries, results)
        try {
          Timeout(queryMaxRunningTime) {
            query.query.run(application, writer)
          }.fold(
            { error =>
              logFailedQuery(query, error)
              queries.setFailed(query.queryId, error)
            },
            { _ =>
              logCompletedQuery(query, writer.objectKeys.size)
              queries.setComplete(query.queryId, writer.objectKeys.toList)
            }
          )
        } catch {
          case t: Throwable =>
            logFailedQuery(query, t.getMessage, Some(t))
            queries.setFailed(query.queryId, t.getMessage)
        }
      }
    }
  }

  def getDownloadUrl(query: CompleteQuery, name: String): Either[HttpStatus, String] =
    TryWithLogging(logger, {
      results.getPresignedDownloadUrl(UUID.fromString(query.queryId), name)
    }).left.map(t => KoskiErrorCategory.badRequest(s"Tiedostoa ei löydy tai tapahtui virhe sen jakamisessa"))

  def cleanup(koskiInstances: Seq[KoskiInstance]): Unit = {
    val instanceArns = koskiInstances.map(_.taskArn)

    queries
      .findOrphanedQueries(instanceArns)
      .foreach { query =>
        if (query.restartCount >= 3) {
          queries.setFailed(query.queryId, "Orphaned")
          logger.warn(s"Orphaned query (${query.name}) detected and cancelled after ${query.restartCount} restarts")
        } else {
          if (queries.restart(query, s"Orphaned ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")) {
            logger.warn(s"Orphaned query (${query.name}) detected and it has been set back to pending state $query")
          }
        }
      }
  }

  def systemIsOverloaded: Boolean =
    (application.replicaDatabase.replayLag.toSeconds > maxAllowedDatabaseReplayLag.getSeconds) || databaseLoadLimiter.checkOverloading

  def cancelAllTasks(reason: String): Boolean = queries.setRunningTasksFailed(reason)

  private def logStart(query: RunningQuery): Unit = {
    logger.info(s"Starting new ${query.name} as user ${query.userOid}")
    metrics.putQueuedQueryMetric("started")
  }

  private def logFailedQuery(query: RunningQuery, reason: String, throwable: Option[Throwable] = None): Unit = {
    val message = s"${query.name} failed: ${reason}"
    throwable.fold(logger.error(message))(t => logger.error(t)(message))
    metrics.putQueuedQueryMetric(QueryState.failed)
  }

  private def logCompletedQuery(query: RunningQuery, fileCount: Int): Unit = {
    logger.info(s"${query.name} completed with $fileCount result files")
    metrics.putQueuedQueryMetric(QueryState.complete)
  }
}

class DatabaseLoadLimiter(
  application: KoskiApplication,
  metrics: CloudWatchMetricsService,
  readDatabaseId: String,
) {
  private val stopAt: Double = application.config.getDouble("kyselyt.backpressureLimits.ebsByteBalance.stopAt")
  private val continueAt: Double = application.config.getDouble("kyselyt.backpressureLimits.ebsByteBalance.continueAt")
  var limiterActive: Boolean = false

  def checkOverloading: Boolean = {
    synchronized {
      ebsByteBalance.apply.foreach { balance =>
        if (limiterActive) {
          if (balance >= continueAt) {
            limiterActive = false
          }
        } else {
          if (balance <= stopAt) {
            limiterActive = true
          }
        }
      }
      limiterActive
    }
  }

  private val ebsByteBalance = SingleValueCache(
    RefreshingCache(name = "DatabaseLoadLimiter.ebsByteBalance", duration = 1.minutes, maxSize = 2),
    () => metrics.getEbsByteBalance(readDatabaseId)
  )
}
