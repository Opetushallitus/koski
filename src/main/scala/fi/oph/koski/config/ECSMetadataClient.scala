package fi.oph.koski.config

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fi.oph.koski.executors.Pools
import org.http4s.blaze.client.BlazeClientBuilder
import org.json4s.JString
import org.json4s.jackson.JsonMethods
import software.amazon.awssdk.services.ecs.EcsClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class ECSMetadataClient {

  lazy val availabilityZone: Option[String] = getString("AvailabilityZone")
  lazy val taskARN: Option[String] = getString("TaskARN")

  // https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-metadata-endpoint-v4-fargate-response.html
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

  private def getString(key: String): Option[String] =
    taskMetadata.map(_ \\ key) match {
      case Some(JString(az)) => Some(az)
      case _ => None
    }
}
