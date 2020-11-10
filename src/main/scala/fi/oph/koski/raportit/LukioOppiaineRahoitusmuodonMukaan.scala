package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult

object LukioMuutaKauttaRahoitetut {
  val sheetTitle = "Muuta kautta rah."

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(LukioOppiaineRahoitusmuodonMukaan.queryMuutaKauttaRahoitetut(
        oppilaitosOids,
        jaksonAlku,
        jaksonLoppu,
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
        jaksonAlku,
        jaksonLoppu,
        None)),
      LukioOppiaineRahoitusmuodonMukaan.columnSettings
    )
  }
}

object LukioOppiaineRahoitusmuodonMukaan extends DatabaseConverters {
  def queryMuutaKauttaRahoitetut(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, rahoitusmuoto: Option[String]) = {
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
            oppilaitos_oid = any($oppilaitosOids)
            and r_paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
            and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
            and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
            and r_opiskeluoikeus_aikajakso.opintojen_rahoitus #${rahoitusmuoto match {
              case Some(rahoitusmuoto) => s"= '$rahoitusmuoto'"
              case None => "is null"
            }}
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
    """.as[LukioKurssinRahoitusmuotoRow]
  }

  implicit private val getResult: GetResult[LukioKurssinRahoitusmuotoRow] = GetResult(r => {
    val rs = r.rs
    LukioKurssinRahoitusmuotoRow(
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

case class LukioKurssinRahoitusmuotoRow(
  opiskeluoikeusOid: String,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: String,
)
