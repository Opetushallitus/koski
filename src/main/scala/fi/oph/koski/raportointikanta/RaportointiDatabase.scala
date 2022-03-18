package fi.oph.koski.raportointikanta

import java.sql.Timestamp.{valueOf => toTimestamp}
import java.sql.{Date, Timestamp}
import java.time.LocalDateTime.now
import java.time._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, DatabaseUtilQueries, QueryMethods, RaportointiDatabaseConfig}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.raportit.PaallekkaisetOpiskeluoikeudet
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019AineopintojenOpintopistekertymat, Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet, Lukio2019OppiaineRahoitusmuodonMukaan, Lukio2019OppimaaranOpintopistekertymat}
import fi.oph.koski.raportit.lukio.{LukioOppiaineEriVuonnaKorotetutKurssit, LukioOppiaineRahoitusmuodonMukaan, LukioOppiaineenOppimaaranKurssikertymat, LukioOppimaaranKussikertymat}
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.{ascedingSqlTimestampOrdering, sqlDateOrdering}
import fi.oph.koski.util.Retry
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.scalaschema.annotation.SyntheticProperty
import org.postgresql.util.PSQLException
import slick.dbio.DBIO

import java.sql.Timestamp.{valueOf => toTimestamp}
import java.sql.{Date, Timestamp}
import java.time.LocalDateTime.now
import java.time._
import scala.concurrent.duration.DurationInt


class RaportointiDatabase(config: RaportointiDatabaseConfig) extends Logging with QueryMethods {
  val schema: Schema = config.schema

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
    TOPKSAmmatillinenOsasuoritusRaportointi
  )

  def vacuumAnalyze(): Unit = {
    logger.info("Starting VACUUM ANALYZE in RaportointiDatabase")
    db.run(sqlu"VACUUM ANALYZE")
    logger.info("Finished VACUUM ANALYZE in RaportointiDatabase")
  }

  def moveTo(newSchema: Schema): Unit = {
    logger.info(s"Moving ${schema.name} -> ${newSchema.name}")
    runDbSync(DBIO.seq(
      RaportointiDatabaseSchema.moveSchema(schema, newSchema),
      RaportointiDatabaseSchema.createRolesIfNotExists,
      RaportointiDatabaseSchema.grantPermissions(newSchema)
    ))
  }

  def dropPublicAndMoveTempToPublic: Unit = {
    // Raportointikannan swappaaminen saattaa epäonnistua timeouttiin, jos esim. käyttäjä on juuri ajamassa
    // hidasta raporttia. Parempi yrittää uudestaan, eikä lopettaa monen tunnin operaatiota vain tästä syystä.
    Retry.retryWithInterval(5, 30000) {
      runDbSync(DBIO.seq(
          RaportointiDatabaseSchema.dropSchema(Public),
          RaportointiDatabaseSchema.moveSchema(Temp, Public),
          RaportointiDatabaseSchema.createRolesIfNotExists,
          RaportointiDatabaseSchema.grantPermissions(Public)
        ).transactionally)
      }
    logger.info("RaportointiDatabase schema swapped")
  }

  def dropAndCreateObjects: Unit = {
    logger.info(s"Creating database ${schema.name}")
    runDbSync(DBIO.sequence(
      Seq(RaportointiDatabaseSchema.createSchemaIfNotExists(schema),
      RaportointiDatabaseSchema.dropAllIfExists(schema)) ++
      tables.map(_.schema.create) ++
      Seq(RaportointiDatabaseSchema.createOtherIndexes(schema),
      RaportointiDatabaseSchema.createRolesIfNotExists,
      RaportointiDatabaseSchema.grantPermissions(schema))
    ).transactionally)
    logger.info(s"${schema.name} created")
  }

  // A helper to help with creating migrations: dumps the SQL DDL to create the full schema
  def logCreateSchemaDdl(): Unit = {
    logger.info((tables.flatMap(_.schema.createStatements) ++ "\n").mkString(";\n"))
  }

  def createOpiskeluoikeusIndexes(): Unit = {
    runDbSync(RaportointiDatabaseSchema.createOpiskeluoikeusIndexes(schema), timeout = 120.minutes)
  }

  def createMaterializedViews(valpasRajapäivätService: ValpasRajapäivätService): Unit = {
    logger.info("Creating materialized views")
    val started = System.currentTimeMillis
    setStatusLoadStarted("materialized_views")

    val views = Seq(
      PaallekkaisetOpiskeluoikeudet.createMaterializedView(schema),
      PaallekkaisetOpiskeluoikeudet.createIndex(schema),
      LukioOppimaaranKussikertymat.createMaterializedView(schema),
      LukioOppimaaranKussikertymat.createIndex(schema),
      Lukio2019OppimaaranOpintopistekertymat.createMaterializedView(schema),
      Lukio2019OppimaaranOpintopistekertymat.createIndex(schema),
      OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.createMaterializedView(schema),
      OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.createIndex(schema),
      LukioOppiaineenOppimaaranKurssikertymat.createMaterializedView(schema),
      LukioOppiaineenOppimaaranKurssikertymat.createIndex(schema),
      Lukio2019AineopintojenOpintopistekertymat.createMaterializedView(schema),
      Lukio2019AineopintojenOpintopistekertymat.createIndex(schema),
      LukioOppiaineRahoitusmuodonMukaan.createMaterializedView(schema),
      LukioOppiaineRahoitusmuodonMukaan.createIndex(schema),
      Lukio2019OppiaineRahoitusmuodonMukaan.createMaterializedView(schema),
      Lukio2019OppiaineRahoitusmuodonMukaan.createIndex(schema),
      LukioOppiaineEriVuonnaKorotetutKurssit.createMaterializedView(schema),
      LukioOppiaineEriVuonnaKorotetutKurssit.createIndex(schema),
      Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet.createMaterializedView(schema),
      Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet.createIndex(schema),
      Oppivelvollisuustiedot.createMaterializedView(schema, valpasRajapäivätService),
      Oppivelvollisuustiedot.createIndexes(schema)
    )
    runDbSync(DBIO.seq(views: _*), timeout = 120.minutes)
    setStatusLoadCompletedAndCount("materialized_views", views.length)

    val duration = (System.currentTimeMillis - started) / 1000
    logger.info(s"Materialized views created in $duration s")
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

  def cloneUpdateableTables(source: RaportointiDatabase): Unit = {
    val kloonattavatTaulut = List(
      "r_opiskeluoikeus",
      "r_organisaatiohistoria",
      "esiopetus_opiskeluoik_aikajakso",
      "r_opiskeluoikeus_aikajakso",
      "r_paatason_suoritus",
      "r_osasuoritus",
      "muu_ammatillinen_raportointi",
      "topks_ammatillinen_raportointi",
      "r_mitatoitu_opiskeluoikeus",
    )

    kloonattavatTaulut.foreach { taulu =>
      logger.info(s"Kopioidaan rivit ${source.schema.name}.${taulu} --> ${schema.name}.${taulu}")
      runDbSync(sqlu"""INSERT INTO #${schema.name}.#${taulu} SELECT * FROM #${source.schema.name}.#${taulu}""")
    }
  }

  def updateOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow]): Unit = {
    runDbSync(DBIO.sequence(opiskeluoikeudet.map(ROpiskeluoikeudet.insertOrUpdate)), timeout = 5.minutes)
  }

  def loadMitätöidytOpiskeluoikeudet(rows: Seq[RMitätöityOpiskeluoikeusRow]): Unit = {
    runDbSync(RMitätöidytOpiskeluoikeudet ++= rows, timeout = 5.minutes)
  }

  def updateMitätöidytOpiskeluoikeudet(rows: Seq[RMitätöityOpiskeluoikeusRow]): Unit = {
    runDbSync(DBIO.sequence(rows.map(RMitätöidytOpiskeluoikeudet.insertOrUpdate)), timeout = 5.minutes)
  }

  def oppijaOidsFromOpiskeluoikeudet: Seq[String] = {
    runDbSync(ROpiskeluoikeudet.map(_.oppijaOid).distinct.result, timeout = 15.minutes)
  }

  def loadOrganisaatioHistoria(organisaatioHistoriat: Seq[ROrganisaatioHistoriaRow]): Unit =
    runDbSync(ROrganisaatioHistoriat ++= organisaatioHistoriat)

  def updateOrganisaatioHistoria(organisaatioHistoriat: Seq[ROrganisaatioHistoriaRow]): Unit =
    runDbSync(DBIO.sequence(organisaatioHistoriat.map(ROrganisaatioHistoriat.insertOrUpdate)))

  def loadOpiskeluoikeusAikajaksot(jaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(ROpiskeluoikeusAikajaksot ++= jaksot)

  def updateOpiskeluoikeusAikajaksot(jaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(DBIO.sequence(jaksot.map(ROpiskeluoikeusAikajaksot.insertOrUpdate)))

  def loadEsiopetusOpiskeluoikeusAikajaksot(jaksot: Seq[EsiopetusOpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(EsiopetusOpiskeluoikeusAikajaksot ++= jaksot)

  def updateEsiopetusOpiskeluoikeusAikajaksot(jaksot: Seq[EsiopetusOpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(DBIO.sequence(jaksot.map(EsiopetusOpiskeluoikeusAikajaksot.insertOrUpdate)))

  def loadPäätasonSuoritukset(suoritukset: Seq[RPäätasonSuoritusRow]): Unit =
    runDbSync(RPäätasonSuoritukset ++= suoritukset)

  def updatePäätasonSuoritukset(suoritukset: Seq[RPäätasonSuoritusRow]): Unit =
    runDbSync(DBIO.sequence(suoritukset.map(RPäätasonSuoritukset.insertOrUpdate)))

  def loadOsasuoritukset(suoritukset: Seq[ROsasuoritusRow]): Unit =
    runDbSync(ROsasuoritukset ++= suoritukset, timeout = 5.minutes)

  def updateOsasuoritukset(suoritukset: Seq[ROsasuoritusRow]): Unit =
    runDbSync(DBIO.sequence(suoritukset.map(ROsasuoritukset.insertOrUpdate)))

  def loadMuuAmmatillinenRaportointi(rows: Seq[MuuAmmatillinenOsasuoritusRaportointiRow]): Unit =
    runDbSync(MuuAmmatillinenOsasuoritusRaportointi ++= rows, timeout = 5.minutes)

  def updateMuuAmmatillinenRaportointi(rows: Seq[MuuAmmatillinenOsasuoritusRaportointiRow]): Unit =
    runDbSync(DBIO.sequence(rows.map(MuuAmmatillinenOsasuoritusRaportointi.insertOrUpdate)))

  def loadTOPKSAmmatillinenRaportointi(rows: Seq[TOPKSAmmatillinenRaportointiRow]): Unit =
    runDbSync(TOPKSAmmatillinenOsasuoritusRaportointi ++= rows, timeout = 5.minutes)

  def updateTOPKSAmmatillinenRaportointi(rows: Seq[TOPKSAmmatillinenRaportointiRow]): Unit =
    runDbSync(DBIO.sequence(rows.map(TOPKSAmmatillinenOsasuoritusRaportointi.insertOrUpdate)))

  def loadHenkilöt(henkilöt: Seq[RHenkilöRow]): Unit =
    runDbSync(RHenkilöt ++= henkilöt)

  def loadOrganisaatiot(organisaatiot: Seq[ROrganisaatioRow]): Unit =
    runDbSync(ROrganisaatiot ++= organisaatiot)

  def loadOrganisaatioKielet(organisaatioKielet: Seq[ROrganisaatioKieliRow]): Unit =
    runDbSync(ROrganisaatioKielet ++= organisaatioKielet)

  def loadKoodistoKoodit(koodit: Seq[RKoodistoKoodiRow]): Unit =
    runDbSync(RKoodistoKoodit ++= koodit)

  def setLastUpdate(name: String, time: LocalDateTime = now): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set last_update=${toTimestamp(time)} where name = $name")

  def setStatusLoadStarted(name: String): Unit =
    runDbSync(sqlu"insert into #${schema.name}.raportointikanta_status (name, load_started, load_completed) values ($name, now(), null) on conflict (name) do update set load_started = now(), load_completed = null")

  def updateStatusCount(name: String, count: Int): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set count=count + $count, load_completed=now() where name = $name")

  def setStatusLoadCompleted(name: String): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set load_completed=now() where name = $name")

  def setStatusLoadCompletedAndCount(name: String, count: Int): Unit =
    runDbSync(sqlu"update #${schema.name}.raportointikanta_status set count=$count, load_completed=now() where name = $name")

  def status: RaportointikantaStatusResponse =
    RaportointikantaStatusResponse(schema.name, queryStatus)

  def latestOpiskeluoikeusTimestamp: Option[Timestamp] =
    try {
      runDbSync(sql"""SELECT max(aikaleima) FROM #${schema.name}.r_opiskeluoikeus""".as(_.rs.getTimestamp(1)))
        .headOption match {
          case Some(null) => None // Tyhjä taulu
          case r: Any => r
        }
    } catch {
      case e: PSQLException if e.getMessage.contains("does not exist") => None // Taulua ei ole vielä luotu
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
    val sisältyvätOpiskeluoikeudet: Map[String, Seq[ROpiskeluoikeusRow]] = runDbSync(sisältyvätOpiskeluoikeudetQuery.result).groupBy(_.sisältyyOpiskeluoikeuteenOid.get)

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

  lazy val ROpiskeluoikeudet = schema match {
    case Public => TableQuery[ROpiskeluoikeusTable]
    case Temp => TableQuery[ROpiskeluoikeusTableTemp]
  }

  lazy val RMitätöidytOpiskeluoikeudet = schema match {
    case Public => TableQuery[RMitätöityOpiskeluoikeusTable]
    case Temp => TableQuery[RMitätöityOpiskeluoikeusTableTemp]
  }

  lazy val ROrganisaatioHistoriat = schema match {
    case Public => TableQuery[ROrganisaatioHistoriaTable]
    case Temp => TableQuery[ROrganisaatioHistoriaTableTemp]
  }

  lazy val ROpiskeluoikeusAikajaksot = schema match {
    case Public => TableQuery[ROpiskeluoikeusAikajaksoTable]
    case Temp => TableQuery[ROpiskeluoikeusAikajaksoTableTemp]
  }

  lazy val EsiopetusOpiskeluoikeusAikajaksot = schema match {
    case Public => TableQuery[EsiopetusOpiskeluoikeusAikajaksoTable]
    case Temp => TableQuery[EsiopetusOpiskeluoikeusAikajaksoTableTemp]
  }

  lazy val RPäätasonSuoritukset = schema match {
    case Public => TableQuery[RPäätasonSuoritusTable]
    case Temp => TableQuery[RPäätasonSuoritusTableTemp]
  }

  lazy val ROsasuoritukset = schema match {
    case Public => TableQuery[ROsasuoritusTable]
    case Temp => TableQuery[ROsasuoritusTableTemp]
  }

  lazy val RHenkilöt = schema match {
    case Public => TableQuery[RHenkilöTable]
    case Temp => TableQuery[RHenkilöTableTemp]
  }

  lazy val ROrganisaatiot = schema match {
    case Public => TableQuery[ROrganisaatioTable]
    case Temp => TableQuery[ROrganisaatioTableTemp]
  }

  lazy val RKoodistoKoodit = schema match {
    case Public => TableQuery[RKoodistoKoodiTable]
    case Temp => TableQuery[RKoodistoKoodiTableTemp]
  }

  lazy val ROrganisaatioKielet = schema match {
    case Public => TableQuery[ROrganisaatioKieliTable]
    case Temp => TableQuery[ROrganisaatioKieliTableTemp]
  }

  lazy val RaportointikantaStatus = schema match {
    case Public => TableQuery[RaportointikantaStatusTable]
    case Temp => TableQuery[RaportointikantaStatusTableTemp]
  }

  lazy val MuuAmmatillinenOsasuoritusRaportointi = schema match {
    case Public => TableQuery[MuuAmmatillinenOsasuoritusRaportointiTable]
    case Temp => TableQuery[MuuAmmatillinenOsasuoritusRaportointiTableTemp]
  }

  lazy val TOPKSAmmatillinenOsasuoritusRaportointi = schema match {
    case Public => TableQuery[TOPKSAmmatillinenOsasuoritusRaportointiTable]
    case Temp => TableQuery[TOPKSAmmatillinenOsasuoritusRaportointiTableTemp]
  }
}

case class RaportointikantaStatusResponse(schema: String, statuses: Seq[RaportointikantaStatusRow]) {
  private val allNames = Seq("opiskeluoikeudet", "henkilot", "organisaatiot", "koodistot", "materialized_views")

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
}
