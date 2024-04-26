package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, DatabaseUtilQueries, QueryMethods, RaportointiDatabaseConfigBase}
import fi.oph.koski.henkilo.Kotikuntahistoria
import fi.oph.koski.log.Logging
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.raportit.PaallekkaisetOpiskeluoikeudet
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019AineopintojenOpintopistekertymat, Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet, Lukio2019OppiaineRahoitusmuodonMukaan, Lukio2019OppimaaranOpintopistekertymat}
import fi.oph.koski.raportit.lukio.{LukioOppiaineEriVuonnaKorotetutKurssit, LukioOppiaineRahoitusmuodonMukaan, LukioOppiaineenOppimaaranKurssikertymat, LukioOppimaaranKussikertymat}
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema._
import fi.oph.koski.schema.Opiskeluoikeus.Oid
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Organisaatio}
import fi.oph.koski.util.DateOrdering.{ascedingSqlTimestampOrdering, sqlDateOrdering}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.scalaschema.annotation.SyntheticProperty
import org.postgresql.util.PSQLException
import slick.dbio.DBIO

import java.sql.Timestamp.{valueOf => toTimestamp}
import java.sql.{Date, Timestamp}
import java.time.LocalDateTime.now
import java.time._
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object RaportointiDatabase {
  // Raportointikannan skeeman muuttuessa päivitä versionumeroa yksi ylöspäin.
  // Jälkimmäinen arvo on skeemasta laskettu tunniste (kts. QueryMethods::getSchemaHash).
  // Testi nimeltä "Schema version has been updated" tarkastaa että versionumeroa päivitetään skeemamuutosten
  // myötä.
  def schemaVersion: (Int, String) = (11, "2b4ae3cd9516b92e3c3e51ffce497273")
}

class RaportointiDatabase(config: RaportointiDatabaseConfigBase) extends Logging with QueryMethods {
  val schema: Schema = config.schema
  val confidential: Option[ConfidentialRaportointiDatabase] = ConfidentialRaportointiDatabase(config)
  override def defaultRetryIntervalMs = 30000

  logger.info(s"Instantiating RaportointiDatabase for ${schema.name}")

  val db: DB = config.toSlickDatabase

  final val smallDatabaseMaxRows = 500

  val util = new DatabaseUtilQueries(db, ROpiskeluoikeudet.length.result, smallDatabaseMaxRows)

  val tables = List(
    ROpiskeluoikeudet,
    RMitätöidytOpiskeluoikeudet,
    ROrganisaatioHistoriat,
    ROpiskeluoikeusAikajaksot,
    EsiopetusOpiskeluoikeusAikajaksot,
    RPäätasonSuoritukset,
    ROsasuoritukset,
    RHenkilöt,
    ROrganisaatiot,
    ROrganisaatioKielet,
    RKoodistoKoodit,
    RaportointikantaStatus,
    MuuAmmatillinenOsasuoritusRaportointi,
    TOPKSAmmatillinenOsasuoritusRaportointi,
    ROppivelvollisuudestaVapautukset,
    RYtrTutkintokokonaisuudenSuoritukset,
    RYtrTutkintokerranSuoritukset,
    RYtrKokeenSuoritukset,
    RYtrTutkintokokonaisuudenKokeenSuoritukset,
    RKotikuntahistoria,
  )

  def vacuumAnalyze(): Unit = {
    logger.info("Starting VACUUM ANALYZE in RaportointiDatabase")
    db.run(sqlu"VACUUM ANALYZE")
    logger.info("Finished VACUUM ANALYZE in RaportointiDatabase")
  }

  def moveTo(newSchema: Schema): Unit = {
    logger.info(s"Moving ${schema.name} -> ${newSchema.name}")
    runDbSync(DBIO.seq(
      schema.moveSchema(newSchema),
      RaportointiDatabaseSchema.createRolesIfNotExists,
      newSchema.grantPermissions()
    ))
  }

  def dropPublicAndMoveTempToPublic: Unit = {
    // Raportointikannan swappaaminen saattaa epäonnistua timeouttiin, jos esim. käyttäjä on juuri ajamassa
    // hidasta raporttia. Parempi yrittää uudestaan, eikä lopettaa monen tunnin operaatiota vain tästä syystä.
    retryDbSync(
      DBIO.seq(
        Public.dropSchema(),
        Confidential.dropSchema(),
        Temp.moveSchema(Public),
        TempConfidential.moveSchema(Confidential),
        RaportointiDatabaseSchema.createRolesIfNotExists,
        Public.grantPermissions(),
        Confidential.grantPermissions(),
      ).transactionally,
      timeout = 5.minutes
    )
    logger.info("RaportointiDatabase schema swapped")
  }

  def dropAndCreateObjects(previousDataVersion: Option[Int] = None): Unit = {
    logger.info(s"Creating database ${schema.name}")
    runDbSync(DBIO.sequence(
      Seq(
        schema.recreateSchema(),
      ) ++
      tables.map(_.schema.create) ++
      Seq(
        RaportointiDatabaseSchema.createRolesIfNotExists,
        schema.grantPermissions(),
      )
    ).transactionally)
    previousDataVersion.foreach(v => writeVersionInfo(v + 1))
    logger.info(s"${schema.name} created")

    confidential.foreach(_.dropAndCreateObjects(previousDataVersion))
  }

  private def writeVersionInfo(dataVersion: Int) = {
    setStatusLoadStarted("version_schema")
    setStatusLoadCompletedAndCount("version_schema", RaportointiDatabase.schemaVersion._1)
    setStatusLoadStarted("version_data")
    setStatusLoadCompletedAndCount("version_data", dataVersion)
  }

  def createIndexesForIncrementalUpdate(): Unit = {
    runDbSync(schema.createIndexesForIncrementalUpdate(), timeout = 120.minutes)
    confidential.foreach(_.createIndexesForIncrementalUpdate())
  }

  def createOpiskeluoikeusIndexes(): Unit = {
    runDbSync(schema.createOpiskeluoikeusIndexes(), timeout = 120.minutes)
    confidential.foreach(_.createOpiskeluoikeusIndexes())
  }

  def createOtherIndexes(): Unit = {
    val indexStartTime = System.currentTimeMillis
    logger.info("Luodaan henkilö-, organisaatio- ja koodisto-indeksit")
    runDbSync(schema.createOtherIndexes(), timeout = 120.minutes)
    val indexElapsedSeconds = (System.currentTimeMillis - indexStartTime)/1000
    logger.info(s"Luotiin henkilö-, organisaatio- ja koodisto-indeksit, ${indexElapsedSeconds} s")
    confidential.foreach(_.createOtherIndexes())
  }

  def createPrecomputedTables(valpasRajapäivätService: ValpasRajapäivätService): Unit = {
    logger.info("Creating precomputed tables (formerly materialized views)")
    val started = System.currentTimeMillis
    setStatusLoadStarted("materialized_views")

    val ConfidentialSchema = confidential.map(_.schema).getOrElse(schema)
    val views = Seq(
      PaallekkaisetOpiskeluoikeudet.createPrecomputedTable(schema),
      PaallekkaisetOpiskeluoikeudet.createIndex(schema),
      LukioOppimaaranKussikertymat.createPrecomputedTable(schema),
      LukioOppimaaranKussikertymat.createIndex(schema),
      Lukio2019OppimaaranOpintopistekertymat.createPrecomputedTable(schema),
      Lukio2019OppimaaranOpintopistekertymat.createIndex(schema),
      OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.createPrecomputedTable(schema),
      OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.createIndex(schema),
      LukioOppiaineenOppimaaranKurssikertymat.createPrecomputedTable(schema),
      LukioOppiaineenOppimaaranKurssikertymat.createIndex(schema),
      Lukio2019AineopintojenOpintopistekertymat.createPrecomputedTable(schema),
      Lukio2019AineopintojenOpintopistekertymat.createIndex(schema),
      LukioOppiaineRahoitusmuodonMukaan.createPrecomputedTable(schema),
      LukioOppiaineRahoitusmuodonMukaan.createIndex(schema),
      Lukio2019OppiaineRahoitusmuodonMukaan.createPrecomputedTable(schema),
      Lukio2019OppiaineRahoitusmuodonMukaan.createIndex(schema),
      LukioOppiaineEriVuonnaKorotetutKurssit.createPrecomputedTable(schema),
      LukioOppiaineEriVuonnaKorotetutKurssit.createIndex(schema),
      Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet.createPrecomputedTable(schema),
      Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet.createIndex(schema),
      Oppivelvollisuustiedot.createPrecomputedTable(schema, ConfidentialSchema, valpasRajapäivätService),
      Oppivelvollisuustiedot.createIndexes(schema),
    )
    runDbSync(DBIO.seq(views: _*), timeout = 120.minutes)

    val vipunen = new VipunenExport(db, schema)
    vipunen.createAndPopulateHenkilöTable

    setStatusLoadCompletedAndCount("materialized_views", views.length)

    val duration = (System.currentTimeMillis - started) / 1000
    logger.info(s"Precomputed tables created in $duration s")
  }

  def createCustomFunctions(): Unit = {
    setStatusLoadStarted("custom_functions")
    val customFunctions = Seq(
      RaportointiDatabaseCustomFunctions.vuodenViimeinenPäivämäärä(schema),
    )
    runDbSync(DBIO.seq(customFunctions: _*), timeout = 120.minutes)
    setStatusLoadCompletedAndCount("custom_functions", customFunctions.length)
  }

  def loadOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow]): Unit = {
    runDbSync(ROpiskeluoikeudet ++= opiskeluoikeudet, timeout = 5.minutes)
  }

  def cloneUpdateableTables(source: RaportointiDatabase, enableYtr: Boolean): Unit = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._

    val BATCH_SIZE = 10000000
    val BATCH_WARNING_TIME_LIMIT = 10.minutes

    case class Kloonaus(
      taulu: String,
      primaryKeys: List[String] = List.empty,
      timeout: FiniteDuration = 120.minutes,
    );

    val kloonattavatTaulut = List(
      Kloonaus("r_opiskeluoikeus", List("opiskeluoikeus_oid")),
      Kloonaus("r_organisaatiohistoria"),
      Kloonaus("esiopetus_opiskeluoik_aikajakso"),
      Kloonaus("r_opiskeluoikeus_aikajakso", List("id")),
      Kloonaus("r_paatason_suoritus", List("paatason_suoritus_id")),
      Kloonaus("r_osasuoritus", List("osasuoritus_id")),
      Kloonaus("muu_ammatillinen_raportointi"),
      Kloonaus("topks_ammatillinen_raportointi"),
      Kloonaus("r_mitatoitu_opiskeluoikeus", List("opiskeluoikeus_oid"))
    ) ++ (if (enableYtr) {
      List(
        Kloonaus("r_ytr_tutkintokokonaisuuden_suoritus", List("ytr_tutkintokokonaisuuden_suoritus_id")),
        Kloonaus("r_ytr_tutkintokerran_suoritus", List("ytr_tutkintokerran_suoritus_id")),
        Kloonaus("r_ytr_kokeen_suoritus", List("ytr_kokeen_suoritus_id")),
        Kloonaus("r_ytr_tutkintokokonaisuuden_kokeen_suoritus", List("ytr_tutkintokokonaisuuden_suoritus_id", "ytr_kokeen_suoritus_id")),
      )
    } else {
      List.empty
    })

    val päivitettävätIdSekvenssit = List(
      ("r_opiskeluoikeus_aikajakso", "id"),
    )

    kloonattavatTaulut.foreach { kloonaus =>
      val startTime = System.currentTimeMillis
      val count = runDbSync(sql"""SELECT COUNT(*) FROM #${source.schema.name}.#${kloonaus.taulu}""".as[Long]).head
      val batchCount = (count / BATCH_SIZE).ceil.toInt
      logger.info(s"Kopioidaan ${count} riviä ${source.schema.name}.${kloonaus.taulu} --> ${schema.name}.${kloonaus.taulu}")

      Range.inclusive(0, batchCount).foreach(i => {
        val batchStartTime = System.currentTimeMillis
        val offset = i * BATCH_SIZE

        logger.info(s"Kopioidaan erä ${i + 1}/${batchCount + 1}...")

        val rowsCloned = retryDbSync(
            sql"""
                 INSERT INTO #${schema.name}.#${kloonaus.taulu}
                 SELECT * FROM #${source.schema.name}.#${kloonaus.taulu}
                 #${kloonaus.primaryKeys.mkString(", ") match {
                   case "" => ""
                   case pks => s"ORDER BY $pks"
                 }}
                 LIMIT $BATCH_SIZE OFFSET $offset
            """.asUpdate,
            timeout = kloonaus.timeout,
          )

        val elapsedSeconds = (System.currentTimeMillis - batchStartTime) / 1000
        logger.info(s"Kopioitiin erä ${i + 1}/${batchCount + 1} (${rowsCloned} riviä), $elapsedSeconds s")
        if (elapsedSeconds.toInt.seconds > BATCH_WARNING_TIME_LIMIT) {
          logger.warn(s"Taulukloonauksen erä ${source.schema.name}.${kloonaus.taulu} --> ${schema.name}.${kloonaus.taulu} kesti yli $BATCH_WARNING_TIME_LIMIT")
        }
      })

      val elapsedSeconds = (System.currentTimeMillis - startTime) / 1000
      logger.info(s"Kopioitiin rivit ${source.schema.name}.${kloonaus.taulu} --> ${schema.name}.${kloonaus.taulu}, ${elapsedSeconds} s")
    }

    päivitettävätIdSekvenssit.foreach { seq =>
      val (taulu, col) = seq
      runDbSync(sql"""SELECT setval('#${schema.name}.#${taulu}_#${col}_seq', (SELECT max(#${col}) FROM #${schema.name}.#${taulu}))""".as[Long])
    }
  }

  def getLatestSuoritusId: Long =
    runDbSync(
      sql"""
        SELECT greatest(
          (SELECT max(paatason_suoritus_id) FROM #${schema.name}.r_paatason_suoritus),
          (SELECT max(osasuoritus_id) FROM #${schema.name}.r_osasuoritus)
        )
      """.as[Option[Long]])
      .head
      .getOrElse(0)

  def updateOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow], mitätöidytOpiskeluoikeudet: Seq[Oid]): Unit = {
    runDbSync(DBIO.sequence(opiskeluoikeudet.map(ROpiskeluoikeudet.insertOrUpdate)), timeout = 5.minutes)
    runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSetBind mitätöidytOpiskeluoikeudet).delete, timeout = 5.minutes)
  }

  def loadMitätöidytOpiskeluoikeudet(rows: Seq[RMitätöityOpiskeluoikeusRow]): Unit = {
    runDbSync(RMitätöidytOpiskeluoikeudet ++= rows, timeout = 5.minutes)
  }

  def updateMitätöidytOpiskeluoikeudet(rows: Seq[RMitätöityOpiskeluoikeusRow], olemassaolevatOot: Seq[Opiskeluoikeus.Oid]): Unit = {
    runDbSync(DBIO.sequence(rows.map(RMitätöidytOpiskeluoikeudet.insertOrUpdate)), timeout = 5.minutes)
    runDbSync(RMitätöidytOpiskeluoikeudet.filter(_.opiskeluoikeusOid inSetBind olemassaolevatOot).delete, timeout = 5.minutes)
  }

  def oppijaOidsFromOpiskeluoikeudet: Seq[String] = {
    runDbSync(ROpiskeluoikeudet.map(_.oppijaOid).distinct.result, timeout = 15.minutes)
  }

  def loadOrganisaatioHistoria(organisaatioHistoriat: Seq[ROrganisaatioHistoriaRow]): Unit =
    runDbSync(ROrganisaatioHistoriat ++= organisaatioHistoriat)

  def updateOrganisaatioHistoria(organisaatioHistoriat: Seq[ROrganisaatioHistoriaRow]): Unit = {
    val opiskeluoikeusOids = organisaatioHistoriat.map(_.opiskeluoikeusOid).toSet
    runDbSync(ROrganisaatioHistoriat.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).delete, timeout = 5.minutes)
    runDbSync(ROrganisaatioHistoriat ++= organisaatioHistoriat, timeout = 5.minutes)
  }

  def loadOpiskeluoikeusAikajaksot(jaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(ROpiskeluoikeusAikajaksot ++= jaksot)

  def updateOpiskeluoikeusAikajaksot(jaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Unit = {
    val opiskeluoikeusOids = jaksot.map(_.opiskeluoikeusOid).toSet
    runDbSync(ROpiskeluoikeusAikajaksot.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).delete, timeout = 5.minutes)
    runDbSync(ROpiskeluoikeusAikajaksot ++= jaksot, timeout = 5.minutes)
  }

  def loadEsiopetusOpiskeluoikeusAikajaksot(jaksot: Seq[EsiopetusOpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(EsiopetusOpiskeluoikeusAikajaksot ++= jaksot)

  def updateEsiopetusOpiskeluoikeusAikajaksot(jaksot: Seq[EsiopetusOpiskeluoikeusAikajaksoRow]): Unit = {
    val opiskeluoikeusOids = jaksot.map(_.opiskeluoikeusOid).toSet
    runDbSync(EsiopetusOpiskeluoikeusAikajaksot.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).delete, timeout = 5.minutes)
    runDbSync(EsiopetusOpiskeluoikeusAikajaksot ++= jaksot, timeout = 5.minutes)
  }

  def loadPäätasonSuoritukset(suoritukset: Seq[RPäätasonSuoritusRow]): Unit =
    runDbSync(RPäätasonSuoritukset ++= suoritukset)

  def updatePäätasonSuoritukset(suoritukset: Seq[RPäätasonSuoritusRow]): Unit = {
    val opiskeluoikeusOids = suoritukset.map(_.opiskeluoikeusOid).toSet
    runDbSync(RPäätasonSuoritukset.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).delete, timeout = 5.minutes)
    runDbSync(RPäätasonSuoritukset ++= suoritukset, timeout = 5.minutes)
  }

  def loadOsasuoritukset(suoritukset: Seq[ROsasuoritusRow]): Unit =
    runDbSync(ROsasuoritukset ++= suoritukset, timeout = 5.minutes)

  def loadYtrOsasuoritukset(
    tutkintokokonaisuudet: Seq[RYtrTutkintokokonaisuudenSuoritusRow],
    tutkintokerrat: Seq[RYtrTutkintokerranSuoritusRow],
    kokeet: Seq[RYtrKokeenSuoritusRow],
    tutkintokokonaisuudenKokeet: Seq[RYtrTutkintokokonaisuudenKokeenSuoritusRow]
  ): Unit = {
    runDbSync(RYtrTutkintokokonaisuudenSuoritukset ++= tutkintokokonaisuudet, timeout = 5.minutes)
    runDbSync(RYtrTutkintokerranSuoritukset ++= tutkintokerrat, timeout = 5.minutes)
    runDbSync(RYtrKokeenSuoritukset ++= kokeet, timeout = 5.minutes)
    runDbSync(RYtrTutkintokokonaisuudenKokeenSuoritukset ++= tutkintokokonaisuudenKokeet, timeout = 5.minutes)
  }

  def updateOsasuoritukset(suoritukset: Seq[ROsasuoritusRow]): Unit = {
    val opiskeluoikeusOids = suoritukset.map(_.opiskeluoikeusOid).toSet
    runDbSync(ROsasuoritukset.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).delete, timeout = 10.minutes)
    runDbSync(ROsasuoritukset ++= suoritukset, timeout = 10.minutes)
  }

  def loadMuuAmmatillinenRaportointi(rows: Seq[MuuAmmatillinenOsasuoritusRaportointiRow]): Unit =
    runDbSync(MuuAmmatillinenOsasuoritusRaportointi ++= rows, timeout = 5.minutes)

  def updateMuuAmmatillinenRaportointi(rows: Seq[MuuAmmatillinenOsasuoritusRaportointiRow]): Unit = {
    val opiskeluoikeusOids = rows.map(_.opiskeluoikeusOid).toSet
    runDbSync(MuuAmmatillinenOsasuoritusRaportointi.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).delete, timeout = 5.minutes)
    runDbSync(MuuAmmatillinenOsasuoritusRaportointi ++= rows, timeout = 5.minutes)
  }

  def loadTOPKSAmmatillinenRaportointi(rows: Seq[TOPKSAmmatillinenRaportointiRow]): Unit =
    runDbSync(TOPKSAmmatillinenOsasuoritusRaportointi ++= rows, timeout = 5.minutes)

  def updateTOPKSAmmatillinenRaportointi(rows: Seq[TOPKSAmmatillinenRaportointiRow]): Unit = {
    val opiskeluoikeusOids = rows.map(_.opiskeluoikeudenOid).toSet
    runDbSync(TOPKSAmmatillinenOsasuoritusRaportointi.filter(_.opiskeluoikeudenOid inSet opiskeluoikeusOids).delete, timeout = 5.minutes)
    runDbSync(TOPKSAmmatillinenOsasuoritusRaportointi ++= rows, timeout = 5.minutes)
  }

  def loadHenkilöt(henkilöt: Seq[RHenkilöRow]): Unit =
    runDbSync(RHenkilöt ++= henkilöt)

  def loadKotikuntahistoria(historia: Seq[RKotikuntahistoriaRow]): Unit =
    runDbSync(RKotikuntahistoria ++= historia.filterNot(_.turvakielto))

  def loadOrganisaatiot(organisaatiot: Seq[ROrganisaatioRow]): Unit =
    runDbSync(ROrganisaatiot ++= organisaatiot)

  def loadOrganisaatioKielet(organisaatioKielet: Seq[ROrganisaatioKieliRow]): Unit =
    runDbSync(ROrganisaatioKielet ++= organisaatioKielet)

  def loadKoodistoKoodit(koodit: Seq[RKoodistoKoodiRow]): Unit =
    runDbSync(RKoodistoKoodit ++= koodit)

  def setLastUpdate(name: String, time: LocalDateTime = now): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set last_update=${toTimestamp(time)} where name = $name")

  def setStatusLoadStarted(name: String, dueTime: Option[Timestamp] = None): Unit =
    runDbSync(sqlu"""
      insert into #${schema.name}.raportointikanta_status
        (name, load_started, load_completed, due_time)
        values ($name, now(), null, $dueTime)
      on conflict (name) do update set
        load_started = now(),
        load_completed = null,
        due_time = $dueTime
    """)

  def updateStatusCount(name: String, count: Int): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set count=count + $count, load_completed=now() where name = $name")

  def setStatusLoadCompleted(name: String): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set load_completed=now() where name = $name")

  def setStatusLoadCompletedAndCount(name: String, count: Int): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set count=$count, load_completed=now() where name = $name")

  def status: RaportointikantaStatusResponse =
    RaportointikantaStatusResponse(schema.name, queryStatus)

  def loadOppivelvollisuudenVapautukset(vapautukset: Seq[ROppivelvollisuudestaVapautusRow]): Unit = {
    runDbSync(ROppivelvollisuudestaVapautukset ++= vapautukset)
  }

  private def queryStatus = {
    if (statusTableExists) {
      try {
        runDbSync(RaportointikantaStatus.result)
      } catch {
        case e: PSQLException =>
          logger.debug(s"status unavailable for ${schema.name}, ${e.getMessage.replace("\n", "")}")
          Nil
      }
    } else {
      logger.debug(s"status table does not exist in schema ${schema.name}")
      Nil
    }
  }

  private def statusTableExists: Boolean = {
    val query = sql"SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_schema = ${schema.name} AND table_name = 'raportointikanta_status')"
    runDbSync(query.as[Boolean]).headOption.getOrElse(false)
  }

  def oppilaitoksenKielet(organisaatioOid: Organisaatio.Oid): Set[RKoodistoKoodiRow] = {
    val splitPart = SimpleFunction.ternary[String, String, Int, String]("SPLIT_PART")

    val query = ROrganisaatioKielet
      .filter(_.organisaatioOid === organisaatioOid)
      .join(RKoodistoKoodit)
      .on((kielet, koodit) => {
        splitPart(splitPart(kielet.kielikoodi, "#", 1), "_", 1) === koodit.koodistoUri && splitPart(splitPart(kielet.kielikoodi, "#", 1), "_", 2) === koodit.koodiarvo
      })
    runDbSync(query.result).map {
      case(kielet, koodit) => koodit
    }.toSet
  }

  def oppilaitoksenKoulutusmuodot(oppilaitos: Organisaatio.Oid): Set[String] = {
    val query = ROpiskeluoikeudet.filter(_.oppilaitosOid === oppilaitos).map(_.koulutusmuoto).distinct
    runDbSync(query.result).toSet
  }

  def oppilaitostenKoulutusmuodot(oppilaitosOids: Set[Organisaatio.Oid]): Set[String] = {
    val query = ROpiskeluoikeudet.filter(_.oppilaitosOid inSet oppilaitosOids).map(_.koulutusmuoto).distinct
    runDbSync(query.result).toSet
  }

  def opiskeluoikeusAikajaksot(oppilaitos: Organisaatio.Oid, koulutusmuoto: String, alku: LocalDate, loppu: LocalDate): Seq[(ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow])] = {
    val alkuDate = Date.valueOf(alku)
    val loppuDate = Date.valueOf(loppu)
    val query1 = ROpiskeluoikeudet
      .filter(_.oppilaitosOid === oppilaitos)
      .filter(_.koulutusmuoto === koulutusmuoto)
      .join(ROpiskeluoikeusAikajaksot.filterNot(_.alku > loppuDate).filterNot(_.loppu < alkuDate))
      .on(_.opiskeluoikeusOid === _.opiskeluoikeusOid)
      .sortBy(_._1.opiskeluoikeusOid)
    val result1: Seq[(ROpiskeluoikeusRow, ROpiskeluoikeusAikajaksoRow)] = runDbSync(query1.result, timeout = 5.minutes)

    val päätasonSuorituksetQuery = RPäätasonSuoritukset.filter(_.opiskeluoikeusOid inSet result1.map(_._1.opiskeluoikeusOid).distinct)
    val päätasonSuoritukset: Map[String, Seq[RPäätasonSuoritusRow]] = runDbSync(päätasonSuorituksetQuery.result).groupBy(_.opiskeluoikeusOid)
    val sisältyvätOpiskeluoikeudetQuery = ROpiskeluoikeudet.filter(_.sisältyyOpiskeluoikeuteenOid inSet result1.map(_._1.opiskeluoikeusOid).distinct)
    val sisältyvätOpiskeluoikeudet: Map[String, Seq[ROpiskeluoikeusRow]] = retryDbSync(sisältyvätOpiskeluoikeudetQuery.result).groupBy(_.sisältyyOpiskeluoikeuteenOid.get)

    val henkilötQuery = RHenkilöt.filter(_.oppijaOid inSet result1.map(_._1.oppijaOid))
    val henkilöt: Map[String, RHenkilöRow] = runDbSync(henkilötQuery.result).groupBy(_.oppijaOid).mapValues(_.head)

    // group rows belonging to same opiskeluoikeus
    result1
      .foldRight[List[(ROpiskeluoikeusRow, List[ROpiskeluoikeusAikajaksoRow])]](List.empty) {
        case (t, head :: tail) if t._1.opiskeluoikeusOid == head._1.opiskeluoikeusOid => (head._1, t._2 :: head._2) :: tail
        case (t, acc) => (t._1, List(t._2)) :: acc
      }
      .map(t => (
        t._1,
        henkilöt(t._1.oppijaOid),
        t._2.map(_.truncateToDates(alkuDate, loppuDate)).sortBy(_.alku)(sqlDateOrdering),
        päätasonSuoritukset.getOrElse(t._1.opiskeluoikeusOid, Seq.empty).sortBy(_.päätasonSuoritusId),
        sisältyvätOpiskeluoikeudet.getOrElse(t._1.opiskeluoikeusOid, Seq.empty).sortBy(_.opiskeluoikeusOid)
      ))
  }

  def kotikuntahistoriat(oppijaOids: Seq[Henkilö.Oid]): Seq[RKotikuntahistoriaRow] =
    runDbSync(
      RKotikuntahistoria
        .filter(_.masterOppijaOid inSetBind oppijaOids)
        .result
    )

  lazy val ROpiskeluoikeudet = schema match {
    case Public => TableQuery[ROpiskeluoikeusTable]
    case Temp => TableQuery[ROpiskeluoikeusTableTemp]
    case Confidential => TableQuery[ROpiskeluoikeusConfidentialTable]
    case TempConfidential => TableQuery[ROpiskeluoikeusConfidentialTableTemp]
  }

  lazy val RMitätöidytOpiskeluoikeudet = schema match {
    case Public => TableQuery[RMitätöityOpiskeluoikeusTable]
    case Temp => TableQuery[RMitätöityOpiskeluoikeusTableTemp]
    case Confidential => TableQuery[RMitätöityOpiskeluoikeusConfidentialTable]
    case TempConfidential => TableQuery[RMitätöityOpiskeluoikeusConfidentialTableTemp]
  }

  lazy val ROrganisaatioHistoriat = schema match {
    case Public => TableQuery[ROrganisaatioHistoriaTable]
    case Temp => TableQuery[ROrganisaatioHistoriaTableTemp]
    case Confidential => TableQuery[ROrganisaatioHistoriaConfidentialTable]
    case TempConfidential => TableQuery[ROrganisaatioHistoriaConfidentialTableTemp]
  }

  lazy val ROpiskeluoikeusAikajaksot = schema match {
    case Public => TableQuery[ROpiskeluoikeusAikajaksoTable]
    case Temp => TableQuery[ROpiskeluoikeusAikajaksoTableTemp]
    case Confidential => TableQuery[ROpiskeluoikeusAikajaksoConfidentialTable]
    case TempConfidential => TableQuery[ROpiskeluoikeusAikajaksoConfidentialTableTemp]
  }

  lazy val EsiopetusOpiskeluoikeusAikajaksot = schema match {
    case Public => TableQuery[EsiopetusOpiskeluoikeusAikajaksoTable]
    case Temp => TableQuery[EsiopetusOpiskeluoikeusAikajaksoTableTemp]
    case Confidential => TableQuery[EsiopetusOpiskeluoikeusAikajaksoConfidentialTable]
    case TempConfidential => TableQuery[EsiopetusOpiskeluoikeusAikajaksoConfidentialTableTemp]
  }

  lazy val RPäätasonSuoritukset = schema match {
    case Public => TableQuery[RPäätasonSuoritusTable]
    case Temp => TableQuery[RPäätasonSuoritusTableTemp]
    case Confidential => TableQuery[RPäätasonSuoritusConfidentialTable]
    case TempConfidential => TableQuery[RPäätasonSuoritusConfidentialTableTemp]
  }

  lazy val ROsasuoritukset = schema match {
    case Public => TableQuery[ROsasuoritusTable]
    case Temp => TableQuery[ROsasuoritusTableTemp]
    case Confidential => TableQuery[ROsasuoritusConfidentialTable]
    case TempConfidential => TableQuery[ROsasuoritusConfidentialTableTemp]
  }

  lazy val RHenkilöt = schema match {
    case Public => TableQuery[RHenkilöTable]
    case Temp => TableQuery[RHenkilöTableTemp]
    case Confidential => TableQuery[RHenkilöConfidentialTable]
    case TempConfidential => TableQuery[RHenkilöConfidentialTableTemp]
  }

  lazy val ROrganisaatiot = schema match {
    case Public => TableQuery[ROrganisaatioTable]
    case Temp => TableQuery[ROrganisaatioTableTemp]
    case Confidential => TableQuery[ROrganisaatioConfidentialTable]
    case TempConfidential => TableQuery[ROrganisaatioConfidentialTableTemp]
  }

  lazy val RKoodistoKoodit = schema match {
    case Public => TableQuery[RKoodistoKoodiTable]
    case Temp => TableQuery[RKoodistoKoodiTableTemp]
    case Confidential => TableQuery[RKoodistoKoodiConfidentialTable]
    case TempConfidential => TableQuery[RKoodistoKoodiConfidentialTableTemp]
  }

  lazy val ROrganisaatioKielet = schema match {
    case Public => TableQuery[ROrganisaatioKieliTable]
    case Temp => TableQuery[ROrganisaatioKieliTableTemp]
    case Confidential => TableQuery[ROrganisaatioKieliConfidentialTable]
    case TempConfidential => TableQuery[ROrganisaatioKieliConfidentialTableTemp]
  }

  lazy val RaportointikantaStatus = schema match {
    case Public => TableQuery[RaportointikantaStatusTable]
    case Temp => TableQuery[RaportointikantaStatusTableTemp]
    case Confidential => TableQuery[RaportointikantaStatusConfidentialTable]
    case TempConfidential => TableQuery[RaportointikantaStatusConfidentialTableTemp]
  }

  lazy val MuuAmmatillinenOsasuoritusRaportointi = schema match {
    case Public => TableQuery[MuuAmmatillinenOsasuoritusRaportointiTable]
    case Temp => TableQuery[MuuAmmatillinenOsasuoritusRaportointiTableTemp]
    case Confidential => TableQuery[MuuAmmatillinenOsasuoritusRaportointiConfidentialTable]
    case TempConfidential => TableQuery[MuuAmmatillinenOsasuoritusRaportointiConfidentialTableTemp]
  }

  lazy val TOPKSAmmatillinenOsasuoritusRaportointi = schema match {
    case Public => TableQuery[TOPKSAmmatillinenOsasuoritusRaportointiTable]
    case Temp => TableQuery[TOPKSAmmatillinenOsasuoritusRaportointiTableTemp]
    case Confidential => TableQuery[TOPKSAmmatillinenOsasuoritusRaportointiConfidentialTable]
    case TempConfidential => TableQuery[TOPKSAmmatillinenOsasuoritusRaportointiTableTemp]
  }

  lazy val ROppivelvollisuudestaVapautukset = schema match {
    case Public => TableQuery[ROppivelvollisuudestaVapautusTable]
    case Temp => TableQuery[ROppivelvollisuudestaVapautusTableTemp]
    case Confidential => TableQuery[ROppivelvollisuudestaVapautusConfidentialTable]
    case TempConfidential => TableQuery[ROppivelvollisuudestaVapautusConfidentialTableTemp]
  }

  lazy val RYtrTutkintokokonaisuudenSuoritukset = schema match {
    case Public => TableQuery[RYtrTutkintokokonaisuudenSuoritusTable]
    case Temp => TableQuery[RYtrTutkintokokonaisuudenSuoritusTableTemp]
    case Confidential => TableQuery[RYtrTutkintokokonaisuudenSuoritusConfidentialTable]
    case TempConfidential => TableQuery[RYtrTutkintokokonaisuudenSuoritusConfidentialTableTemp]
  }

  lazy val RYtrTutkintokerranSuoritukset = schema match {
    case Public => TableQuery[RYtrTutkintokerranSuoritusTable]
    case Temp => TableQuery[RYtrTutkintokerranSuoritusTableTemp]
    case Confidential => TableQuery[RYtrTutkintokerranSuoritusConfidentialTable]
    case TempConfidential => TableQuery[RYtrTutkintokerranSuoritusConfidentialTableTemp]
  }

  lazy val RYtrKokeenSuoritukset = schema match {
    case Public => TableQuery[RYtrKokeenSuoritusTable]
    case Temp => TableQuery[RYtrKokeenSuoritusTableTemp]
    case Confidential => TableQuery[RYtrKokeenSuoritusConfidentialTable]
    case TempConfidential => TableQuery[RYtrKokeenSuoritusConfidentialTableTemp]
  }

  lazy val RYtrTutkintokokonaisuudenKokeenSuoritukset = schema match {
    case Public => TableQuery[RYtrTutkintokokonaisuudenKokeenSuoritusTable]
    case Temp => TableQuery[RYtrTutkintokokonaisuudenKokeenSuoritusTableTemp]
    case Confidential => TableQuery[RYtrTutkintokokonaisuudenKokeenSuoritusConfidentialTable]
    case TempConfidential => TableQuery[RYtrTutkintokokonaisuudenKokeenSuoritusConfidentialTableTemp]
  }

  lazy val RKotikuntahistoria = schema match {
    case Public => TableQuery[RKotikuntahistoriaTable]
    case Temp => TableQuery[RKotikuntahistoriaTableTemp]
    case Confidential => TableQuery[RKotikuntahistoriaConfidentialTable]
    case TempConfidential => TableQuery[RKotikuntahistoriaConfidentialTableTemp]
  }
}

case class RaportointikantaStatusResponse(schema: String, statuses: Seq[RaportointikantaStatusRow]) {
  private val allNames = Seq("opiskeluoikeudet", "henkilot", "organisaatiot", "koodistot", "materialized_views", "version_schema", "version_data")

  @SyntheticProperty
  def isComplete: Boolean = completionTime.isDefined && !isEmpty && allNames.forall(statuses.map(_.name).contains)
  @SyntheticProperty
  def isLoading: Boolean = !isComplete && startedTime.isDefined
  @SyntheticProperty
  def isEmpty: Boolean = statuses.isEmpty

  @SyntheticProperty
  def startedTime: Option[Timestamp] =
    statuses.collect { case r if r.loadStarted.isDefined => r.loadStarted.get } match {
      case Nil => None
      case xs => Some(xs.min(ascedingSqlTimestampOrdering))
    }

  @SyntheticProperty
  def completionTime: Option[Timestamp] = {
    val allDates = statuses.collect { case s if allNames.contains(s.name) && s.loadCompleted.isDefined => s.loadCompleted.get }
    if (allDates.length == allNames.length) {
      Some(allDates.max(ascedingSqlTimestampOrdering))
    } else {
      None
    }
  }

  @SyntheticProperty
  def lastUpdate: Option[Timestamp] = if (isEmpty) {
    None
  } else {
    Some(statuses.map(_.lastUpdate).max(ascedingSqlTimestampOrdering))
  }

  @SyntheticProperty
  def schemaVersion: Option[Int] = statuses.find(_.name == "version_schema").map(_.count)

  @SyntheticProperty
  def dataVersion: Option[Int] = statuses.find(_.name == "version_data").map(_.count)
}

class ConfidentialRaportointiDatabase(config: RaportointiDatabaseConfigBase) extends RaportointiDatabase(config) {
  import scala.language.existentials
  override val tables = List(
    RaportointikantaStatus,
    RKotikuntahistoria,
  )

  override def loadKotikuntahistoria(historia: Seq[RKotikuntahistoriaRow]): Unit =
    runDbSync(RKotikuntahistoria ++= historia)
}

object ConfidentialRaportointiDatabase {
  def apply(config: RaportointiDatabaseConfigBase): Option[ConfidentialRaportointiDatabase] =
    config.schema match {
      case Public => Some(new ConfidentialRaportointiDatabase(config.withSchema(Confidential)))
      case Temp => Some(new ConfidentialRaportointiDatabase(config.withSchema(TempConfidential)))
      case _ => None
    }
}
