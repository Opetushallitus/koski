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
    val tiedonsiirrot = tiedonsiirtoStack.popAll.reverse
    logger.debug(s"Updating ${tiedonsiirrot.length} tiedonsiirrot documents to elasticsearch")
    if (tiedonsiirrot.isEmpty) {
      return None
    }

    val (errors, response) = index.updateBulk(tiedonsiirrot.flatMap { tiedonsiirto =>
      List(
        JObject("update" -> JObject("_id" -> JString(tiedonsiirto.id), "_index" -> JString("koski"), "_type" -> JString("tiedonsiirto"))),
        JObject("doc_as_upsert" -> JBool(true), "doc" -> Serializer.serialize(tiedonsiirto, serializationContext))
      )
    }, refresh = true)

    if (errors) {
      val msg = s"Elasticsearch indexing failed: ${JsonMethods.pretty(response)}"
      logger.error(msg)
    }
    None
  }
}
