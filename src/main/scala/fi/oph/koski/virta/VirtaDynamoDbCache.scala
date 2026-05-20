package fi.oph.koski.virta

import fi.oph.koski.log.Logging
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, PutItemRequest}

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal
import scala.xml.{Elem, XML}

class VirtaDynamoDbCache(client: DynamoDbClient, tableName: String) extends Logging {
  private val helsinkiZone = ZoneId.of("Europe/Helsinki")

  def get(hakuehdot: List[VirtaHakuehto]): Option[Elem] = {
    try {
      val key = serializeKey(hakuehdot)
      val request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Map("cacheKey" -> str(key)).asJava)
        .build()
      val item = client.getItem(request).item()
      if (item.isEmpty) return None
      val storedGeneration = item.get("generation").s()
      if (storedGeneration != currentGeneration()) return None
      Some(XML.loadString(item.get("data").s()))
    } catch {
      case NonFatal(e) =>
        logger.warn(s"VirtaDynamoDbCache.get epäonnistui: ${e.getMessage}")
        None
    }
  }

  def put(hakuehdot: List[VirtaHakuehto], data: Elem): Unit = {
    try {
      val ttlEpoch = Instant.now().getEpochSecond + 48 * 3600
      val item = Map(
        "cacheKey"   -> str(serializeKey(hakuehdot)),
        "data"       -> str(data.toString()),
        "generation" -> str(currentGeneration()),
        "ttl"        -> num(ttlEpoch),
      ).asJava
      client.putItem(PutItemRequest.builder().tableName(tableName).item(item).build())
    } catch {
      case NonFatal(e) =>
        logger.warn(s"VirtaDynamoDbCache.put epäonnistui: ${e.getMessage}")
    }
  }

  private[virta] def currentGeneration(): String = {
    val now = ZonedDateTime.now(helsinkiZone)
    val todayAt6 = now.toLocalDate.atTime(6, 0).atZone(helsinkiZone)
    val date = if (now.isBefore(todayAt6)) now.toLocalDate.minusDays(1) else now.toLocalDate
    date.toString
  }

  private def serializeKey(hakuehdot: List[VirtaHakuehto]): String =
    hakuehdot.map {
      case VirtaHakuehtoHetu(h)                      => s"hetu:$h"
      case VirtaHakuehtoKansallinenOppijanumero(oid)  => s"oid:$oid"
    }.sorted.mkString(",")

  private def str(s: String) = AttributeValue.builder().s(s).build()
  private def num(n: Long)   = AttributeValue.builder().n(n.toString).build()
}
