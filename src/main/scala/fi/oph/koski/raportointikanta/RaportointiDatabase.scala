package fi.oph.koski.raportointikanta

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.{KoskiDatabaseConfig, KoskiDatabaseMethods}
import fi.oph.koski.log.Logging
import slick.driver.PostgresDriver
import slick.dbio.DBIO
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema._

object RaportointiDatabase {
  type DB = PostgresDriver.backend.DatabaseDef
}

class RaportointiDatabase(val config: Config) extends Logging with KoskiDatabaseMethods {
  val db: DB = KoskiDatabaseConfig(config, raportointi = true).toSlickDatabase

  private val ROpiskeluoikeudet = TableQuery[ROpiskeluoikeusTable]
  private val RPäätasonSuoritukset = TableQuery[RPäätasonSuoritusTable]
  private val ROsasuoritukset = TableQuery[ROsasuoritusTable]
  private val RHenkilöt = TableQuery[RHenkilöTable]
  private val ROrganisaatiot = TableQuery[ROrganisaatioTable]
  private val RKoodistoKoodit = TableQuery[RKoodistoKoodiTable]

  def dropAndCreateSchema: Unit = {
    runDbSync(DBIO.seq(
      RaportointiDatabaseSchema.dropAllIfExists,
      ROpiskeluoikeudet.schema.create,
      RPäätasonSuoritukset.schema.create,
      ROsasuoritukset.schema.create,
      RHenkilöt.schema.create,
      ROrganisaatiot.schema.create,
      RKoodistoKoodit.schema.create,
      RaportointiDatabaseSchema.createIndexes
    ))
  }

  def deleteOpiskeluoikeudet: Unit =
    runDbSync(ROpiskeluoikeudet.delete)
  def loadOpiskeluoikeudet(opiskeluoikeudet: Seq[ROpiskeluoikeusRow]): Unit =
    runDbSync(ROpiskeluoikeudet ++= opiskeluoikeudet)
  def oppijaOidsFromOpiskeluoikeudet: Seq[String] =
    runDbSync(ROpiskeluoikeudet.map(_.oppijaOid).distinct.result)

  def deletePäätasonSuoritukset: Unit =
    runDbSync(RPäätasonSuoritukset.delete)
  def loadPäätasonSuoritukset(suoritukset: Seq[RPäätasonSuoritusRow]): Unit =
    runDbSync(RPäätasonSuoritukset ++= suoritukset)
  def deleteOsasuoritukset: Unit =
    runDbSync(ROsasuoritukset.delete)
  def loadOsasuoritukset(suoritukset: Seq[ROsasuoritusRow]): Unit =
    runDbSync(ROsasuoritukset ++= suoritukset)


  def deleteHenkilöt: Unit =
    runDbSync(RHenkilöt.delete)
  def loadHenkilöt(henkilöt: Seq[RHenkilöRow]): Unit =
    runDbSync(RHenkilöt ++= henkilöt)

  def deleteOrganisaatiot: Unit =
    runDbSync(ROrganisaatiot.delete)
  def loadOrganisaatiot(organisaatiot: Seq[ROrganisaatioRow]): Unit =
    runDbSync(ROrganisaatiot ++= organisaatiot)

  def deleteKoodistoKoodit(koodistoUri: String): Unit =
    runDbSync(RKoodistoKoodit.filter(_.koodistoUri === koodistoUri).delete)
  def loadKoodistoKoodit(koodit: Seq[RKoodistoKoodiRow]): Unit =
    runDbSync(RKoodistoKoodit ++= koodit)
}
