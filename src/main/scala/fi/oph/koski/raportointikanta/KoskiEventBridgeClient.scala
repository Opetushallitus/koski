package fi.oph.koski.raportointikanta

import fi.oph.common.log.Logging
import fi.oph.koski.config.Environment
import fi.oph.koski.json.LegacyJsonSerialization
import org.json4s.jackson.JsonMethods
import software.amazon.awssdk.services.eventbridge.model.{PutEventsRequest, PutEventsRequestEntry}
import software.amazon.awssdk.services.eventbridge.EventBridgeClient

import scala.collection.JavaConverters._

object KoskiEventBridgeClient extends Logging {
  private lazy val client = EventBridgeClient.create()

  def putEvents(events: EventBridgeEvent*): Unit = {
    if (Environment.isLocalDevelopmentEnvironment) {
      logger.info(s"Mocking event creation: ${events.map(_.toString).mkString(", ")}")
    } else {
      putEventsToAWS(events)
    }
  }

  private def putEventsToAWS(events: Seq[EventBridgeEvent]) = {
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
