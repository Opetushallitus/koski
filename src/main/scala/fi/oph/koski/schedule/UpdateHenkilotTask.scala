package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.oppijanumerorekisteriservice.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.OpiskeluoikeudenHenkilötiedot
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedotWithMasterInfo
import fi.oph.koski.util.Timing
import org.json4s.JValue

class UpdateHenkilotTask(application: KoskiApplication) extends Timing {
  // Start by scanning 10 minutes to the past to take possible CPU time difference into account.
  // After first call that actually yields some changes from the data source, we'll use the timestamp
  // of latest change (+1 millisecond) as the limit.
  private val backBufferMs = 10 * 60 * 1000
  def scheduler = new Scheduler(application.masterDatabase.db, "henkilötiedot-update", new IntervalSchedule(henkilötiedotUpdateInterval), henkilöUpdateContext(currentTimeMillis - backBufferMs), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    try {
      val oldContext = JsonSerializer.extract[HenkilöUpdateContext](context.get)
      val (unfilteredChangedOids, changedKoskiOids) = findChangedOppijaOids(oldContext.lastRun)
      val newContext = runUpdate(unfilteredChangedOids, changedKoskiOids, oldContext)
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


  private def runUpdate(oids: List[Oid], koskiOids: List[Oid], lastContext: HenkilöUpdateContext) = {
    val oppijat: List[OppijaHenkilö] = findOppijat(koskiOids)

    val oppijatWithMaster: List[WithModifiedTime] = oppijat.map { oppija =>
      WithModifiedTime(application.henkilöRepository.opintopolku.withMasterInfo(oppija.toTäydellisetHenkilötiedot), oppija.modified)
    }

    val oppijatByOid: Map[Oid, WithModifiedTime] = oppijatWithMaster.groupBy(_.tiedot.henkilö.oid).mapValues(_.head)

    val lastModified = oppijat.lastOption.map(_.modified + 1)
      .orElse(oids.lastOption.flatMap(o => findOppijat(List(o)).headOption).map(_.modified))
      .getOrElse(lastContext.lastRun)

    if (oids.nonEmpty) {
      logger.info(s"Changed count: ${oids.size}, filtered count ${oppijat.size}, lastModified: $lastModified")
    }

    val updatedInKoskiHenkilöCache: List[Oid] = oppijatWithMaster
      .filter(o => application.henkilöCache.updateHenkilöAction(o.tiedot) > 0)
      .map(_.tiedot.henkilö.oid)

    if (updatedInKoskiHenkilöCache.isEmpty) {
      HenkilöUpdateContext(lastModified)
    } else {
      val muuttuneidenHenkilötiedot: List[OpiskeluoikeudenHenkilötiedot] = application.perustiedotRepository
        .findHenkiloPerustiedotByOids(updatedInKoskiHenkilöCache)
        .map(p => {
          val päivitetytTiedot = oppijatByOid(p.henkilöOid.getOrElse(p.henkilö.get.oid))
          OpiskeluoikeudenHenkilötiedot(p.id, päivitetytTiedot.tiedot)
        })

      application.perustiedotIndexer.updateBulk(muuttuneidenHenkilötiedot, false) match {
        case Right(updatedCount) => {
          logger.info(s"Updated ${updatedInKoskiHenkilöCache.length} entries to henkilö table and $updatedCount to elasticsearch, latest oppija modified timestamp: $lastModified")
          HenkilöUpdateContext(lastModified)
        }
        case Left(HttpStatus(_, errors)) => {
          logger.error(s"Couldn't update data to elasticsearch ${errors.mkString}")
          HenkilöUpdateContext(oppijatByOid(updatedInKoskiHenkilöCache.head).modified - 1000)
        }
      }
    }
  }

  private def findOppijat(filteredOids: List[String]) =
    application.opintopolkuHenkilöFacade.findOppijatByOids(filteredOids).sortBy(_.modified)

  private def henkilöUpdateContext(lastRun: Long) = Some(JsonSerializer.serializeWithRoot(HenkilöUpdateContext(lastRun)))
  private def henkilötiedotUpdateInterval = application.config.getDuration("schedule.henkilötiedotUpdateInterval")
}

case class WithModifiedTime(tiedot: TäydellisetHenkilötiedotWithMasterInfo, modified: Long)
case class HenkilöUpdateContext(lastRun: Long)
