package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log.Logging

class VipunenExport(val db: DB, schema: Schema) extends Logging with QueryMethods {
  def createAndPopulateHenkilöTable: Int = {
    logger.info(s"Create and populate ${schema.name}.r_henkilo_vipunen")
    runDbSync(sqlu"""DROP TABLE IF EXISTS #${schema.name}.r_henkilo_vipunen""")
    runDbSync(sqlu"""
      CREATE TABLE #${schema.name}.r_henkilo_vipunen
      AS SELECT
        oppija_oid,
        master_oid,
        linkitetyt_oidit,
        sukupuoli,
        syntymaaika,
        aidinkieli,
        kansalaisuus,
        turvakielto,
        kotikunta,
        kotikunta_nimi_fi,
        kotikunta_nimi_sv,
        yksiloity
      FROM #${schema.name}.r_henkilo
    """)
  }
}
