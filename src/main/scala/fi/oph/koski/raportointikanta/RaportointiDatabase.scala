package fi.oph.koski.raportointikanta

import java.sql.Timestamp.{valueOf => toTimestamp}
import java.sql.{Date, Timestamp}
import java.time.LocalDateTime.now
import java.time._

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{KoskiDatabaseConfig, KoskiDatabaseMethods}
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.{sqlDateOrdering, sqlTimestampOrdering}
import fi.oph.scalaschema.annotation.SyntheticProperty
import org.postgresql.util.PSQLException
import slick.dbio.DBIO
import slick.driver.PostgresDriver

import scala.concurrent.duration._

object RaportointiDatabase {
  type DB = PostgresDriver.backend.DatabaseDef
}

case class RaportointiDatabase(config: KoskiDatabaseConfig) extends Logging with KoskiDatabaseMethods {
  val schema = config.raportointiSchema.get
  logger.info(s"Instantiating RaportointiDatabase for ${schema.name}")

  val db: DB = config.toSlickDatabase
  val tables = List(ROpiskeluoikeudet, ROpiskeluoikeusAikajaksot, RPäätasonSuoritukset, ROsasuoritukset, RHenkilöt, ROrganisaatiot, RKoodistoKoodit, RaportointikantaStatus)

  def moveTo(newSchema: Schema): Unit = {
    logger.info(s"Moving ${schema.name} -> ${newSchema.name}")
    runDbSync(RaportointiDatabaseSchema.moveSchema(schema, newSchema))
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
    ))
    logger.info(s"${schema.name} created")
  }

  def createOpiskeluoikeusIndexes: Unit = {
    runDbSync(RaportointiDatabaseSchema.createOpiskeluoikeusIndexes(schema), timeout = 60.minutes)
  }

  def deleteOpiskeluoikeudet =
    runDbSync(ROpiskeluoikeudet.schema.truncate)

  def loadOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow]): Unit = {
    runDbSync(ROpiskeluoikeudet ++= opiskeluoikeudet)
  }

  def oppijaOidsFromOpiskeluoikeudet: Seq[String] = {
    runDbSync(ROpiskeluoikeudet.map(_.oppijaOid).distinct.result, timeout = 15.minutes)
  }

  def deleteOpiskeluoikeusAikajaksot: Unit =
    runDbSync(ROpiskeluoikeusAikajaksot.schema.truncate)

  def loadOpiskeluoikeusAikajaksot(jaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Unit =
    runDbSync(ROpiskeluoikeusAikajaksot ++= jaksot)

  def deletePäätasonSuoritukset: Unit =
    runDbSync(RPäätasonSuoritukset.schema.truncate)
  def loadPäätasonSuoritukset(suoritukset: Seq[RPäätasonSuoritusRow]): Unit =
    runDbSync(RPäätasonSuoritukset ++= suoritukset)
  def deleteOsasuoritukset: Unit =
    runDbSync(ROsasuoritukset.schema.truncate)
  def loadOsasuoritukset(suoritukset: Seq[ROsasuoritusRow]): Unit =
    runDbSync(ROsasuoritukset ++= suoritukset, timeout = 5.minutes)


  def deleteHenkilöt: Unit =
    runDbSync(RHenkilöt.schema.truncate)
  def loadHenkilöt(henkilöt: Seq[RHenkilöRow]): Unit =
    runDbSync(RHenkilöt ++= henkilöt)

  def deleteOrganisaatiot: Unit =
    runDbSync(ROrganisaatiot.schema.truncate)
  def loadOrganisaatiot(organisaatiot: Seq[ROrganisaatioRow]): Unit =
    runDbSync(ROrganisaatiot ++= organisaatiot)

  def deleteKoodistoKoodit(koodistoUri: String): Unit =
    runDbSync(RKoodistoKoodit.filter(_.koodistoUri === koodistoUri).delete)
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

  private def queryStatus = try {
    runDbSync(RaportointikantaStatus.result)
  } catch {
    case e: PSQLException =>
      logger.debug(s"status unavailable for ${schema.name}, ${e.getMessage.replace("\n", "")}")
      Nil
  }

  def oppilaitoksenKoulutusmuodot(oppilaitos: Organisaatio.Oid): Set[String] = {
    val query = ROpiskeluoikeudet.filter(_.oppilaitosOid === oppilaitos).map(_.koulutusmuoto).distinct
    runDbSync(query.result).toSet
  }

  def oppilaitostenKoulutusmuodot(oppilaitosOids: Set[Organisaatio.Oid]): Set[String] = {
    val query = ROpiskeluoikeudet.filter(_.oppilaitosOid inSet oppilaitosOids).map(_.koulutusmuoto).distinct
    runDbSync(query.result).toSet
  }

  def opiskeluoikeusAikajaksot(oppilaitos: Organisaatio.Oid, koulutusmuoto: String, alku: LocalDate, loppu: LocalDate): Seq[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow])] = {
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
        henkilöt.get(t._1.oppijaOid),
        t._2.map(_.truncateToDates(alkuDate, loppuDate)).sortBy(_.alku)(sqlDateOrdering),
        päätasonSuoritukset.getOrElse(t._1.opiskeluoikeusOid, Seq.empty).sortBy(_.päätasonSuoritusId),
        sisältyvätOpiskeluoikeudet.getOrElse(t._1.opiskeluoikeusOid, Seq.empty).sortBy(_.opiskeluoikeusOid)
      ))
  }

  lazy val ROpiskeluoikeudet = schema match {
    case Public => TableQuery[ROpiskeluoikeusTable]
    case Temp => TableQuery[ROpiskeluoikeusTableTemp]
    case _ => ???
  }

  lazy val ROpiskeluoikeusAikajaksot = schema match {
    case Public => TableQuery[ROpiskeluoikeusAikajaksoTable]
    case Temp => TableQuery[ROpiskeluoikeusAikajaksoTableTemp]
    case _ => ???
  }

  lazy val RPäätasonSuoritukset = schema match {
    case Public => TableQuery[RPäätasonSuoritusTable]
    case Temp => TableQuery[RPäätasonSuoritusTableTemp]
    case _ => ???
  }

  lazy val ROsasuoritukset = schema match {
    case Public => TableQuery[ROsasuoritusTable]
    case Temp => TableQuery[ROsasuoritusTableTemp]
    case _ => ???
  }

  lazy val RHenkilöt = schema match {
    case Public => TableQuery[RHenkilöTable]
    case Temp => TableQuery[RHenkilöTableTemp]
    case _ => ???
  }

  lazy val ROrganisaatiot = schema match {
    case Public => TableQuery[ROrganisaatioTable]
    case Temp => TableQuery[ROrganisaatioTableTemp]
    case _ => ???
  }

  lazy val RKoodistoKoodit = schema match {
    case Public => TableQuery[RKoodistoKoodiTable]
    case Temp => TableQuery[RKoodistoKoodiTableTemp]
    case _ => ???
  }

  lazy val RaportointikantaStatus = schema match {
    case Public => TableQuery[RaportointikantaStatusTable]
    case Temp => TableQuery[RaportointikantaStatusTableTemp]
    case _ => ???
  }
}

case class RaportointikantaStatusResponse(schema: String, statuses: Seq[RaportointikantaStatusRow]) {
  private val allNames = Seq("opiskeluoikeudet", "henkilot", "organisaatiot", "koodistot")

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
      case xs => Some(xs.min(sqlTimestampOrdering))
    }

  @SyntheticProperty
  def completionTime: Option[Timestamp] = {
    val allDates = statuses.collect { case s if allNames.contains(s.name) && s.loadCompleted.isDefined => s.loadCompleted.get }
    if (allDates.length == allNames.length) {
      Some(allDates.max(sqlTimestampOrdering))
    } else {
      None
    }
  }

  @SyntheticProperty
  def lastUpdate: Option[Timestamp] = if (isEmpty) {
    None
  } else {
    Some(statuses.map(_.lastUpdate).max(sqlTimestampOrdering))
  }
}
