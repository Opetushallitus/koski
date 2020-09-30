package fi.oph.koski.raportit

import java.sql.{Date, ResultSet}
import java.time.LocalDate

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.util.SQL
import slick.jdbc.GetResult

object LukioOppimaaranKussikertymat {
  val sheetTitle = "Oppimaara"

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, SQL.toSqlDate(jaksonAlku), SQL.toSqlDate(jaksonLoppu))),
      columnSettings
    )
  }

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: Date, viimeistaan: Date) = {
    sql"""
          with paatason_suoritus as (
            select
              r_opiskeluoikeus.oppilaitos_nimi,
              r_paatason_suoritus.paatason_suoritus_id
            from r_opiskeluoikeus
            join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
            where
              oppilaitos_oid in (#${SQL.toSqlListUnsafe(oppilaitosOids)})
              and r_paatason_suoritus.suorituksen_tyyppi = 'lukionoppimaara'
              and exists (
                select 1 from r_opiskeluoikeus_aikajakso
                where r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
                  and r_opiskeluoikeus_aikajakso.tila = 'lasna'
                  and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
                  and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
              )
      ) select
          oppilaitos_nimi oppilaitos,
          count(*) filter (where tunnustettu = false) suoritettuja,
          count(*) filter (where tunnustettu) tunnustettuja,
          count(*) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa
        from paatason_suoritus
        join r_osasuoritus on paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
        where r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
        group by paatason_suoritus.oppilaitos_nimi;
    """.as[LukioKurssikertymaOppimaaraRow]
  }

  implicit private val getResult: GetResult[LukioKurssikertymaOppimaaraRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioKurssikertymaOppimaaraRow(
      oppilaitos = rs.getString("oppilaitos"),
      suoritettujaKursseja = rs.getInt("suoritettuja"),
      tunnustettujaKursseja = rs.getInt("tunnustettuja"),
      tunnustettujaKursseja_rahoituksenPiirissa = rs.getInt(("tunnustettuja_rahoituksen_piirissa"))
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitos" -> Column("Oppilaitos"),
    "suoritettujaKursseja" -> Column("suoritettujaKursseja"),
    "tunnustettujaKursseja" -> Column("tunnustettujaKursseja"),
    "tunnustettujaKursseja_rahoituksenPiirissa" -> Column("tunnustettujaKursseja_rahoituksenPiirissa")
  )
}

case class LukioKurssikertymaOppimaaraRow(
  oppilaitos: String,
  suoritettujaKursseja: Int,
  tunnustettujaKursseja: Int,
  tunnustettujaKursseja_rahoituksenPiirissa: Int
)
