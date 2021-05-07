package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object LukioOppiaineEriVuonnaKorotetutKurssit extends DatabaseConverters {
  val sheetTitle = "Eri vuonna korotetut kurssit"

  def dataSheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(queryOppimaara(oppilaitosOids, jaksonAlku, jaksonLoppu)),
      columnSettings
    )
  }

  def createMaterializedView(s: Schema) =
    sqlu"""
      create materialized view #${s.name}.lukion_oppiaineen_oppimaaran_eri_vuonna_korotetut as select
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
        where paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
          and (osasuoritus.arviointi_paiva between aikajakso.alku and aikajakso.loppu)
          and osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and osasuoritus.arviointi_arvosana_koodiarvo != 'O'
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.lukion_oppiaineen_oppimaaran_eri_vuonna_korotetut(oppilaitos_oid)"

  def queryOppimaara(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate) = {
    sql"""
      select
        opiskeluoikeus_oid,
        oppija_oid,
        koulutusmoduuli_koodiarvo,
        koulutusmoduuli_nimi
      from lukion_oppiaineen_oppimaaran_eri_vuonna_korotetut
      where oppilaitos_oid = any($oppilaitosOids)
        and (arviointi_paiva between $aikaisintaan and $viimeistaan)
        and korotettu_eri_vuonna = true
      """.as[LukioOppiaineEriVuonnaKorotetutKurssitRow]
  }

  implicit private val getResult: GetResult[LukioOppiaineEriVuonnaKorotetutKurssitRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioOppiaineEriVuonnaKorotetutKurssitRow(
      opiskeluoikeusOid = rs.getString("opiskeluoikeus_oid"),
      oppijaOid = rs.getString("oppija_oid"),
      koulutusmoduuliKoodiarvo = rs.getString("koulutusmoduuli_koodiarvo"),
      koulutusmoduuliNimi = rs.getString("koulutusmoduuli_nimi")
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "oppijaOid" -> Column("Oppijan oid"),
    "koulutusmoduuliKoodiarvo" -> Column("Kurssikoodi"),
    "koulutusmoduuliNimi" -> Column("Kurssin nimi"),
  )
}

case class LukioOppiaineEriVuonnaKorotetutKurssitRow(
  opiskeluoikeusOid: String,
  oppijaOid: String,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: String,
)
