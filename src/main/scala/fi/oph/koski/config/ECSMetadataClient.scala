package fi.oph.koski.config

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.config.Config
import fi.oph.koski.cache.GlobalCacheManager._
import fi.oph.koski.cache.{RefreshingCache, SingleValueCache}
import fi.oph.koski.executors.Pools
import fi.oph.koski.log.Logging
import fi.oph.koski.util.TryWithLogging
import org.http4s.blaze.client.BlazeClientBuilder
import org.json4s.JString
import org.json4s.jackson.JsonMethods
import software.amazon.awssdk.services.ecs.EcsClient
import software.amazon.awssdk.services.ecs.model.{DescribeTasksRequest, ListTasksRequest, Task}

import java.time.Instant
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Try

class ECSMetadataClient(config: Config) {

  lazy val availabilityZone: Option[String] = getString("AvailabilityZone")
  lazy val taskARN: Option[String] = getString("TaskARN")

  def currentlyRunningKoskiInstances: Seq[KoskiInstance] = koskiInstances.apply

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

  private lazy val ecs = EcsService(config)

  private val koskiInstances = SingleValueCache[Seq[KoskiInstance]](
    RefreshingCache(name = "ECSMetadataClient.koskiInstances", duration = 5.seconds),
    () => ecs.describeKoskiTasks(ecs.getKoskiTaskArns)
  )
}

trait EcsService {
  def getKoskiTaskArns: Seq[String]
  def describeKoskiTasks(taskArns: Seq[String]): Seq[KoskiInstance]
}

object EcsService {
  def apply(config: Config): EcsService =
    if (Environment.isServerEnvironment(config)) {
      new RemoteEcsService(config)
    } else {
      new MockEcsService
    }
}

class MockEcsService extends EcsService {
  val createdAt: Instant = Instant.now()
  def getKoskiTaskArns: Seq[String] = Seq("local")
  def describeKoskiTasks(taskArns: Seq[String]): Seq[KoskiInstance] = taskArns.map(KoskiInstance(_, createdAt))
}

class RemoteEcsService(config: Config) extends EcsService with Logging {
  val client: EcsClient = EcsClient.create()
  val cluster: String = config.getString("kyselyt.ecs.cluster")
  val serviceName: String = config.getString("kyselyt.ecs.service")

  def getKoskiTaskArns: Seq[String] = TryWithLogging(logger, {
    val request = ListTasksRequest.builder()
      .cluster(cluster)
      .serviceName(serviceName)
      .build()
    client.listTasks(request).taskArns().asScala
  }).getOrElse(Seq.empty)

  def describeKoskiTasks(taskArns: Seq[String]): Seq[KoskiInstance] = TryWithLogging(logger, {
    val request = DescribeTasksRequest.builder()
      .cluster(cluster)
      .tasks(taskArns.asJava)
      .build()
    client.describeTasks(request)
      .tasks().asScala
      .map(KoskiInstance.apply)
  }).getOrElse(Seq.empty)
}

case class KoskiInstance(
  taskArn: String,
  createdAt: Instant
)

object KoskiInstance {
  def apply(task: Task): KoskiInstance = KoskiInstance(
    taskArn = task.taskArn(),
    createdAt = task.createdAt(),
  )
}
