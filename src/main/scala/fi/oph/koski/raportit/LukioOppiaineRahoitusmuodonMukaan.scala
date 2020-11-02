package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.util.SQL
import slick.jdbc.GetResult

object LukioMuutaKauttaRahoitetut {
  val sheetTitle = "Muuta kautta rah."

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(LukioOppiaineRahoitusmuodonMukaan.queryMuutaKauttaRahoitetut(
        oppilaitosOids,
        SQL.toSqlDate(jaksonAlku),
        SQL.toSqlDate(jaksonLoppu),
        Some("6"))),
      LukioOppiaineRahoitusmuodonMukaan.columnSettings
    )
  }
}

object LukioRahoitusmuotoEiTiedossa {
  val sheetTitle = "Ei rahoitusmuotoa"

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(LukioOppiaineRahoitusmuodonMukaan.queryMuutaKauttaRahoitetut(
        oppilaitosOids,
        SQL.toSqlDate(jaksonAlku),
        SQL.toSqlDate(jaksonLoppu),
        None)),
      LukioOppiaineRahoitusmuodonMukaan.columnSettings
    )
  }
}

object LukioOppiaineRahoitusmuodonMukaan {
  def queryMuutaKauttaRahoitetut(oppilaitosOids: List[String], aikaisintaan: Date, viimeistaan: Date, rahoitusmuoto: Option[String]) = {
    sql"""
      with muuta_kautta_rahoitetut_lasna_jaksot as (
          select
            paatason_suoritus_id,
            r_opiskeluoikeus_aikajakso.alku,
            r_opiskeluoikeus_aikajakso.loppu
          from r_opiskeluoikeus
            join r_opiskeluoikeus_aikajakso on r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
            join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
          where
            oppilaitos_oid in (#${SQL.toSqlListUnsafe(oppilaitosOids)})
            and r_paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
            and r_opiskeluoikeus_aikajakso.tila = 'lasna'
            and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
            and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
            and r_opiskeluoikeus_aikajakso.opintojen_rahoitus #${SQL.toNullableEqUnsafe(rahoitusmuoto)}
      ) select
          r_osasuoritus.opiskeluoikeus_oid,
          koulutusmoduuli_koodiarvo,
          koulutusmoduuli_nimi
        from muuta_kautta_rahoitetut_lasna_jaksot
          join r_osasuoritus
            on r_osasuoritus.paatason_suoritus_id = muuta_kautta_rahoitetut_lasna_jaksot.paatason_suoritus_id
               and r_osasuoritus.arviointi_paiva >= muuta_kautta_rahoitetut_lasna_jaksot.alku
               and r_osasuoritus.arviointi_paiva <= muuta_kautta_rahoitetut_lasna_jaksot.loppu
        where koulutusmoduuli_paikallinen = false
              and (tunnustettu = false or tunnustettu_rahoituksen_piirissa)
              and suorituksen_tyyppi = 'lukionkurssi';
    """.as[MuutaKauttaRahoitetutRow]
  }

  implicit private val getResult: GetResult[MuutaKauttaRahoitetutRow] = GetResult(r => {
    val rs = r.rs
    MuutaKauttaRahoitetutRow(
      opiskeluoikeusOid = rs.getString("opiskeluoikeus_oid"),
      koulutusmoduuliKoodiarvo = rs.getString("koulutusmoduuli_koodiarvo"),
      koulutusmoduuliNimi = rs.getString("koulutusmoduuli_nimi")
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "koulutusmoduuliKoodiarvo" -> Column("Kurssikoodi"),
    "koulutusmoduuliNimi" -> Column("Kurssin nimi"),
  )
}

case class MuutaKauttaRahoitetutRow(
  opiskeluoikeusOid: String,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: String,
)
