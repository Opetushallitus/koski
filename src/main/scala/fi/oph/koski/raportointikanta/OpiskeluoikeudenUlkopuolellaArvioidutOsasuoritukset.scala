package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

object OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset {

  def createPrecomputedTable(s: Schema) =
    sqlu"""
      create table #${s.name}.osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella as select
        opiskeluoikeus.opiskeluoikeus_oid,
        opiskeluoikeus.oppilaitos_oid,
        osasuoritus.osasuoritus_id,
        osasuoritus.arviointi_paiva osasuorituksen_arviointi_paiva,
        osasuoritus.suorituksen_tyyppi osasuorituksen_tyyppi,
        paatason_suoritus.suorituksen_tyyppi paatason_suorituksen_tyyppi
      from #${s.name}.r_opiskeluoikeus opiskeluoikeus
        join #${s.name}.r_osasuoritus osasuoritus on osasuoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
          and (
            osasuoritus.arviointi_paiva < opiskeluoikeus.alkamispaiva
            or
           (osasuoritus.arviointi_paiva > coalesce(opiskeluoikeus.paattymispaiva, '9999-12-31') and viimeisin_tila = 'valmistunut')
          )
        join #${s.name}.r_paatason_suoritus paatason_suoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella(oppilaitos_oid)"
}
