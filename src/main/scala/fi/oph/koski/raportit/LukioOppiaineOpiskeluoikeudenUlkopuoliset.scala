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
        oppija_oid,
        r_osasuoritus.opiskeluoikeus_oid,
        r_osasuoritus.koulutusmoduuli_koodiarvo as kurssikoodi,
        r_osasuoritus.koulutusmoduuli_nimi as kurssin_nimi
      from osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella
      join r_opiskeluoikeus on r_opiskeluoikeus.opiskeluoikeus_oid = osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella.opiskeluoikeus_oid
      join r_osasuoritus on r_osasuoritus.osasuoritus_id = osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella.osasuoritus_id
        where osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella.oppilaitos_oid = any($oppilaitosOids)
          and osasuorituksen_tyyppi = 'lukionkurssi'
          and paatason_suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
          and (osasuorituksen_arviointi_paiva between $aikaisintaan and $viimeistaan)
          and (
            koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
            or (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false)
          )
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
      oppijaOid = rs.getString("oppija_oid"),
      kurssikoodi = rs.getString("kurssikoodi"),
      kurssinNimi = rs.getString("kurssin_nimi")
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "oppijaOid" -> Column("Oppijan oid"),
    "kurssikoodi" -> Column("Kurssikoodi"),
    "kurssinNimi" -> Column("Kurssin nimi")
  )
}

case class LukioOppiaineOpiskeluoikeudenUlkopuolisetRow(
  opiskeluoikeusOid: String,
  oppijaOid: String,
  kurssikoodi: String,
  kurssinNimi: String
)
