package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.perustiedot.Henkilötiedot
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.util.Timing
import org.json4s.JValue

class KoskiScheduledTasks(application: KoskiApplication) {
  if (!application.database.config.isReadonly) {
    new UpdateHenkilot(application).scheduler
  }
}

class UpdateHenkilot(application: KoskiApplication) extends Timing {
  def scheduler = new Scheduler(application.database.db, "henkilötiedot-update", new IntervalSchedule(henkilötiedotUpdateInterval), henkilöUpdateContext(currentTimeMillis), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    implicit val formats = Json.jsonFormats
    try {
      val oldContext = context.get.extract[HenkilöUpdateContext]
      val startMillis = currentTimeMillis
      val changedOids = application.authenticationServiceClient.findChangedOppijaOids(oldContext.lastRun)
      val newContext = runUpdate(changedOids, startMillis, oldContext)
      Some(Json.toJValue(newContext))
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running scheduledHenkilötiedotUpdate")
        context
    }
  }

  private def runUpdate(oids: List[Oid], startMillis: Long, lastContext: HenkilöUpdateContext) = {
    val oppijat: List[OppijaHenkilö] = application.authenticationServiceClient.findOppijatByOids(oids).sortBy(_.modified)
    val oppijatByOid: Map[Oid, OppijaHenkilö] = oppijat.groupBy(_.oidHenkilo).mapValues(_.head)
    val foundFromKoski: List[Oid] = oppijat.map(_.toTäydellisetHenkilötiedot).filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o) > 0).map(_.oid)
    val lastModified = oppijat.lastOption.map(o => o.modified + 1).getOrElse(startMillis)

    if (foundFromKoski.isEmpty) {
      HenkilöUpdateContext(lastModified)
    } else {
      val muuttuneidenHenkilötiedot: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(foundFromKoski).map(p => Henkilötiedot(p.id, oppijatByOid(p.henkilö.oid).toNimitiedotJaOid))
      application.perustiedotIndexer.updateBulk(muuttuneidenHenkilötiedot, insertMissing = false) match {
        case Right(updatedCount) => updatedCount
          logger.info(s"Updated ${foundFromKoski.length} entries to henkilö table and $updatedCount to elasticsearch, latest oppija modified timestamp: $lastModified")
          HenkilöUpdateContext(lastModified)
        case Left(HttpStatus(_, errors)) =>
          logger.error(s"Couldn't update data to elasticsearch ${errors.mkString}")
          HenkilöUpdateContext(oppijatByOid(foundFromKoski.head).modified - 1000)
      }
    }
  }

  private def henkilöUpdateContext(lastRun: Long) = Some(Json.toJValue(HenkilöUpdateContext(lastRun)))
  private def henkilötiedotUpdateInterval = application.config.getDuration("schedule.henkilötiedotUpdateInterval")
}

case class HenkilöUpdateContext(lastRun: Long)
