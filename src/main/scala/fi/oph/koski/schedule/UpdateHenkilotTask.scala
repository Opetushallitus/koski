package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.oppijanumerorekisteriservice.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
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
      val changedOids = application.opintopolkuHenkilöFacade.findChangedOppijaOids(oldContext.lastRun)
      val newContext = HenkiloUpdate(application, changedOids, oldContext).runUpdate
      Some(JsonSerializer.serializeWithRoot(newContext))
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running scheduledHenkilötiedotUpdate")
        context
    }
  }

  private def henkilöUpdateContext(lastRun: Long) = Some(JsonSerializer.serializeWithRoot(HenkilöUpdateContext(lastRun)))
  private def henkilötiedotUpdateInterval = application.config.getDuration("schedule.henkilötiedotUpdateInterval")
}

case class HenkiloUpdate(application: KoskiApplication, oids: List[Oid], lastContext: HenkilöUpdateContext) extends Logging {
  def runUpdate: HenkilöUpdateContext = {
    val oppijatWithMaster: List[WithModifiedTime] = oppijatToUpdate.map { oppija =>
      WithModifiedTime(application.henkilöRepository.opintopolku.withMasterInfo(oppija.toTäydellisetHenkilötiedot), oppija.modified)
    }

    val oppijatByOid: Map[Oid, WithModifiedTime] = oppijatWithMaster.groupBy(_.tiedot.henkilö.oid).mapValues(_.head)

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

  private lazy val oppijatToUpdate: List[OppijaHenkilö] = findOppijat(koskiOids)
  private lazy val koskiOids: List[Oid] = application.henkilöCache.filterOidsByCache(oids).toList

  private def findOppijat(oidsToFind: List[Oid]) = application.opintopolkuHenkilöFacade.findOppijatByOids(oidsToFind).sortBy(_.modified)

  private def lastModified = if (oids.isEmpty) {
    lastContext.lastRun
  } else if (oppijatToUpdate.isEmpty) {
    System.currentTimeMillis - 300000 // 5 min ago
  } else {
    oppijatToUpdate.last.modified + 1
  }
}

case class WithModifiedTime(tiedot: TäydellisetHenkilötiedotWithMasterInfo, modified: Long)
case class HenkilöUpdateContext(lastRun: Long)
