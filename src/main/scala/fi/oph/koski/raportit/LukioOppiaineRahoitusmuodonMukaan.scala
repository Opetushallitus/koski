package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
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
  def createMaterializedView(s: Schema) =
    sqlu"""
      create materialized view #${s.name}.lukion_oppiaineen_oppimaaran_kurssien_rahoitusmuodot as select
        opiskeluoikeus.oppilaitos_oid,
        opiskeluoikeus.opiskeluoikeus_oid,
        opiskeluoikeus.oppija_oid,
        osasuoritus.koulutusmoduuli_koodiarvo,
        osasuoritus.koulutusmoduuli_nimi,
        osasuoritus.arviointi_paiva,
        aikajakso.opintojen_rahoitus
      from #${s.name}.r_paatason_suoritus paatason_suoritus
        join #${s.name}.r_osasuoritus osasuoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
        join #${s.name}.r_opiskeluoikeus opiskeluoikeus on paatason_suoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
        left join #${s.name}.r_opiskeluoikeus_aikajakso aikajakso
          on paatason_suoritus.opiskeluoikeus_oid = aikajakso.opiskeluoikeus_oid
          and (osasuoritus.arviointi_paiva between aikajakso.alku and aikajakso.loppu)
        where
          paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
          and osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and (
            osasuoritus.tunnustettu = false
            or
            tunnustettu_rahoituksen_piirissa
          )
          and (
            osasuoritus.koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
            or
            (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false)
          )
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.lukion_oppiaineen_oppimaaran_kurssien_rahoitusmuodot(oppilaitos_oid)"

  def queryMuutaKauttaRahoitetut(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, rahoitusmuoto: Option[String]) = {
    sql"""
      select
        opiskeluoikeus_oid,
        oppija_oid,
        koulutusmoduuli_koodiarvo,
        koulutusmoduuli_nimi
      from lukion_oppiaineen_oppimaaran_kurssien_rahoitusmuodot
      where oppilaitos_oid = any($oppilaitosOids)
        and (arviointi_paiva between $aikaisintaan and $viimeistaan)
        and opintojen_rahoitus #${rahoitusmuoto match {
          case Some(rahoitusmuoto) => s"= '$rahoitusmuoto'"
          case None => "is null"
        }}
    """.as[LukioKurssinRahoitusmuotoRow]
  }

  implicit private val getResult: GetResult[LukioKurssinRahoitusmuotoRow] = GetResult(r => {
    val rs = r.rs
    LukioKurssinRahoitusmuotoRow(
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

case class LukioKurssinRahoitusmuotoRow(
  opiskeluoikeusOid: String,
  oppijaOid: String,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: String,
)
