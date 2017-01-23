package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.OppijaNumerorekisteriOppija
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.opiskeluoikeus.Henkilötiedot
import fi.oph.koski.util.Timing
import org.json4s.JValue

class KoskiScheduledTasks(application: KoskiApplication) extends Timing {
  val updateHenkilötScheduler =
    new Scheduler(application.database.db, "henkilötiedot-update", new FixedTimeOfDaySchedule(5, 40), henkilöUpdateContext(currentTimeMillis), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    implicit val formats = Json.jsonFormats
    val oldContext = context.get.extract[HenkilöUpdateContext]
    val kaikkiMuuttuneet: Map[String, OppijaNumerorekisteriOppija] = application.authenticationServiceClient.findChangedOppijat(oldContext.lastRun).groupBy(_.oidHenkilo).mapValues(_.head)
    val koskenMuuttuneet: List[OppijaNumerorekisteriOppija] = kaikkiMuuttuneet.values.filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o.toNimitiedotJaOid) > 0).toList
    val muuttuneidenHenkilötiedot: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(koskenMuuttuneet.map(_.oidHenkilo)).map(p => Henkilötiedot(p.id, kaikkiMuuttuneet(p.henkilö.oid).toNimitiedotJaOid))
    application.perustiedotRepository.updateBulk(muuttuneidenHenkilötiedot, insertMissing = false) match {
      case Right(updatedCount) =>
        val msg = s"Updated ${koskenMuuttuneet.length} entries to henkilö table and ${muuttuneidenHenkilötiedot.length} to elasticsearch"
        if (koskenMuuttuneet.length == muuttuneidenHenkilötiedot.length)
          logger.info(msg)
        else logger.warn(msg)
        henkilöUpdateContext(lastModified(koskenMuuttuneet))
      case Left(HttpStatus(_, errors)) =>
        logger.error(s"Problem running scheduledHenkilötiedotUpdate ${errors.mkString}")
        Some(Json.toJValue(oldContext))
    }
  }

  private def defaultTimestamp = currentTimeMillis - 10000
  private def henkilöUpdateContext(lastRun: Long) = Some(Json.toJValue(HenkilöUpdateContext(lastRun)))
  private def lastModified(oppijat: List[OppijaNumerorekisteriOppija]) =
    if (oppijat.isEmpty) defaultTimestamp
    else oppijat.maxBy(_.modified).modified
}
case class HenkilöUpdateContext(lastRun: Long)
