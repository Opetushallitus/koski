package fi.oph.koski.raportit

import java.sql.ResultSet
import java.time.LocalDate

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult

object LukioOppimaaranKussikertymat extends DatabaseConverters {
  val sheetTitle = "Oppimaara"

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, jaksonAlku, jaksonLoppu)),
      columnSettings
    )
  }

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate) = {
    sql"""
          with paatason_suoritus as (
            select
              r_opiskeluoikeus.oppilaitos_nimi,
              r_opiskeluoikeus.oppilaitos_oid,
              r_paatason_suoritus.paatason_suoritus_id
            from r_opiskeluoikeus
            join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
            where
              oppilaitos_oid = any($oppilaitosOids)
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
          oppilaitos_oid,
          count(*) filter (where tunnustettu = false) suoritettuja,
          count(*) filter (where tunnustettu) tunnustettuja,
          count(*) yhteensa,
          count(*) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa
        from paatason_suoritus
        join r_osasuoritus on paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
        where r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
        group by paatason_suoritus.oppilaitos_nimi, oppilaitos_oid;
    """.as[LukioKurssikertymaOppimaaraRow]
  }

  implicit private val getResult: GetResult[LukioKurssikertymaOppimaaraRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioKurssikertymaOppimaaraRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitos = rs.getString("oppilaitos"),
      suoritettujaKursseja = rs.getInt("suoritettuja"),
      tunnustettujaKursseja = rs.getInt("tunnustettuja"),
      kurssejaYhteensa = rs.getInt("yhteensa"),
      tunnustettujaKursseja_rahoituksenPiirissa = rs.getInt(("tunnustettuja_rahoituksen_piirissa"))
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column("Oppilaitoksen oid-tunniste"),
    "oppilaitos" -> Column("Oppilaitos"),
    "kurssejaYhteensa" -> Column("Kursseja yhteensä", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "suoritettujaKursseja" -> Column("Suoritetut kurssit", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "tunnustettujaKursseja" -> Column("Tunnustetut kurssit", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "tunnustettujaKursseja_rahoituksenPiirissa" -> Column("Tunnustetut kurssit - rahoituksen piirissä", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi."))
  )
}

case class LukioKurssikertymaOppimaaraRow(
  oppilaitosOid: String,
  oppilaitos: String,
  kurssejaYhteensa: Int,
  suoritettujaKursseja: Int,
  tunnustettujaKursseja: Int,
  tunnustettujaKursseja_rahoituksenPiirissa: Int
)
