package fi.oph.koski.valpas.valpasrepository

import com.typesafe.config.Config
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.db.ValpasDatabase
import fi.oph.koski.valpas.db.ValpasSchema.{OppivelvollisuudenKeskeytys, OppivelvollisuudenKeskeytysRow}

class OppivelvollisuudenKeskeytysRepository(database: ValpasDatabase, config: Config) extends QueryMethods with Logging {
  protected val db: DB = database.db

  def getKeskeytykset(oppijaOids: Seq[String]): Seq[OppivelvollisuudenKeskeytysRow] = {
    runDbSync(
      OppivelvollisuudenKeskeytys
        .filter(_.oppijaOid inSetBind oppijaOids)
        .sortBy(_.luotu.desc)
        .result
    )
  }

  def setKeskeytys(row: OppivelvollisuudenKeskeytysRow): Unit = {
    runDbSync(
      OppivelvollisuudenKeskeytys.insertOrUpdate(row)
    )
  }

  def truncate(): Unit = {
    if (config.getString("opintopolku.virkailija.url") == "mock") {
      runDbSync(OppivelvollisuudenKeskeytys.delete)
    } else {
      throw new RuntimeException("Oppivelvollisuuden keskeytyksiä ei voi tyhjentää tuotantotilassa")
    }
  }}
