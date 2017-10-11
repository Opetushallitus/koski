package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.authenticationservice.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.{NimitiedotJaOid, OpiskeluoikeudenHenkilötiedot}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedotWithMasterInfo
import fi.oph.koski.util.Timing
import org.json4s.JValue

class UpdateHenkilotTask(application: KoskiApplication) extends Timing {
  def scheduler = new Scheduler(application.masterDatabase.db, "henkilötiedot-update", new IntervalSchedule(henkilötiedotUpdateInterval), henkilöUpdateContext(currentTimeMillis), updateHenkilöt)

  def updateHenkilöt(context: Option[JValue]): Option[JValue] = timed("scheduledHenkilötiedotUpdate") {
    try {
      val oldContext = JsonSerializer.extract[HenkilöUpdateContext](context.get)
      val startMillis = currentTimeMillis
      val changedOids = application.authenticationServiceClient.findChangedOppijaOids(oldContext.lastRun)
      val newContext = runUpdate(changedOids, startMillis, oldContext)
      Some(JsonSerializer.serializeWithRoot(newContext))
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running scheduledHenkilötiedotUpdate")
        context
    }
  }

  private def runUpdate(oids: List[Oid], startMillis: Long, lastContext: HenkilöUpdateContext) = {

    val oppijat: List[OppijaHenkilö] = application.authenticationServiceClient.findOppijatByOids(oids).sortBy(_.modified)
    // TODO: hae yllä vain ne, jotka löytyvät Koskesta!

    val oppijatWithMaster: List[WithModifiedTime] = oppijat.map { oppija =>
      WithModifiedTime(application.henkilöRepository.opintopolku.withMasterInfo(oppija.toTäydellisetHenkilötiedot), oppija.modified)
    }

    val oppijatByOid: Map[Oid, WithModifiedTime] = oppijatWithMaster.groupBy(_.tiedot.henkilö.oid).mapValues(_.head)

    val updatedInKoskiHenkilöCache: List[Oid] = oppijatWithMaster
      .filter(o => application.henkilöCache.updateHenkilöAction(o.tiedot) > 0)
      .map(_.tiedot.henkilö.oid)

    val lastModified = oppijat.lastOption.map(o => o.modified + 1).getOrElse(startMillis)

    if (updatedInKoskiHenkilöCache.isEmpty) {
      HenkilöUpdateContext(lastModified)
    } else {
      val muuttuneidenHenkilötiedot: List[OpiskeluoikeudenHenkilötiedot] = application.perustiedotRepository
        .findHenkiloPerustiedotByOids(updatedInKoskiHenkilöCache)
        .map(p => {
          val päivitetytTiedot = oppijatByOid(p.henkilö.oid)
          OpiskeluoikeudenHenkilötiedot(p.id, NimitiedotJaOid(päivitetytTiedot.tiedot.henkilö), päivitetytTiedot.tiedot.master.map(NimitiedotJaOid.apply))
        })

      application.perustiedotIndexer.updateBulk(muuttuneidenHenkilötiedot, replaceDocument = false) match {
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

  private def henkilöUpdateContext(lastRun: Long) = Some(JsonSerializer.serializeWithRoot(HenkilöUpdateContext(lastRun)))
  private def henkilötiedotUpdateInterval = application.config.getDuration("schedule.henkilötiedotUpdateInterval")
}

case class WithModifiedTime(tiedot: TäydellisetHenkilötiedotWithMasterInfo, modified: Long)
case class HenkilöUpdateContext(lastRun: Long)
