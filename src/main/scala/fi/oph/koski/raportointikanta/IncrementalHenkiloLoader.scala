package fi.oph.koski.raportointikanta

import fi.oph.koski.henkilo.{KoskiHenkilöCache, LaajatOppijaHenkilöTiedot, OpintopolkuHenkilöFacade}
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository

object IncrementalHenkilöLoader extends Logging {
  private val BatchSize = 1000
  private val ChangeFeedPageSize = 5000
  // Sama 10 minuutin puskuri kuin UpdateHenkilotTaskissa, jotta CPU-kellojen pieni heitto eri instanssien välillä ei jätä muutoksia välistä
  private val BackBufferMs: Long = 10L * 60 * 1000
  private val name = "henkilot"

  def loadHenkilötIncremental(
    opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade,
    previousDb: RaportointiDatabase,
    db: RaportointiDatabase,
    koodistoPalvelu: KoodistoPalvelu,
    opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
    henkilöCache: KoskiHenkilöCache,
    kuntakoodit: Seq[KoodistoKoodi],
  ): Int = {

    val edellinenAloitus = previousDb.getStatusLoadStarted(name).getOrElse {
      throw new IllegalStateException(
        s"Henkilödatan aiemman latauksen aloitusaikaleimaa ei löytynyt skeemasta ${previousDb.schema.name}"
      )
    }
    val sinceMs = edellinenAloitus.toInstant.toEpochMilli - BackBufferMs

    db.setStatusLoadStarted(name)
    logger.info(s"Aloitetaan inkrementaalinen henkilölataus, muutoksia haetaan alkaen $edellinenAloitus (epoch=$sinceMs)")

    val changedOids = readAllChangedOids(opintopolkuHenkilöFacade, sinceMs)
    logger.info(s"Oppijanumerorekisterissä on muuttunut ${changedOids.size} henkilöä")

    val koskenTuntemat = henkilöCache.filterOidsByCache(changedOids).toList.distinct
    logger.info(s"Näistä ${koskenTuntemat.size} on Koskessa tunnettuja master-oideja")

    val total = koskenTuntemat
      .grouped(BatchSize)
      .map(batch => päivitäErä(opintopolkuHenkilöFacade, db, koodistoPalvelu, opiskeluoikeusRepository, henkilöCache, kuntakoodit, batch))
      .sum

    db.setStatusLoadCompletedAndCount(name, total)
    logger.info(s"Inkrementaalinen henkilölataus valmis, päivitettiin $total henkilöä")
    total
  }

  private def päivitäErä(
    opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade,
    db: RaportointiDatabase,
    koodistoPalvelu: KoodistoPalvelu,
    opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
    henkilöCache: KoskiHenkilöCache,
    kuntakoodit: Seq[KoodistoKoodi],
    masterOids: List[String],
  ): Int = {
    val mastersWithSlaves: Map[String, LaajatOppijaHenkilöTiedot] =
      opintopolkuHenkilöFacade.findMasterOppijatWithSlaveOids(masterOids)

    val uniqueMasters = mastersWithSlaves.values.toList.distinctBy(_.oid)
    val kaikkiOidPerMaster: List[(String, LaajatOppijaHenkilöTiedot)] = uniqueMasters.flatMap { master =>
      master.kaikkiOidit.map(oid => oid -> master)
    }
    val tunnetutOidit = henkilöCache.filterOidsByCache(kaikkiOidPerMaster.map(_._1)).toSet
    val rivit = kaikkiOidPerMaster
      .filter { case (oid, _) => tunnetutOidit.contains(oid) }
      .map { case (oid, master) => HenkilöLoader.buildRHenkilöRow(oid, master, koodistoPalvelu, opiskeluoikeusRepository) }

    db.updateHenkilöt(rivit)

    val affectedMasterOids = uniqueMasters.map(_.oid).toSet
    val kotikuntahistoria = fetchKotikuntahistoria(opintopolkuHenkilöFacade, kuntakoodit, affectedMasterOids.toSeq, turvakielto = false)
    db.updateKotikuntahistoria(affectedMasterOids, kotikuntahistoria)

    val kotikuntahistoriaTurvakielto = fetchKotikuntahistoria(opintopolkuHenkilöFacade, kuntakoodit, affectedMasterOids.toSeq, turvakielto = true)
    db.confidential.foreach(_.updateKotikuntahistoria(affectedMasterOids, kotikuntahistoria ++ kotikuntahistoriaTurvakielto))

    rivit.size
  }

  private def fetchKotikuntahistoria(
    opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade,
    kuntakoodit: Seq[KoodistoKoodi],
    masterOids: Seq[String],
    turvakielto: Boolean,
  ): Seq[RKotikuntahistoriaRow] =
    opintopolkuHenkilöFacade
      .findKuntahistoriat(oids = masterOids, turvakiellolliset = turvakielto)
      .fold(
        virhe => throw new RuntimeException(
          s"Kotikuntahistorian haku oppijanumerorekisteristä epäonnistui (turvakielto=$turvakielto): $virhe"
        ),
        _.map(r => r.toDbRow(
          turvakielto = turvakielto,
          kuntakoodi = kuntakoodit.find(_.koodiArvo == r.kotikunta),
        ))
      )

  private def readAllChangedOids(opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade, sinceMs: Long): List[String] = {
    val buffer = scala.collection.mutable.ListBuffer[String]()
    var offset = 0
    var jatka = true
    while (jatka) {
      val page = opintopolkuHenkilöFacade.findChangedOppijaOids(sinceMs, offset, ChangeFeedPageSize)
      buffer ++= page
      offset += ChangeFeedPageSize
      if (page.size < ChangeFeedPageSize) jatka = false
    }
    buffer.toList
  }
}
