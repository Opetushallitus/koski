package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import slick.jdbc.GetResult

import java.time.LocalDate

object LukioMuutaKauttaRahoitetut {

  def dataSheet(
    oppilaitosOids: List[String],
    jaksonAlku: LocalDate,
    jaksonLoppu: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader
  ): DataSheet = {
    DataSheet(
      t.get("raportti-excel-muutakauttarah-sheet-name"),
      rows = raportointiDatabase.runDbSync(LukioOppiaineRahoitusmuodonMukaan.queryMuutaKauttaRahoitetut(
        oppilaitosOids,
        jaksonAlku,
        jaksonLoppu,
        Some("6"),
        t.language
      )),
      LukioOppiaineRahoitusmuodonMukaan.columnSettings(t)
    )
  }
}

object LukioRahoitusmuotoEiTiedossa {

  def dataSheet(
    oppilaitosOids: List[String],
    jaksonAlku: LocalDate,
    jaksonLoppu: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader
  ): DataSheet = {
    DataSheet(
      t.get("raportti-excel-eirahoitusmuotoa-sheet-name"),
      rows = raportointiDatabase.runDbSync(LukioOppiaineRahoitusmuodonMukaan.queryMuutaKauttaRahoitetut(
        oppilaitosOids,
        jaksonAlku,
        jaksonLoppu,
        None,
        t.language
      )),
      LukioOppiaineRahoitusmuodonMukaan.columnSettings(t)
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
        COALESCE(osasuoritus.data -> 'koulutusmoduuli' -> 'tunniste' -> 'nimi' ->> 'sv', osasuoritus.koulutusmoduuli_nimi) as koulutusmoduuli_nimi_sv,
        osasuoritus.arviointi_paiva,
        aikajakso.opintojen_rahoitus
      from #${s.name}.r_paatason_suoritus paatason_suoritus
        join #${s.name}.r_osasuoritus osasuoritus on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
        join #${s.name}.r_opiskeluoikeus opiskeluoikeus on paatason_suoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
        join #${s.name}.r_opiskeluoikeus_aikajakso aikajakso on paatason_suoritus.opiskeluoikeus_oid = aikajakso.opiskeluoikeus_oid
        where paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
          and (osasuoritus.arviointi_paiva between aikajakso.alku and aikajakso.loppu)
          and osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and osasuoritus.arviointi_arvosana_koodiarvo != 'O'
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

  def queryMuutaKauttaRahoitetut(
    oppilaitosOids: List[String],
    aikaisintaan: LocalDate,
    viimeistaan: LocalDate,
    rahoitusmuoto: Option[String],
    lang: String
  ) = {
    val nimiSarake = if(lang == "sv") "koulutusmoduuli_nimi_sv" else "koulutusmoduuli_nimi"
    sql"""
      select
        opiskeluoikeus_oid,
        oppija_oid,
        koulutusmoduuli_koodiarvo,
        #$nimiSarake as koulutusmoduuli_nimi
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

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "koulutusmoduuliKoodiarvo" -> Column(t.get("raportti-excel-kolumni-kurssikoodi")),
    "koulutusmoduuliNimi" -> Column(t.get("raportti-excel-kolumni-kurssinNimi")),
  )
}

case class LukioKurssinRahoitusmuotoRow(
  opiskeluoikeusOid: String,
  oppijaOid: String,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: String,
)
