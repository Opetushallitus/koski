package fi.oph.koski.raportit.lukio.lops2021

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet extends DatabaseConverters {

  def dataSheet(
    oppilaitosOids: List[String],
    jaksonAlku: LocalDate,
    jaksonLoppu: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader
  ): DataSheet = {
    DataSheet(
      t.get("raportti-excel-erivuonnakorotetutopintopisteet-sheet-name"),
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, jaksonAlku, jaksonLoppu)),
      columnSettings(t)
    )
  }

  def createPrecomputedTable(s: Schema) =
    sqlu"""
      create table #${s.name}.lukion_aineopintojen_eri_vuonna_korotetut as select
        opiskeluoikeus.oppilaitos_oid,
        opiskeluoikeus.opiskeluoikeus_oid,
        opiskeluoikeus.oppija_oid,
        osasuoritus.koulutusmoduuli_koodiarvo,
        osasuoritus.koulutusmoduuli_nimi,
        osasuoritus.arviointi_paiva,
        osasuoritus.korotettu_eri_vuonna
      from #${s.name}.r_paatason_suoritus paatason_suoritus
        join #${s.name}.r_osasuoritus osasuoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
        join #${s.name}.r_opiskeluoikeus opiskeluoikeus on paatason_suoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
        join #${s.name}.r_opiskeluoikeus_aikajakso aikajakso on paatason_suoritus.opiskeluoikeus_oid = aikajakso.opiskeluoikeus_oid
        where paatason_suoritus.suorituksen_tyyppi = 'lukionaineopinnot'
          and (osasuoritus.arviointi_paiva between aikajakso.alku and aikajakso.loppu)
          and osasuoritus.suorituksen_tyyppi in ('lukionvaltakunnallinenmoduuli', 'lukionpaikallinenopintojakso')
          and osasuoritus.arviointi_arvosana_koodiarvo != 'O'
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.lukion_aineopintojen_eri_vuonna_korotetut(oppilaitos_oid)"

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate) = {
    sql"""
      select
        opiskeluoikeus_oid,
        oppija_oid,
        koulutusmoduuli_koodiarvo,
        koulutusmoduuli_nimi
      from lukion_aineopintojen_eri_vuonna_korotetut
      where oppilaitos_oid = any($oppilaitosOids)
        and (arviointi_paiva between $aikaisintaan and $viimeistaan)
        and korotettu_eri_vuonna = true
      """.as[Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow]
  }

  implicit private val getResult: GetResult[Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow(
      opiskeluoikeusOid = rs.getString("opiskeluoikeus_oid"),
      oppijaOid = rs.getString("oppija_oid"),
      koulutusmoduuliKoodiarvo = rs.getString("koulutusmoduuli_koodiarvo"),
      koulutusmoduuliNimi = rs.getString("koulutusmoduuli_nimi")
    )
  })

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "koulutusmoduuliKoodiarvo" -> Column(t.get("raportti-excel-kolumni-moduulikoodi")),
    "koulutusmoduuliNimi" -> Column(t.get("raportti-excel-kolumni-moduulinNimi")),
  )
}

case class Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow(
  opiskeluoikeusOid: String,
  oppijaOid: String,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: String,
)
