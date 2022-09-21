package fi.oph.koski.raportointikanta

import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.oppija.ValpasOppivelvollisuudestaVapautusService

import java.sql.Timestamp

object OppivelvollisuudenVapautusLoader extends Logging {
  def loadOppivelvollisuudestaVapautukset(ovVapautusService: ValpasOppivelvollisuudestaVapautusService, db: RaportointiDatabase): Int = {
    logger.info("Ladataan oppivelvollisuudesta vapautuksia...")
    var rowCount = 0
    ovVapautusService.kaikkiVapautuksetIterator(1000)
      .map(_.map(v => ROppivelvollisuudestaVapautusRow(
        oppijaOid = v.oppijaOid,
        vapautettu = Timestamp.valueOf(v.vapautettu.atStartOfDay),
      )))
      .foreach(rows => {
        db.loadOppivelvollisuudenVapautukset(rows)
        rowCount += rows.length
      })
    logger.info(s"Ladattiin $rowCount oppivelvollisuudesta vapautusta")
    rowCount
  }
}
