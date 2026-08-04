package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet, Lukio2019OppiaineRahoitusmuodonMukaan}
import fi.oph.koski.raportit.lukio.{LukioOppiaineEriVuonnaKorotetutKurssit, LukioOppiaineRahoitusmuodonMukaan}
import slick.dbio.DBIO

trait OpiskeluoikeusPrecomputedTable {
  def precomputedTableName: String

  protected def precomputedTableSelectSql(schemaName: String): String

  def createPrecomputedTable(s: Schema): DBIO[Int] =
    sqlu"create table #${s.name}.#$precomputedTableName as #${precomputedTableSelectSql(s.name)}"

  def createIndex(s: Schema): DBIO[Unit]

  def updatePrecomputedTable(s: Schema, opiskeluoikeusOids: Seq[String]): DBIO[Unit] =
    DBIO.seq(
      sqlu"delete from #${s.name}.#$precomputedTableName where opiskeluoikeus_oid = any($opiskeluoikeusOids)",
      sqlu"""
        insert into #${s.name}.#$precomputedTableName
        select * from ( #${precomputedTableSelectSql(s.name)} ) precomputed
        where precomputed.opiskeluoikeus_oid = any($opiskeluoikeusOids)
      """
    )
}

object OpiskeluoikeusPrecomputedTables {
  val all: Seq[OpiskeluoikeusPrecomputedTable] = Seq(
    OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset,
    LukioOppiaineRahoitusmuodonMukaan,
    Lukio2019OppiaineRahoitusmuodonMukaan,
    LukioOppiaineEriVuonnaKorotetutKurssit,
    Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet,
  )
}
