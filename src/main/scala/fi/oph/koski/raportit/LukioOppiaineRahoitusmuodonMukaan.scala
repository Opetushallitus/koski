package fi.oph.koski.raportit

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
        select
          r_osasuoritus.opiskeluoikeus_oid,
          r_osasuoritus.koulutusmoduuli_koodiarvo,
          r_osasuoritus.koulutusmoduuli_nimi
        from r_osasuoritus
          join r_paatason_suoritus
            on r_paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
          join r_opiskeluoikeus
            on r_opiskeluoikeus.opiskeluoikeus_oid = r_osasuoritus.opiskeluoikeus_oid
          left join r_opiskeluoikeus_aikajakso
            on r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid = r_osasuoritus.opiskeluoikeus_oid
            and (r_osasuoritus.arviointi_paiva between r_opiskeluoikeus_aikajakso.alku and r_opiskeluoikeus_aikajakso.loppu)
          where r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOids)
            -- rahoitusmuoto
            and r_opiskeluoikeus_aikajakso.opintojen_rahoitus #${rahoitusmuoto match {
              case Some(rahoitusmuoto) => s"= '$rahoitusmuoto'"
              case None => "is null"
            }}
            -- lukion aineoppimäärä
            and r_paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
            and r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
            -- kurssi menee parametrien sisään
            and (r_osasuoritus.arviointi_paiva between $aikaisintaan and $viimeistaan)
            -- suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut pakolliset tai valtakunnalliset syventävät kurssit, joiden arviointipäivä osuu muuta kautta rahoitetun läsnäolojakson sisälle. Kurssien
            and (
                tunnustettu = false
                or tunnustettu_rahoituksen_piirissa
            )
            and (
                koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
                or (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false)
            );
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
