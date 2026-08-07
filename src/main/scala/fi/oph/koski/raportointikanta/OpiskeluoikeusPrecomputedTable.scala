package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.SQLHelpers
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet, Lukio2019OppiaineRahoitusmuodonMukaan}
import fi.oph.koski.raportit.lukio.{LukioOppiaineEriVuonnaKorotetutKurssit, LukioOppiaineRahoitusmuodonMukaan}
import slick.dbio.DBIO
import slick.jdbc.SQLActionBuilder

trait OpiskeluoikeusPrecomputedTable {
  def precomputedTableName: String

  protected def precomputedTableSelectSql(schemaName: String, opiskeluoikeusRajaus: SQLActionBuilder): SQLActionBuilder

  def createPrecomputedTable(s: Schema): DBIO[Int] =
    SQLHelpers.concat(
      sql"create table #${s.name}.#$precomputedTableName as ",
      precomputedTableSelectSql(s.name, sql"")
    ).asUpdate

  def createIndex(s: Schema): DBIO[Unit]

  def updatePrecomputedTable(s: Schema, opiskeluoikeusOids: Seq[String]): DBIO[Unit] =
    DBIO.seq(
      sqlu"delete from #${s.name}.#$precomputedTableName where opiskeluoikeus_oid = any($opiskeluoikeusOids)",
      SQLHelpers.concat(
        sql"insert into #${s.name}.#$precomputedTableName ",
        precomputedTableSelectSql(s.name, sql" and opiskeluoikeus.opiskeluoikeus_oid = any($opiskeluoikeusOids)")
      ).asUpdate
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
