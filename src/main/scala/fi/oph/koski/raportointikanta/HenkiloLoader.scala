package fi.oph.koski.raportointikanta

import fi.oph.koski.henkilo.{Hetu, OpintopolkuHenkilöFacade}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.log.Logging
import java.sql.Date

object HenkilöLoader extends Logging {
  private val BatchSize = 1000
  private val name = "henkilot"

  def loadHenkilöt(opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade, db: RaportointiDatabase): Int = {
    logger.info("Ladataan henkilö-OIDeja opiskeluoikeuksista...")
    // note: this list has 1-2M oids in production.
    val oids = db.oppijaOidsFromOpiskeluoikeudet
    logger.info(s"Löytyi ${oids.size} henkilö-OIDia")
    db.setStatusLoadStarted(name)
    db.deleteHenkilöt
    var masterOids = scala.collection.mutable.Set[String]()
    val count = oids.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findMasterOppijat(batchOids)
      val batchRows = batchOppijat.map { case (oid, oppija) => buildRHenkilöRow(oid, oppija) }.toList
      db.loadHenkilöt(batchRows)
      db.setLastUpdate(name)
      batchRows.foreach(masterOids += _.masterOid)
      batchRows.size
    }).sum

    val masterOidsEiKoskessa = masterOids.diff(oids.toSet)

    val masterFetchCount =  masterOidsEiKoskessa.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findMasterOppijat(batchOids)
      val batchRows = batchOppijat.map { case (oid, oppija) => buildRHenkilöRow(oid, oppija) }.toList
      db.loadHenkilöt(batchRows)
      batchRows.size
    }).sum
    val total = count + masterFetchCount

    db.setStatusLoadCompletedAndCount(name, total)
    logger.info(s"Ladattiin $total henkilöä")
    logger.info(s"Haettiin masterMaster tiedot $masterFetchCount henkilölle")
    logger.info(s"Puuttuvia masterMaster pareja ${masterOidsEiKoskessa.size - masterFetchCount}")
    total
  }

  private def buildRHenkilöRow(oid: String, oppija: LaajatOppijaHenkilöTiedot) =
    RHenkilöRow(
      oppijaOid = oid,
      masterOid = oppija.oid,
      hetu = oppija.hetu,
      sukupuoli = oppija.sukupuoli,
      syntymäaika = oppija.syntymäaika.orElse(oppija.hetu.flatMap(Hetu.toBirthday)).map(Date.valueOf),
      sukunimi = oppija.sukunimi,
      etunimet = oppija.etunimet,
      aidinkieli = oppija.äidinkieli,
      kansalaisuus = oppija.kansalaisuus.filter(_.nonEmpty).map(_.sorted.mkString(",")),
      turvakielto = oppija.turvakielto,
      kotikunta = oppija.kotikunta
    )
}
