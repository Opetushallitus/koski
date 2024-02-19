package fi.oph.koski.config

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fi.oph.koski.executors.Pools
import org.http4s.blaze.client.BlazeClientBuilder
import org.json4s.JString
import org.json4s.jackson.JsonMethods

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class ECSMetadataClient {

  lazy val availabilityZone: Option[String] = taskMetadata.map(_ \\ "AvailabilityZone") match {
    case Some(JString(az)) => Some(az)
    case _ => None
  }

  private lazy val taskMetadata =
    taskMetadataUri.map(
      httpClient
        .get(_)(_.bodyText.compile.string)
        .map(body => JsonMethods.parse(body))
        .unsafeRunSync()
    )

  private val containerMetadataUri = sys.env.get("ECS_CONTAINER_METADATA_URI_V4")
  private val taskMetadataUri = containerMetadataUri.map(uri => s"$uri/task")
  private lazy val httpClient =
    BlazeClientBuilder[IO].withExecutionContext(ExecutionContext.fromExecutor(Pools.httpPool))
      .withMaxTotalConnections(1)
      .withMaxWaitQueueLimit(1024)
      .withConnectTimeout(10.seconds)
      .withResponseHeaderTimeout(10.seconds)
      .withRequestTimeout(15.seconds)
      .withIdleTimeout(20.seconds)
      .resource
      .allocated
      .map(_._1)
      .unsafeRunSync()
}

case class ECSTaskMetadata(
  AvailabilityZone: String,
)
