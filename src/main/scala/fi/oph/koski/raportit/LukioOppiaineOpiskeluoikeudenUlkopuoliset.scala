package fi.oph.koski.raportit

import java.sql.ResultSet
import java.time.LocalDate

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult

object LukioOppiaineOpiskeluoikeudenUlkopuoliset extends DatabaseConverters {
  val sheetTitle = "Opiskeluoikeuden ulkop."

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, jaksonAlku, jaksonLoppu)),
      columnSettings
    )
  }

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate) = {
    sql"""
      select
        r_opiskeluoikeus.opiskeluoikeus_oid,
        r_osasuoritus.koulutusmoduuli_koodiarvo as kurssikoodi,
        r_osasuoritus.koulutusmoduuli_nimi as kurssin_nimi
      from r_osasuoritus
      join r_paatason_suoritus
        on r_paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
      join r_opiskeluoikeus
        on r_opiskeluoikeus.opiskeluoikeus_oid = r_osasuoritus.opiskeluoikeus_oid
      where r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOids)
        -- lukion aineoppimäärä
        and r_paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
        and r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
        -- kurssi menee parametrien sisään
        and r_osasuoritus.arviointi_paiva between $aikaisintaan and $viimeistaan
        -- mutta kurssi jää opiskeluoikeuden ulkopuolelle
        and (
          r_osasuoritus.arviointi_paiva < r_opiskeluoikeus.alkamispaiva
          or (
            r_osasuoritus.arviointi_paiva > r_opiskeluoikeus.paattymispaiva
            and r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
          )
        )
        -- pakolliset tai valtakunnalliset syventävät kurssit
        and (
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
          or (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false)
        )
        -- jotka ovat joko suoritettuja tai tunnustettuja ja rahoituksen piirissä olevia
        and (
            tunnustettu = false
            or tunnustettu_rahoituksen_piirissa
        )
      """.as[LukioOppiaineOpiskeluoikeudenUlkopuolisetRow]
  }

  implicit private val getResult: GetResult[LukioOppiaineOpiskeluoikeudenUlkopuolisetRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioOppiaineOpiskeluoikeudenUlkopuolisetRow(
      opiskeluoikeusOid = rs.getString("opiskeluoikeus_oid"),
      kurssikoodi = rs.getString("kurssikoodi"),
      kurssinNimi = rs.getString("kurssin_nimi")
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "kurssikoodi" -> Column("Kurssikoodi"),
    "kurssinNimi" -> Column("Kurssin nimi")
  )
}

case class LukioOppiaineOpiskeluoikeudenUlkopuolisetRow(
  opiskeluoikeusOid: String,
  kurssikoodi: String,
  kurssinNimi: String
)
