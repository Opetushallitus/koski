package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object LukioOppimaaranKussikertymat extends DatabaseConverters {

  def dataSheet(
    oppilaitosOids: List[String],
    jaksonAlku: LocalDate,
    jaksonLoppu: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader
  ): DataSheet = {
    DataSheet(
      t.get("raportti-excel-oppimäärä-sheet-name"),
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, jaksonAlku, jaksonLoppu, t.language)),
      columnSettings(t)
    )
  }

  def createMaterializedView(s: Schema) =
    sqlu"""
      create materialized view #${s.name}.lukion_oppimaaran_kurssikertyma as select
        oppilaitos_oid,
        osasuoritus.arviointi_paiva,
        count(*) filter (where tunnustettu = false) suoritettuja,
        count(*) filter (where tunnustettu) tunnustettuja,
        count(*) yhteensa,
        count(*) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa
      from #${s.name}.r_osasuoritus osasuoritus
        join #${s.name}.r_paatason_suoritus paatason_suoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
        join #${s.name}.r_opiskeluoikeus opiskeluoikeus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
      where paatason_suoritus.suorituksen_tyyppi = 'lukionoppimaara'
        and osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
        and osasuoritus.arviointi_arvosana_koodiarvo != 'O'
      group by opiskeluoikeus.oppilaitos_oid, osasuoritus.arviointi_paiva
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.lukion_oppimaaran_kurssikertyma(oppilaitos_oid, arviointi_paiva)"

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, lang: String) = {
    val nimiSarake = if(lang == "sv") "nimi_sv" else "nimi"
    sql"""
      select
        #$nimiSarake oppilaitos,
        kurssikertyma.*
      from (
        select
          oppilaitos_oid,
          sum(suoritettuja) suoritettuja,
          sum(tunnustettuja) tunnustettuja,
          sum(yhteensa) yhteensa,
          sum(tunnustettuja_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa
        from lukion_oppimaaran_kurssikertyma
          where oppilaitos_oid = any($oppilaitosOids)
            and (arviointi_paiva between $aikaisintaan and $viimeistaan)
          group by oppilaitos_oid
      ) kurssikertyma
      join r_organisaatio on organisaatio_oid = oppilaitos_oid
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

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column(t.get("raportti-excel-kolumni-oppilaitosOid")),
    "oppilaitos" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "kurssejaYhteensa" -> Column(t.get("raportti-excel-kolumni-kurssejaYhteensa"), comment = Some(t.get("raportti-excel-kolumni-kurssejaYhteensa-comment"))),
    "suoritettujaKursseja" -> Column(t.get("raportti-excel-kolumni-yhteensäSuoritettujaSuorituksia"), comment = Some(t.get("raportti-excel-kolumni-yhteensäSuoritettujaSuorituksia-lukio-comment"))),
    "tunnustettujaKursseja" -> Column(t.get("raportti-excel-kolumni-yhteensäTunnistettujaSuorituksia"), comment = Some(t.get("raportti-excel-kolumni-yhteensäTunnistettujaSuorituksia-lukio-comment"))),
    "tunnustettujaKursseja_rahoituksenPiirissa" -> Column(t.get("raportti-excel-kolumni-yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä-lukio-comment")))
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
