package fi.oph.koski.schedule

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.OppijaHenkilö
import fi.oph.koski.json.Json
import fi.oph.koski.opiskeluoikeus.Henkilötiedot
import fi.oph.koski.schema.Henkilö.Oid
import org.json4s.JValue

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler =
    new Scheduler(application.database.db, "henkilötiedot-update", new FixedTimeOfDaySchedule(5, 40), henkilöUpdateContext(now), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = {
    implicit val formats = Json.jsonFormats
    val start = now
    val lastRun = context.get.extract[HenkilöUpdateContext].lastRun
    val kaikkiMuuttuneet: Map[Oid, OppijaHenkilö] = application.authenticationServiceClient.findChangedOppijat(lastRun).groupBy(_.oidHenkilo).mapValues(_.head)
    val koskenMuuttuneet: List[Oid] = kaikkiMuuttuneet.values.map(_.toNimitiedotJaOid).filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o) > 0).map(_.oid).toList
    val koskeenPäivitetyt: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(koskenMuuttuneet).map(p => Henkilötiedot(p.id, kaikkiMuuttuneet(p.henkilö.oid).toNimitiedotJaOid))
    application.perustiedotRepository.updateBulk(koskeenPäivitetyt, insertMissing = false)
    henkilöUpdateContext(start)
  }

  private def henkilöUpdateContext(lastRun: LocalDateTime) = Some(Json.toJValue(HenkilöUpdateContext(lastRun)))
}
case class HenkilöUpdateContext(lastRun: LocalDateTime)
