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
import fi.oph.koski.util.Futures

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
      RaportointiDatabaseSchema.createOtherIndexes
    ))
  }
  def createOpiskeluoikeusIndexes: Unit = {
    // plain "runDbSync" times out after 1 minute, which is too short here
    Futures.await(db.run(RaportointiDatabaseSchema.createOpiskeluoikeusIndexes), atMost = 15.minutes)
  }

  def deleteOpiskeluoikeudet: Unit =
    runDbSync(ROpiskeluoikeudet.schema.truncate)
  def loadOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow]): Unit =
    runDbSync(ROpiskeluoikeudet ++= opiskeluoikeudet)
  def oppijaOidsFromOpiskeluoikeudet: Seq[String] = {
    // plain "runDbSync" times out after 1 minute, which is too short here
    Futures.await(db.run(ROpiskeluoikeudet.map(_.oppijaOid).distinct.result), atMost = 15.minutes)
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
    runDbSync(ROsasuoritukset ++= suoritukset)


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
}
