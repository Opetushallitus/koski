package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.opiskeluoikeus.Henkilötiedot
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.util.Timing
import org.json4s.JValue

class KoskiScheduledTasks(application: KoskiApplication) extends Timing {
  val updateHenkilötScheduler: Scheduler = new UpdateHenkilot(application).scheduler
}

class UpdateHenkilot(application: KoskiApplication) extends Timing {
  def scheduler = new Scheduler(application.database.db, "henkilötiedot-update", new IntervalSchedule(henkilötiedotUpdateInterval), henkilöUpdateContext(changedHenkilösSince), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    implicit val formats = Json.jsonFormats
    val start = changedHenkilösSince
    val oldContext = context.get.extract[HenkilöUpdateContext]

    try {
      val (henkilöTauluCount, elasticCount) = runUpdate(oldContext)
      logger.info(s"Updated $henkilöTauluCount entries to henkilö table and $elasticCount to elasticsearch")
      henkilöUpdateContext(start)
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running scheduledHenkilötiedotUpdate")
        Some(Json.toJValue(oldContext))
    }
  }

  private def runUpdate(oldContext: HenkilöUpdateContext) = {
    val kaikkiMuuttuneet: List[Oid] = application.authenticationServiceClient.findChangedOppijaOids(oldContext.lastRun)
    val (henkilöTauluCounts, elasticCounts) = kaikkiMuuttuneet.sliding(1000, 1000).map { oids: List[Oid] =>
      val muuttuneet: Map[Oid, OppijaHenkilö] = application.authenticationServiceClient.findOppijatByOids(oids).groupBy(_.oidHenkilo).mapValues(_.head)
      val muuttuneidenOidit: List[Oid] = muuttuneet.values.map(_.toNimitiedotJaOid).filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o) > 0).map(_.oid).toList
      val muuttuneidenHenkilötiedot: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(muuttuneidenOidit).map(p => Henkilötiedot(p.id, muuttuneet(p.henkilö.oid).toNimitiedotJaOid))
      val updatedToElastic = application.perustiedotRepository.updateBulk(muuttuneidenHenkilötiedot, insertMissing = false) match {
        case Right(updatedCount) => updatedCount
        case Left(HttpStatus(_, errors)) => throw new Exception(s"Couldn't update data to elasticsearch ${errors.mkString}")
      }
      (muuttuneidenOidit.length, updatedToElastic)
    }.toList.unzip
    (henkilöTauluCounts.sum, elasticCounts.sum)
  }

  private def changedHenkilösSince = currentTimeMillis - 10 * 1000 // 10 seconds before now
  private def henkilöUpdateContext(lastRun: Long) = Some(Json.toJValue(HenkilöUpdateContext(lastRun)))
  private def henkilötiedotUpdateInterval = application.config.getDuration("schedule.henkilötiedotUpdateInterval")
}

case class HenkilöUpdateContext(lastRun: Long)
