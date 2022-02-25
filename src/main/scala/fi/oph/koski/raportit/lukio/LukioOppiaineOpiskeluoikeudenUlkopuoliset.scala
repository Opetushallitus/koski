package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object LukioOppiaineOpiskeluoikeudenUlkopuoliset extends DatabaseConverters {

  def dataSheet(
    oppilaitosOids: List[String],
    jaksonAlku: LocalDate,
    jaksonLoppu: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader
  ): DataSheet = {
    DataSheet(
      t.get("raportti-excel-opiskeluoikeudenulkop-sheet-name"),
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, jaksonAlku, jaksonLoppu)),
      columnSettings(t)
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

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "kurssikoodi" -> Column(t.get("raportti-excel-kolumni-kurssikoodi")),
    "kurssinNimi" -> Column(t.get("raportti-excel-kolumni-kurssinNimi")),
  )
}

case class LukioOppiaineOpiskeluoikeudenUlkopuolisetRow(
  opiskeluoikeusOid: String,
  oppijaOid: String,
  kurssikoodi: String,
  kurssinNimi: String
)
