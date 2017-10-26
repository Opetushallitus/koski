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

class TiedonsiirtoScheduler(db: DB, config: Config, index: KoskiElasticSearchIndex, tiedonsiirtoService: TiedonsiirtoService) extends Timing {
  val scheduler: Scheduler =
    new Scheduler(db, "tiedonsiirto-sync", new IntervalSchedule(config.getDuration("schedule.tiedonsiirtoSyncInterval")), None, syncTiedonsiirrot, runOnSingleNode = false, intervalMillis = 1000)

  def syncTiedonsiirrot(ctx: Option[JValue]): Option[JValue] = {
    tiedonsiirtoService.syncToElasticsearch()
    None
  }
}
