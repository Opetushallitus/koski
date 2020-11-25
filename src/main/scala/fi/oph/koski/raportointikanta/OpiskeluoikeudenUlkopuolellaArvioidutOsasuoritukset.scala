package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

object OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset {

  def createMaterializedView =
    sqlu"""
      create materialized view osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella as select
        r_opiskeluoikeus.opiskeluoikeus_oid,
        r_opiskeluoikeus.oppilaitos_oid,
        r_osasuoritus.osasuoritus_id,
        r_osasuoritus.arviointi_paiva osasuorituksen_arviointi_paiva,
        r_osasuoritus.suorituksen_tyyppi osasuorituksen_tyyppi,
        r_paatason_suoritus.suorituksen_tyyppi paatason_suorituksen_tyyppi
      from r_opiskeluoikeus
        join r_osasuoritus on r_osasuoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          and (
            r_osasuoritus.arviointi_paiva < r_opiskeluoikeus.alkamispaiva
            or
           (r_osasuoritus.arviointi_paiva > coalesce(r_opiskeluoikeus.paattymispaiva, '9999-12-31') and viimeisin_tila = 'valmistunut')
          )
        join r_paatason_suoritus on r_paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
    """

  def createIndex =
    sqlu"create index on osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella(oppilaitos_oid)"
}
