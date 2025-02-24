package fi.oph.koski.raportointikanta

import fi.oph.koski.henkilo.{Hetu, LaajatOppijaHenkilöTiedot, OpintopolkuHenkilöFacade}
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu, Kunta}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository

import java.sql.Date

object HenkilöLoader extends Logging {
  private val BatchSize = 1000
  private val name = "henkilot"

  def loadHenkilöt(opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade,
                   db: RaportointiDatabase,
                   koodistoPalvelu: KoodistoPalvelu,
                   opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
                   kuntakoodit: Seq[KoodistoKoodi]): Int = {
    def fetchKotikuntahistoria(batchRows: Seq[RHenkilöRow], turvakielto: Boolean) =
      opintopolkuHenkilöFacade
        .findKuntahistoriat(oids = batchRows.map(_.masterOid), turvakiellolliset = turvakielto)
        .getOrElse(Seq.empty)
        .map(r => r.toDbRow(
          turvakielto = turvakielto,
          kuntakoodi = kuntakoodit.find(_.koodiArvo == r.kotikunta),
        ))

    logger.info("Ladataan henkilö-OIDeja opiskeluoikeuksista...")
    // note: this list has 1-2M oids in production.
    val oids = db.oppijaOidsFromOpiskeluoikeudet
    logger.info(s"Löytyi ${oids.size} henkilö-OIDia")
    db.setStatusLoadStarted(name)
    val masterOids = scala.collection.mutable.Set[String]()
    val count = oids.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findMasterOppijat(batchOids)
      val batchRows = batchOppijat.map { case (oid, oppija) => {
        buildRHenkilöRow(oid, oppija, koodistoPalvelu, opiskeluoikeusRepository)
      } }.toList
      db.loadHenkilöt(batchRows)
      db.setLastUpdate(name)
      batchRows.foreach(masterOids += _.masterOid)

      val kotikuntahistoria = fetchKotikuntahistoria(batchRows, turvakielto = false)
      db.loadKotikuntahistoria(kotikuntahistoria)

      val kotikuntahistoriaTurvakielto = fetchKotikuntahistoria(batchRows, turvakielto = true)
      db.confidential.foreach(_.loadKotikuntahistoria(kotikuntahistoria ++ kotikuntahistoriaTurvakielto))

      batchRows.size
    }).sum

    val masterOidsEiKoskessa = masterOids.diff(oids.toSet)

    val masterFetchCount =  masterOidsEiKoskessa.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findMasterOppijat(batchOids)
      val batchRows = batchOppijat.map { case (oid, oppija) => buildRHenkilöRow(oid, oppija, koodistoPalvelu, opiskeluoikeusRepository) }.toList
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

  private def buildRHenkilöRow(oid: String,
                               oppija: LaajatOppijaHenkilöTiedot,
                               koodistoPalvelu: KoodistoPalvelu,
                               opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository) =
    RHenkilöRow(
      oppijaOid = oid,
      masterOid = oppija.oid,
      linkitetytOidit = oppija.linkitetytOidit,
      hetu = oppija.hetu,
      sukupuoli = oppija.sukupuoli,
      syntymäaika = oppija.syntymäaika.orElse(oppija.hetu.flatMap(Hetu.toBirthday)).map(Date.valueOf),
      sukunimi = oppija.sukunimi,
      etunimet = oppija.etunimet,
      aidinkieli = oppija.äidinkieli,
      kansalaisuus = oppija.kansalaisuus.filter(_.nonEmpty).map(_.sorted.mkString(",")),
      turvakielto = oppija.turvakielto,
      kotikunta = oppija.kotikunta,
      kotikuntaNimiFi = Kunta.getKunnanNimi(oppija.kotikunta, koodistoPalvelu, "fi"),
      kotikuntaNimiSv = Kunta.getKunnanNimi(oppija.kotikunta, koodistoPalvelu, "sv"),
      yksiloity =  oppija.yksilöity
    )
}
