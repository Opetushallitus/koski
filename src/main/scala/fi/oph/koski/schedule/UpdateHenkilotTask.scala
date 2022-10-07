package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{OppijaHenkilö, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.OpiskeluoikeudenHenkilötiedot
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.util.Timing
import org.json4s.JValue

class UpdateHenkilotTask(application: KoskiApplication) extends Timing {
  // Start by scanning 10 minutes to the past to take possible CPU time difference into account.
  // After first call that actually yields some changes from the data source, we'll use the timestamp
  // of latest change (+1 millisecond) as the limit.
  private val backBufferMs = 10 * 60 * 1000
  def scheduler: Option[Scheduler] =
    if (application.config.getString("schedule.henkilötiedotUpdateInterval") == "never") {
      None
    } else {
      Some(new Scheduler(
        application.masterDatabase.db,
        "henkilötiedot-update",
        new IntervalSchedule(application.config.getDuration("schedule.henkilötiedotUpdateInterval")),
        henkilöUpdateContext(currentTimeMillis - backBufferMs),
        updateHenkilöt(refresh = false)
      ))
    }

  def updateHenkilöt(refresh: Boolean)(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    try {
      val oldContext = JsonSerializer.extract[HenkilöUpdateContext](context.get)
      val (unfilteredChangedOids, changedKoskiOids) = findChangedOppijaOids(oldContext.lastRun)
      val newContext = runUpdate(unfilteredChangedOids, changedKoskiOids, oldContext, refresh)
      Some(JsonSerializer.serializeWithRoot(newContext))
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running scheduledHenkilötiedotUpdate")
        context
    }
  }

  private def findChangedOppijaOids(since: Long): (List[Oid], List[String]) = {
    val batchSize = 5000
    var offset = 0
    def changedOids = {
      val oids = application.opintopolkuHenkilöFacade.findChangedOppijaOids(since, offset, batchSize)
      offset = offset + batchSize
      (oids, application.henkilöCache.filterOidsByCache(oids).toList)
    }

    Iterator.continually(changedOids)
      // query until oids found from koski or got less oids than requested
      .span { case (oids, koskiOids) => koskiOids.isEmpty && oids.size == batchSize }._2
      .next
  }


  private def runUpdate(oids: List[Oid], koskiOids: List[Oid], lastContext: HenkilöUpdateContext, refresh: Boolean) = {
    val oppijat: Seq[OppijaHenkilö] = findOppijatWithoutSlaveOids(koskiOids)

    val oppijatWithMaster: Seq[WithModifiedTime] = oppijat.map { oppija =>
      WithModifiedTime(application.henkilöRepository.opintopolku.withMasterInfo(oppija), oppija.modified)
    }

    val oppijatByOid: Map[Oid, WithModifiedTime] = oppijatWithMaster.groupBy(_.tiedot.henkilö.oid).mapValues(_.head)

    val lastModified = oppijat.lastOption.map(_.modified + 1)
      .orElse(oids.lastOption.flatMap(o => findOppijatWithoutSlaveOids(List(o)).headOption).map(_.modified))
      .getOrElse(lastContext.lastRun)

    if (oids.nonEmpty) {
      logger.info(s"Changed count: ${oids.size}, filtered count ${oppijat.size}, lastModified: $lastModified")
    }

    val updatedInKoskiHenkilöCache: Seq[Oid] = oppijatWithMaster
      .filter(o => application.henkilöCache.updateHenkilö(o.tiedot) > 0)
      .map(_.tiedot.henkilö.oid)

    if (updatedInKoskiHenkilöCache.isEmpty) {
      HenkilöUpdateContext(lastModified)
    } else {
      val muuttuneidenHenkilötiedot: Seq[OpiskeluoikeudenHenkilötiedot] = application.perustiedotRepository
        .findHenkiloPerustiedotByOids(updatedInKoskiHenkilöCache)
        .map(p => {
          val päivitetytTiedot: WithModifiedTime = oppijatByOid(p.henkilöOid.getOrElse(p.henkilö.get.oid))
          OpiskeluoikeudenHenkilötiedot(p.id, päivitetytTiedot.tiedot)
        })

      application.perustiedotIndexer.updatePerustiedot(muuttuneidenHenkilötiedot, upsert = false, refresh) match {
        case Right(updatedCount) => {
          logger.info(s"Updated ${updatedInKoskiHenkilöCache.length} entries to henkilö table and $updatedCount to OpenSearch, latest oppija modified timestamp: $lastModified")
          HenkilöUpdateContext(lastModified)
        }
        case Left(HttpStatus(_, errors)) => {
          logger.error(s"Couldn't update data to OpenSearch ${errors.mkString}")
          HenkilöUpdateContext(oppijatByOid(updatedInKoskiHenkilöCache.head).modified - 1000)
        }
      }
    }
  }

  private def findOppijatWithoutSlaveOids(filteredOids: List[String]) =
    application.opintopolkuHenkilöFacade.findOppijatNoSlaveOids(filteredOids).sortBy(_.modified)

  private def henkilöUpdateContext(lastRun: Long) = Some(JsonSerializer.serializeWithRoot(HenkilöUpdateContext(lastRun)))
}

private case class WithModifiedTime(tiedot: OppijaHenkilöWithMasterInfo, modified: Long)
private case class HenkilöUpdateContext(lastRun: Long)
