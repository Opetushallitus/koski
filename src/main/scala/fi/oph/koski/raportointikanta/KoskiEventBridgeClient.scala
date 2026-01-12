package fi.oph.koski.raportointikanta

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.log.Logging
import org.json4s.jackson.JsonMethods
import software.amazon.awssdk.services.eventbridge.model.{PutEventsRequest, PutEventsRequestEntry}
import software.amazon.awssdk.services.eventbridge.EventBridgeClient

import scala.jdk.CollectionConverters._

object KoskiEventBridgeClient extends Logging {
  def apply(config: Config): KoskiEventBridgeClient = {
    if (Environment.isServerEnvironment(config)) {
      new AwsEventBridgeClient
    } else {
      new MockEventBridgeClient
    }
  }
}

trait KoskiEventBridgeClient {
  def putEvents(events: EventBridgeEvent*): Unit
}

class MockEventBridgeClient extends KoskiEventBridgeClient with Logging {
  override def putEvents(events: EventBridgeEvent*): Unit = {
    logger.info(s"Mocking event creation: ${events.map(_.toString).mkString(", ")}")
  }
}

class AwsEventBridgeClient extends KoskiEventBridgeClient {
  private val client = EventBridgeClient.create()

  override def putEvents(events: EventBridgeEvent*): Unit = {
    val entries = events.map(e =>
      PutEventsRequestEntry.builder()
        .source("fi.oph.koski")
        .detailType(e.eventType)
        .detail(JsonMethods.compact(LegacyJsonSerialization.toJValue(e.payload)))
        .build()
    ).asJava

    client.putEvents(PutEventsRequest.builder().entries(entries).build())
  }
}

case class EventBridgeEvent(
  eventType: String,
  payload: Map[String, String]
)
