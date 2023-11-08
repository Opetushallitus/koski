package fi.oph.koski.kela

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods, SQLHelpers}
import fi.oph.koski.history.RawOpiskeluoikeusData
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import slick.jdbc.GetResult

class KelaOpiskeluoikeusRepository(
  val db: DB,
  val validatingAndResolvingExtractor: ValidatingAndResolvingExtractor
) extends QueryMethods {
  def getOppijanKaikkiOpiskeluoikeudet(
    oppijaMasterOids: Seq[String]
  ): Seq[RawOpiskeluoikeusData] = {

    runDbSync(SQLHelpers.concatMany(Some(
      sql"""
with
  haettu_oppija as (
    select
      oid as oppija_oid,
      coalesce(master_oid, oid) as oppija_master_oid
    from henkilo
    where henkilo.oid = any($oppijaMasterOids) or henkilo.master_oid = any($oppijaMasterOids)
  )
  , linkitetty as (
    select
      distinct haettu_oppija.oppija_master_oid
    from
      haettu_oppija
      inner join haettu_oppija h2 on h2.oppija_master_oid = haettu_oppija.oppija_master_oid and h2.oppija_master_oid <> h2.oppija_oid
  )
  , opiskeluoikeus_kaikki as (
    select
      haettu_oppija.oppija_master_oid,
      oid as opiskeluoikeus_oid,
      versionumero,
      aikaleima,
      data
    from
      opiskeluoikeus
      join haettu_oppija on haettu_oppija.oppija_oid = opiskeluoikeus.oppija_oid
      left join linkitetty on linkitetty.oppija_master_oid = haettu_oppija.oppija_master_oid
    where mitatoity = false
    order by oppija_master_oid
  )
  , opiskeluoikeus_palautettavat as (
    select
      distinct on (opiskeluoikeus_kaikki.opiskeluoikeus_oid) opiskeluoikeus_kaikki.opiskeluoikeus_oid "opiskeluoikeusOid",
      opiskeluoikeus_kaikki.oppija_master_oid as "masterOppijaOid",
      opiskeluoikeus_kaikki.versionumero,
      opiskeluoikeus_kaikki.aikaleima,
      opiskeluoikeus_kaikki.data,
      opiskeluoikeus_kaikki.opiskeluoikeus_oid
    from
      opiskeluoikeus_kaikki
      inner join opiskeluoikeus_kaikki o2 on o2.oppija_master_oid = opiskeluoikeus_kaikki.oppija_master_oid
  )
select
  "opiskeluoikeusOid",
  "masterOppijaOid",
  versionumero,
  aikaleima,
  data
from opiskeluoikeus_palautettavat
order by "masterOppijaOid", opiskeluoikeus_oid
    """)).as[RawOpiskeluoikeusData])
  }

  private implicit def getOppijanOpiskeluoikeusRow: GetResult[RawOpiskeluoikeusData] = GetResult(r => {
    RawOpiskeluoikeusData(
      data = r.getJson("data"),
      oid = r.rs.getString("opiskeluoikeusOid"),
      versionumero = r.rs.getInt("versionumero"),
      aikaleima = r.rs.getTimestamp("aikaleima")
    )
  })
}
