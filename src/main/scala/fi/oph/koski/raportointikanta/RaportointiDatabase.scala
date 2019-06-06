package fi.oph.koski.raportointikanta

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.{KoskiDatabaseConfig, KoskiDatabaseMethods}
import fi.oph.koski.log.Logging
import scala.concurrent.duration._
import slick.driver.PostgresDriver
import slick.dbio.DBIO
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema._
import java.sql.{Date, Timestamp}
import java.time.LocalDate
import fi.oph.koski.util.DateOrdering.{sqlDateOrdering, sqlTimestampOrdering}
import fi.oph.koski.schema.Organisaatio

object RaportointiDatabase {
  type DB = PostgresDriver.backend.DatabaseDef
}

class RaportointiDatabase(val config: Config) extends Logging with KoskiDatabaseMethods {
  val db: DB = KoskiDatabaseConfig(config, raportointi = true).toSlickDatabase

  private[raportointikanta] val ROpiskeluoikeudet = TableQuery[ROpiskeluoikeusTable]
  private[raportointikanta] val ROpiskeluoikeusAikajaksot = TableQuery[ROpiskeluoikeusAikajaksoTable]
  private[raportointikanta] val RPäätasonSuoritukset = TableQuery[RPäätasonSuoritusTable]
  private[raportointikanta] val ROsasuoritukset = TableQuery[ROsasuoritusTable]
  private[raportointikanta] val RHenkilöt = TableQuery[RHenkilöTable]
  private[raportointikanta] val ROrganisaatiot = TableQuery[ROrganisaatioTable]
  private[raportointikanta] val RKoodistoKoodit = TableQuery[RKoodistoKoodiTable]
  private[raportointikanta] val RaportointikantaStatus = TableQuery[RaportointikantaStatusTable]

  def dropAndCreateSchema: Unit = {
    runDbSync(DBIO.seq(
      RaportointiDatabaseSchema.dropAllIfExists,
      ROpiskeluoikeudet.schema.create,
      ROpiskeluoikeusAikajaksot.schema.create,
      RPäätasonSuoritukset.schema.create,
      ROsasuoritukset.schema.create,
      RHenkilöt.schema.create,
      ROrganisaatiot.schema.create,
      RKoodistoKoodit.schema.create,
      RaportointikantaStatus.schema.create,
      RaportointiDatabaseSchema.createOtherIndexes,
      RaportointiDatabaseSchema.createRolesIfNotExists,
      RaportointiDatabaseSchema.grantPermissions
    ))
  }

  def createOpiskeluoikeusIndexes: Unit = {
    runDbSync(RaportointiDatabaseSchema.createOpiskeluoikeusIndexes, timeout = 60.minutes)
  }

  def deleteOpiskeluoikeudet: Unit =
    runDbSync(ROpiskeluoikeudet.schema.truncate)
  def loadOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow]): Unit =
    runDbSync(ROpiskeluoikeudet ++= opiskeluoikeudet)
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

  def setStatusLoadStarted(name: String): Unit =
    runDbSync(sqlu"insert into raportointikanta_status (name, load_started, load_completed) values ($name, now(), null) on conflict (name) do update set load_started = now(), load_completed = null")
  def setStatusLoadCompleted(name: String): Unit =
    runDbSync(sqlu"update raportointikanta_status set load_completed=now() where name = $name")
  def statuses: Seq[RaportointikantaStatusRow] =
    runDbSync(RaportointikantaStatus.result)
  def fullLoadCompleted(statuses: Seq[RaportointikantaStatusRow]): Option[Timestamp] = {
    val AllNames = Seq("opiskeluoikeudet", "henkilot", "organisaatiot", "koodistot")
    val allDates = statuses.collect { case s if AllNames.contains(s.name) && s.loadCompleted.nonEmpty => s.loadCompleted.get }
    if (allDates.length == AllNames.length) {
      Some(allDates.max(sqlTimestampOrdering))
    } else {
      None
    }
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
}
