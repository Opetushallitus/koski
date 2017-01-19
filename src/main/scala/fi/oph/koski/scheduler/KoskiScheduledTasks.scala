package fi.oph.koski.scheduler

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.OppijaHenkilö
import fi.oph.koski.opiskeluoikeus.{Henkilötiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.NimitiedotJaOid
import org.joda.time.DateTime

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler =
    new Scheduler(application.database.db, "henkilötiedot-update", new FixedTimeOfDaySchedule(5, 40), () => updateHenkilöt())

  def updateHenkilöt() {
    val since = DateTime.now().minusDays(1)
    val kaikkiMuuttuneet: Map[Oid, OppijaHenkilö] = application.authenticationServiceClient.findChangedOppijat(since).groupBy(_.oidHenkilo).mapValues(_.head)
    val koskenMuuttuneet: List[Oid] = kaikkiMuuttuneet.values.map(_.toNimitiedotJaOid).filter(o => application.henkilöCacheUpdater.updateHenkilöAction(o) > 0).map(_.oid).toList
    val koskeenPäivitetyt: List[Henkilötiedot] = application.perustiedotRepository.findHenkiloPerustiedotByOids(koskenMuuttuneet).map(p => Henkilötiedot(p.id, kaikkiMuuttuneet(p.henkilö.oid).toNimitiedotJaOid))
    application.perustiedotRepository.updateBulk(koskeenPäivitetyt, insertMissing = false)
  }
}
