package fi.oph.koski.tiedonsiirto

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.perustiedot.KoskiElasticSearchIndex
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.util.{ConcurrentStack, Timing}
import fi.oph.scalaschema.{SerializationContext, Serializer}
import org.json4s.{JBool, JObject, JValue}
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods

class TiedonsiirtoScheduler(db: DB, config: Config, index: KoskiElasticSearchIndex) extends Timing {
  val serializationContext = SerializationContext(KoskiSchema.schemaFactory, omitEmptyFields = false)
  val tiedonsiirtoStack = new ConcurrentStack[TiedonsiirtoDocument]
  val scheduler: Scheduler =
    new Scheduler(db, "tiedonsiirto-sync", new IntervalSchedule(config.getDuration("schedule.tiedonsiirtoSyncInterval")), None, syncTiedonsiirrot, runOnSingleNode = false, intervalMillis = 1000)

  def syncTiedonsiirrot(ctx: Option[JValue]): Option[JValue] = {
    val allTiedonsiirrot = tiedonsiirtoStack.popAll.reverse
    logger.debug(s"Updating ${allTiedonsiirrot.length} tiedonsiirrot documents to elasticsearch")
    if (allTiedonsiirrot.isEmpty) {
      return None
    }

    val tiedonsiirtoChunks = allTiedonsiirrot.grouped(1000).toList
    tiedonsiirtoChunks.zipWithIndex.map { case (tiedonsiirrot, i) =>
      index.updateBulk(tiedonsiirrot.flatMap { tiedonsiirto =>
        List(
          JObject("update" -> JObject("_id" -> JString(tiedonsiirto.id), "_index" -> JString("koski"), "_type" -> JString("tiedonsiirto"))),
          JObject("doc_as_upsert" -> JBool(true), "doc" -> Serializer.serialize(tiedonsiirto, serializationContext))
        )
      }, refresh = i == tiedonsiirtoChunks.length - 1) // wait for elasticsearch to refresh after the last batch, makes testing easier
    }.collect { case (errors, response) if errors => JsonMethods.pretty(response) }
     .foreach(resp => logger.error(s"Elasticsearch indexing failed: $resp"))

    None
  }
}
