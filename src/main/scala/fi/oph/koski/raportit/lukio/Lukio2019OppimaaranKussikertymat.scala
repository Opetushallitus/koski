package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object Lukio2019OppimaaranOpintopistekertymat extends DatabaseConverters {

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
      create materialized view #${s.name}.lukion_oppimaaran_opintopistekertyma as select
        oppilaitos_oid,
        osasuoritus.arviointi_paiva,
        sum(osasuoritus.koulutusmoduuli_laajuus_arvo) filter (where tunnustettu = false) suoritettuja,
        sum(osasuoritus.koulutusmoduuli_laajuus_arvo) filter (where tunnustettu) tunnustettuja,
        sum(osasuoritus.koulutusmoduuli_laajuus_arvo) yhteensa,
        sum(osasuoritus.koulutusmoduuli_laajuus_arvo) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa
      from #${s.name}.r_osasuoritus osasuoritus
        join #${s.name}.r_paatason_suoritus paatason_suoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
        join #${s.name}.r_opiskeluoikeus opiskeluoikeus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
      where paatason_suoritus.suorituksen_tyyppi = 'lukionoppimaara'
        and osasuoritus.suorituksen_tyyppi in ('lukionvaltakunnallinenmoduuli', 'lukionpaikallinenopintojakso')
        and osasuoritus.arviointi_arvosana_koodiarvo != 'O'
      group by opiskeluoikeus.oppilaitos_oid, osasuoritus.arviointi_paiva
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.lukion_oppimaaran_opintopistekertyma(oppilaitos_oid, arviointi_paiva)"

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, lang: String) = {
    val nimiSarake = if(lang == "sv") "nimi_sv" else "nimi"
    sql"""
      select
        #$nimiSarake oppilaitos,
        opintopistekertyma.*
      from (
        select
          oppilaitos_oid,
          sum(suoritettuja) suoritettuja,
          sum(tunnustettuja) tunnustettuja,
          sum(yhteensa) yhteensa,
          sum(tunnustettuja_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa
        from lukion_oppimaaran_opintopistekertyma
          where oppilaitos_oid = any($oppilaitosOids)
            and (arviointi_paiva between $aikaisintaan and $viimeistaan)
          group by oppilaitos_oid
      ) opintopistekertyma
      join r_organisaatio on organisaatio_oid = oppilaitos_oid
    """.as[LukioOpintopistekertymaOppimaaraRow]
  }

  implicit private val getResult: GetResult[LukioOpintopistekertymaOppimaaraRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioOpintopistekertymaOppimaaraRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitos = rs.getString("oppilaitos"),
      suoritettujaOpintopisteita = rs.getInt("suoritettuja"),
      tunnustettujaOpintopisteita = rs.getInt("tunnustettuja"),
      opintopisteitaYhteensa = rs.getInt("yhteensa"),
      tunnustettujaOpintopisteita_rahoituksenPiirissa = rs.getInt(("tunnustettuja_rahoituksen_piirissa"))
    )
  })

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column(t.get("raportti-excel-kolumni-oppilaitosOid")),
    "oppilaitos" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "opintopisteitaYhteensa" -> Column(t.get("raportti-excel-kolumni-opintopisteitaYhteensa"), comment = Some(t.get("raportti-excel-kolumni-opintopisteitaYhteensa-comment"))),
    "suoritettujaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-yhteensäSuoritettujaOpintopisteitä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäSuoritettujaOpintopisteitä-lukio-comment"))),
    "tunnustettujaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitä-lukio-comment"))),
    "tunnustettujaOpintopisteita_rahoituksenPiirissa" -> Column(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitäRahoituksenPiirissä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitäRahoituksenPiirissä-lukio-comment")))
  )
}

case class LukioOpintopistekertymaOppimaaraRow(
  oppilaitosOid: String,
  oppilaitos: String,
  opintopisteitaYhteensa: Int,
  suoritettujaOpintopisteita: Int,
  tunnustettujaOpintopisteita: Int,
  tunnustettujaOpintopisteita_rahoituksenPiirissa: Int
)
