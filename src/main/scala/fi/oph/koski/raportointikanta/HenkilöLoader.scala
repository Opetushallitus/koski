package fi.oph.koski.raportointikanta

import fi.oph.koski.henkilo.{Hetu, OpintopolkuHenkilöFacade}
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.log.Logging
import java.sql.Date

object HenkilöLoader extends Logging {
  private val BatchSize = 1000

  def loadHenkilöt(raportointiDatabase: RaportointiDatabase, opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade): Int = {
    logger.info("Ladataan henkilö-OIDeja opiskeluoikeuksista...")
    // note: this list has 1-2M oids in production.
    val oids = raportointiDatabase.oppijaOidsFromOpiskeluoikeudet
    logger.info(s"Löytyi ${oids.size} henkilö-OIDia")
    raportointiDatabase.setStatusLoadStarted("henkilot")
    raportointiDatabase.deleteHenkilöt
    val count = oids.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findOppijatByOids(batchOids)
      val batchRows = batchOppijat.map(buildRHenkilöRow)
      raportointiDatabase.loadHenkilöt(batchRows)
      batchRows.size
    }).sum
    raportointiDatabase.setStatusLoadCompleted("henkilot")
    logger.info(s"Ladattiin $count henkilöä")
    count
  }

  private def buildRHenkilöRow(oppija: OppijaHenkilö) =
    RHenkilöRow(
      oppijaOid = oppija.oid,
      hetu = oppija.hetu,
      syntymäaika = oppija.syntymäaika.orElse(oppija.hetu.flatMap(Hetu.toBirthday)).map(Date.valueOf),
      sukunimi = oppija.sukunimi,
      etunimet = oppija.etunimet,
      aidinkieli = oppija.äidinkieli,
      kansalaisuus = oppija.kansalaisuus.filter(_.nonEmpty).map(_.sorted.mkString(",")),
      turvakielto = oppija.turvakielto
    )
}
