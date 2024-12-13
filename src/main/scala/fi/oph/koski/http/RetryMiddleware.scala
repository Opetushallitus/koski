package fi.oph.koski.http

import cats.effect.Temporal
import org.http4s.client.Client
import org.http4s.client.middleware.{Retry, RetryPolicy}
import org.http4s.{Request, Response}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.higherKinds

object RetryMiddleware {
  type RetriableFilter[F[_]] = (Request[F], Either[Throwable, Response[F]]) => Boolean

  def retryNonIdempotentRequests[F[_]]: RetriableFilter[F] = (_req, result) => RetryPolicy.recklesslyRetriable(result)

  val DefaultBackoffPolicy: Int => Option[FiniteDuration] = RetryPolicy.exponentialBackoff(
    maxWait = 2.seconds,
    maxRetry = 5
  )

  private def failInfo[F[_]](req: Request[F], result: Either[Throwable, Response[F]]): String = {
    val reason = result match {
      case Left(error) => s"threw ${error.toString}"
      case Right(response) => s"status ${response.status.code}"
    }
    s"Request ${req.method} ${req.uri} failed ($reason)"
  }

  private def loggingRetryPolicy[F[_]](
    backoff: Int => Option[FiniteDuration],
    retriable: (Request[F], Either[Throwable, Response[F]]) => Boolean
  ): RetryPolicy[F] = { (req, result, retries) =>
    if (retriable(req, result)) {
      backoff(retries) match {
        case Some(backoff) =>
          HttpResponseLog.logger.info(s"${failInfo(req, result)}: retrying (retry #$retries, waiting $backoff)")
          Some(backoff)
        case None =>
          HttpResponseLog.logger.error(s"${failInfo(req, result)}: giving up after ${retries-1} retries")
          None
      }
    } else {
      if (!req.method.isIdempotent && RetryPolicy.isErrorOrRetriableStatus(result)) {
        HttpResponseLog.logger.error(s"${failInfo(req, result)} but will not retry nonidempotent request")
      }
      None
    }
  }

  def withLoggedRetry[F[_]](client: Client[F])(implicit F: Temporal[F]): Client[F] = {
    withLoggedRetry(client, RetryPolicy.defaultRetriable)
  }

  def withLoggedRetry[F[_]](
    client: Client[F],
    retriableFilter: RetriableFilter[F],
    backoffPolicy: Int => Option[FiniteDuration] = DefaultBackoffPolicy,
  )(implicit F: Temporal[F]): Client[F] = {
    Retry[F](loggingRetryPolicy(backoffPolicy, retriableFilter))(client)
  }
}
