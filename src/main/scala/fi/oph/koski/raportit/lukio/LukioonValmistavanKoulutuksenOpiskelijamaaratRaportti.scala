package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat
import fi.oph.koski.raportit.{Column, CompactColumn, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object LukioonValmistavanKoulutuksenOpiskelijamaaratRaportti extends DatabaseConverters {

  def dataSheet(
    oppilaitosOid: List[String],
    päivä: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader
  ) = {
    DataSheet(
      t.get("raportti-excel-opiskelijamäärä-sheet-name"),
      rows = raportointiDatabase.runDbSync(query(oppilaitosOid, päivä, t.language)),
      columnSettings(t)
    )
  }

  private def query(oppilaitosOid: List[String], paiva: LocalDate, lang: String) = {
    val oppilaitosNimiSarake = if(lang == "sv") "oppilaitos_nimi_sv" else "oppilaitos_nimi"
    sql"""
      with oppija as (
        select
          r_opiskeluoikeus.oppilaitos_oid,
          r_opiskeluoikeus.#$oppilaitosNimiSarake as oppilaitos_nimi,
          r_opiskeluoikeus_aikajakso.opintojen_rahoitus,
          r_opiskeluoikeus_aikajakso.sisaoppilaitosmainen_majoitus,
          r_henkilo.kotikunta,
          r_paatason_suoritus.oppimaara_koodiarvo
        from r_opiskeluoikeus
        join r_opiskeluoikeus_aikajakso on r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
        join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        where r_opiskeluoikeus.koulutusmuoto = 'luva'
          and r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOid)
          and r_paatason_suoritus.suorituksen_tyyppi = 'luva'
          and r_opiskeluoikeus_aikajakso.tila = 'lasna'
          and r_opiskeluoikeus_aikajakso.alku <= $paiva
          and r_opiskeluoikeus_aikajakso.loppu >= $paiva
      ) select
          oppilaitos_oid oppilaitos_oid,
          oppilaitos_nimi oppilaitos,
          count(*) filter (where opintojen_rahoitus = '1') valtionosuus_rahoitteinen,
          count(*) filter (where opintojen_rahoitus = '6') muuta_kautta_rahoitettu,
          count(*) filter (where sisaoppilaitosmainen_majoitus and opintojen_rahoitus = '1') sisaoppilaitosmainen_majoitus_valtionosuus_rahoitteinen,
          count(*) opiskelijoiden_maara,
          count(*) filter (where oppimaara_koodiarvo = 'nuortenops') opiskelijoiden_maara_nuortenops,
          count(*) filter (where oppimaara_koodiarvo = 'nuortenops' and opintojen_rahoitus = '1') nuorten_valtionosuus_rahoitteinen,
          count(*) filter (where oppimaara_koodiarvo = 'nuortenops' and opintojen_rahoitus = '6') nuorten_muuta_kautta_rahoitettu,
          count(*) filter (where oppimaara_koodiarvo = 'nuortenops' and kotikunta isnull) nuorten_ei_kotikuntaa,
          count(*) filter (where oppimaara_koodiarvo = 'nuortenops' and kotikunta = any($ahvenanmaanKunnat)) nuorten_kotikunta_ahvenanmaa,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops') opiskelijoiden_maara_aikuistenops,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and opintojen_rahoitus = '1') aikuisten_valtionosuus_rahoitteinen,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and opintojen_rahoitus = '6') aikuisten_muuta_kautta_rahoitettu,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and kotikunta isnull) aikuisten_ei_kotikuntaa,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and kotikunta = any($ahvenanmaanKunnat)) aikuisten_kotikunta_ahvenanmaa
      from oppija
      group by oppilaitos_oid, oppilaitos_nimi;
      """.as[LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow]
  }

  implicit private val getResult: GetResult[LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitos = rs.getString("oppilaitos"),
      opiskelijoidenMaara = rs.getInt("opiskelijoiden_maara"),
      opiskelijoidenMaara_VOSRahoitteisia = rs.getInt("valtionosuus_rahoitteinen"),
      opiskelijoidenMaara_MuutaKauttaRahoitettu = rs.getInt("muuta_kautta_rahoitettu"),
      opiskelijoidenMaara_SisaoppilaitosmainenMajoitus_VOSRahoitteisia = rs.getInt("sisaoppilaitosmainen_majoitus_valtionosuus_rahoitteinen"),
      nuortenOppimaaranSuorittajia = rs.getInt("opiskelijoiden_maara_nuortenops"),
      nuortenOppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("nuorten_valtionosuus_rahoitteinen"),
      nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("nuorten_muuta_kautta_rahoitettu"),
      nuortenOppimaaranSuorittajia_EiKotikuntaa = rs.getInt("nuorten_ei_kotikuntaa"),
      nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa = rs.getInt("nuorten_kotikunta_ahvenanmaa"),
      aikuistenOppimaaranSuorittajia = rs.getInt("opiskelijoiden_maara_aikuistenops"),
      aikuistenOppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("aikuisten_valtionosuus_rahoitteinen"),
      aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("aikuisten_muuta_kautta_rahoitettu"),
      aikuistenOppimaaranSuorittajia_EiKotikuntaa = rs.getInt("aikuisten_ei_kotikuntaa"),
      aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa = rs.getInt("aikuisten_kotikunta_ahvenanmaa")
    )
  })

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column(t.get("raportti-excel-kolumni-oppilaitosOid")),
    "oppilaitos" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "opiskelijoidenMaara" -> CompactColumn(t.get("raportti-excel-kolumni-opiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-opiskelijoita-lukio-comment"))),
    "opiskelijoidenMaara_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-oppilaidenMääräVOS"), comment = Some(t.get("raportti-excel-kolumni-oppilaidenMääräVOS-comment"))),
    "opiskelijoidenMaara_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-oppilaidenMääräMuuKuinVOS"), comment = Some(t.get("raportti-excel-kolumni-oppilaidenMääräMuuKuinVOS-comment"))),
    "opiskelijoidenMaara_SisaoppilaitosmainenMajoitus_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-opiskelijoidenMaaraSisaoppilaitosmainenVOS"), comment = Some(t.get("raportti-excel-kolumni-opiskelijoidenMaaraSisaoppilaitosmainenVOS-comment"))),
    "nuortenOppimaaranSuorittajia" -> CompactColumn(t.get("raportti-excel-kolumni-luvaNuortenOps"), comment = Some(t.get("raportti-excel-kolumni-luvaNuortenOps-comment"))),
    "nuortenOppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-luvaNuortenOpsVOS"), comment = Some(t.get("raportti-excel-kolumni-luvaNuortenOpsVOS-comment"))),
    "nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-luvaNuortenOpsMuutaKauttaRahoitettu"), comment = Some(t.get("raportti-excel-kolumni-luvaNuortenOpsMuutaKauttaRahoitettu-comment"))),
    "nuortenOppimaaranSuorittajia_EiKotikuntaa" -> CompactColumn(t.get("raportti-excel-kolumni-luvaNuortenOpsEiKotikuntaa"), comment = Some(t.get("raportti-excel-kolumni-luvaNuortenOpsEiKotikuntaa-comment"))),
    "nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> CompactColumn(t.get("raportti-excel-kolumni-luvaNuortenOpsKotikuntaAhvenanmaa"), comment = Some(t.get("raportti-excel-kolumni-luvaNuortenOpsKotikuntaAhvenanmaa-comment"))),
    "aikuistenOppimaaranSuorittajia" -> CompactColumn(t.get("raportti-excel-kolumni-luvaAikuistenOps"), comment = Some(t.get("raportti-excel-kolumni-luvaAikuistenOps-comment"))),
    "aikuistenOppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-luvaAikuistenOpsVOS"), comment = Some(t.get("raportti-excel-kolumni-luvaAikuistenOpsVOS-comment"))),
    "aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-luvaAikuistenOpsMuutaKauttaRahoitetut"), comment = Some(t.get("raportti-excel-kolumni-luvaAikuistenOpsMuutaKauttaRahoitetut-comment"))),
    "aikuistenOppimaaranSuorittajia_EiKotikuntaa" -> CompactColumn(t.get("raportti-excel-kolumni-luvaAikuistenOpsEiKotikuntaa"), comment = Some(t.get("raportti-excel-kolumni-luvaAikuistenOpsEiKotikuntaa-comment"))),
    "aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> CompactColumn(t.get("raportti-excel-kolumni-luvaAikuistenOpsKotikuntaAhvenanmaa"), comment = Some(t.get("raportti-excel-kolumni-luvaAikuistenOpsKotikuntaAhvenanmaa-comment")))
  )
}

case class LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow(
  oppilaitosOid: String,
  oppilaitos: String,
  opiskelijoidenMaara: Int,
  opiskelijoidenMaara_VOSRahoitteisia: Int,
  opiskelijoidenMaara_MuutaKauttaRahoitettu: Int,
  opiskelijoidenMaara_SisaoppilaitosmainenMajoitus_VOSRahoitteisia: Int,
  nuortenOppimaaranSuorittajia: Int,
  nuortenOppimaaranSuorittajia_VOSRahoitteisia: Int,
  nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  nuortenOppimaaranSuorittajia_EiKotikuntaa: Int,
  nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa: Int,
  aikuistenOppimaaranSuorittajia: Int,
  aikuistenOppimaaranSuorittajia_VOSRahoitteisia: Int,
  aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  aikuistenOppimaaranSuorittajia_EiKotikuntaa: Int,
  aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa: Int
)
