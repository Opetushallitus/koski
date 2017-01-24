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
    new Scheduler(application.database.db, "henkilötiedot-update", new FixedTimeOfDaySchedule(5, 40), henkilöUpdateContext(changedHenkilösSince), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    implicit val formats = Json.jsonFormats
    val start = changedHenkilösSince
    val oldContext = context.get.extract[HenkilöUpdateContext]
    val kaikkiMuuttuneet: Map[Oid, OppijaHenkilö] = application.authenticationServiceClient.findChangedOppijat(oldContext.lastRun).groupBy(_.oidHenkilo).mapValues(_.head)
    val muuttuneidenOidit: List[Oid] = kaikkiMuuttuneet.values.map(_.toNimitiedotJaOid).filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o) > 0).map(_.oid).toList
    val muuttuneidenHenkilötiedot: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(muuttuneidenOidit).map(p => Henkilötiedot(p.id, kaikkiMuuttuneet(p.henkilö.oid).toNimitiedotJaOid))
    application.perustiedotRepository.updateBulk(muuttuneidenHenkilötiedot, insertMissing = false) match {
      case Right(updatedCount) =>
        val msg = s"Updated ${muuttuneidenOidit.length} entries to henkilö table and ${muuttuneidenHenkilötiedot.length} to elasticsearch"
        if (muuttuneidenOidit.length == muuttuneidenHenkilötiedot.length)
          logger.info(msg)
        else logger.warn(msg)
        henkilöUpdateContext(start)
      case Left(HttpStatus(_, errors)) =>
        logger.error(s"Problem running scheduledHenkilötiedotUpdate ${errors.mkString}")
        Some(Json.toJValue(oldContext))
    }
  }

  private def changedHenkilösSince = currentTimeMillis - 60 * 1000 // one minute before now
  private def henkilöUpdateContext(lastRun: Long) = Some(Json.toJValue(HenkilöUpdateContext(lastRun)))
}
case class HenkilöUpdateContext(lastRun: Long)
