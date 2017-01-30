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
  val updateHenkilötScheduler =
    new Scheduler(application.database.db, "henkilötiedot-update", new IntervalSchedule(henkilötiedotUpdateInterval), henkilöUpdateContext(changedHenkilösSince), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    implicit val formats = Json.jsonFormats
    val start = changedHenkilösSince
    val oldContext = context.get.extract[HenkilöUpdateContext]
    val kaikkiMuuttuneet: Map[Oid, OppijaHenkilö] = application.authenticationServiceClient.findChangedOppijat(oldContext.lastRun).groupBy(_.oidHenkilo).mapValues(_.head)
    val muuttuneidenOidit: List[Oid] = kaikkiMuuttuneet.values.map(_.toNimitiedotJaOid).filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o) > 0).map(_.oid).toList

    try {
      val updatedToElastic = muuttuneidenOidit.sliding(1000, 1000).map { oidit: List[Oid] =>
        val muuttuneidenHenkilötiedot: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(oidit).map(p => Henkilötiedot(p.id, kaikkiMuuttuneet(p.henkilö.oid).toNimitiedotJaOid))
        application.perustiedotRepository.updateBulk(muuttuneidenHenkilötiedot, insertMissing = false) match {
          case Right(updatedCount) => updatedCount
          case Left(HttpStatus(_, errors)) => throw new Exception(s"Couldn't update data to elasticsearch ${errors.mkString}")
        }
      }.sum
      logger.info(s"Updated ${muuttuneidenOidit.length} entries to henkilö table and $updatedToElastic to elasticsearch")
      henkilöUpdateContext(start)
    } catch {
      case e: Exception => logger.error(e)("Problem running scheduledHenkilötiedotUpdate")
      Some(Json.toJValue(oldContext))
    }
  }

  private def changedHenkilösSince = currentTimeMillis - 10 * 1000 // 10 seconds before now
  private def henkilöUpdateContext(lastRun: Long) = Some(Json.toJValue(HenkilöUpdateContext(lastRun)))
  private def henkilötiedotUpdateInterval = application.config.getDuration("schedule.henkilötiedotUpdateInterval")
}
case class HenkilöUpdateContext(lastRun: Long)
