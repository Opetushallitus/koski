package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.SQLHelpers
import slick.dbio.DBIO
import slick.jdbc.SQLActionBuilder

object OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset extends OpiskeluoikeusPrecomputedTable {

  val precomputedTableName = "osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella"

  protected def precomputedTableSelectSql(schemaName: String, opiskeluoikeusRajaus: SQLActionBuilder): SQLActionBuilder =
    SQLHelpers.concat(
      sql"""
        select
          opiskeluoikeus.opiskeluoikeus_oid,
          opiskeluoikeus.oppilaitos_oid,
          osasuoritus.osasuoritus_id,
          osasuoritus.arviointi_paiva osasuorituksen_arviointi_paiva,
          osasuoritus.suorituksen_tyyppi osasuorituksen_tyyppi,
          paatason_suoritus.suorituksen_tyyppi paatason_suorituksen_tyyppi,
          paatason_suoritus.oppimaara_koodiarvo
        from #$schemaName.r_opiskeluoikeus opiskeluoikeus
          join #$schemaName.r_osasuoritus osasuoritus on osasuoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
          join #$schemaName.r_paatason_suoritus paatason_suoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
        where (
            osasuoritus.arviointi_paiva < opiskeluoikeus.alkamispaiva
            or
           (osasuoritus.arviointi_paiva > coalesce(opiskeluoikeus.paattymispaiva, '9999-12-31') and viimeisin_tila = 'valmistunut')
          )
      """,
      opiskeluoikeusRajaus
    )

  def createIndex(s: Schema): DBIO[Unit] =
    DBIO.seq(
      sqlu"create index on #${s.name}.#$precomputedTableName(oppilaitos_oid)",
      sqlu"create index on #${s.name}.#$precomputedTableName(opiskeluoikeus_oid)",
    )
}
