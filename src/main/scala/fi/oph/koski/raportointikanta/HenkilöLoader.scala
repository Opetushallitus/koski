package fi.oph.koski.raportointikanta

import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.henkilo.oppijanumerorekisteriservice.OppijaHenkilö
import fi.oph.koski.log.Logging

object HenkilöLoader extends Logging {
  private val BatchSize = 1000

  def loadHenkilöt(raportointiDatabase: RaportointiDatabase, opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade): Int = {
    logger.info("Ladataan henkilö-OIDeja opiskeluoikeuksista...")
    // note: this list has 1-2M oids in production.
    val oids = raportointiDatabase.oppijaOidsFromOpiskeluoikeudet
    logger.info(s"Löytyi ${oids.size} henkilö-OIDia")
    raportointiDatabase.deleteHenkilöt
    val count = oids.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findOppijatByOids(batchOids)
      val batchRows = batchOppijat.map(buildRHenkilöRow)
      raportointiDatabase.loadHenkilöt(batchRows)
      batchRows.size
    }).sum
    logger.info(s"Ladattiin $count henkilöä")
    count
  }

  private def buildRHenkilöRow(oppija: OppijaHenkilö) =
    RHenkilöRow(
      oppijaOid = oppija.oidHenkilo,
      hetu = oppija.hetu,
      sukunimi = oppija.sukunimi,
      etunimet = oppija.etunimet,
      aidinkieli = oppija.aidinkieli,
      kansalaisuus = oppija.kansalaisuus.filter(_.nonEmpty).map(_.mkString(",")),
      turvakielto = oppija.turvakielto
    )
}
