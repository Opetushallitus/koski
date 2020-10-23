package fi.oph.koski.raportit

import java.sql.{Date, ResultSet}
import java.time.LocalDate

import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import slick.jdbc.GetResult
import fi.oph.koski.util.SQL
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat

object LukioonValmistavanKoulutuksenOpiskelijamaaratRaportti {

  def dataSheet(oppilaitosOid: List[String], paiva: LocalDate, raportointiDatabase: RaportointiDatabase) = {
    DataSheet(
      "Opiskelijamaarat",
      rows = raportointiDatabase.runDbSync(query(oppilaitosOid, SQL.toSqlDate(paiva))),
      columnSettings
    )
  }

  private def query(oppilaitosOid: List[String], paiva: Date) = {
    sql"""
      with oppija as (
        select
          r_opiskeluoikeus.oppilaitos_oid,
          r_opiskeluoikeus.oppilaitos_nimi,
          r_opiskeluoikeus_aikajakso.opintojen_rahoitus,
          r_opiskeluoikeus_aikajakso.sisaoppilaitosmainen_majoitus,
          r_henkilo.kotikunta,
          r_paatason_suoritus.oppimaara_koodiarvo
        from r_opiskeluoikeus
        join r_opiskeluoikeus_aikajakso on r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
        join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        where r_opiskeluoikeus.koulutusmuoto = 'luva'
          and r_opiskeluoikeus.oppilaitos_oid in (#${SQL.toSqlListUnsafe(oppilaitosOid)})
          and r_paatason_suoritus.suorituksen_tyyppi = 'luva'
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
          count(*) filter (where oppimaara_koodiarvo = 'nuortenops' and kotikunta in (#${SQL.toSqlListUnsafe(ahvenanmaanKunnat)})) nuorten_kotikunta_ahvenanmaa,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops') opiskelijoiden_maara_aikuistenops,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and opintojen_rahoitus = '1') aikuisten_valtionosuus_rahoitteinen,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and opintojen_rahoitus = '6') aikuisten_muuta_kautta_rahoitettu,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and kotikunta isnull) aikuisten_ei_kotikuntaa,
          count(*) filter (where oppimaara_koodiarvo = 'aikuistenops' and kotikunta in (#${SQL.toSqlListUnsafe(ahvenanmaanKunnat)})) aikuisten_kotikunta_ahvenanmaa
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

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column("Oppilaitoksen oid-tunniste"),
    "oppilaitos" -> Column("oppilaitos"),
    "opiskelijoidenMaara" -> Column("opiskelijoidenMaara"),
    "opiskelijoidenMaara_VOSRahoitteisia" -> Column("opiskelijoidenMaara_VOSRahoitteisia"),
    "opiskelijoidenMaara_MuutaKauttaRahoitettu" -> Column("opiskelijoidenMaara_MuutaKauttaRahoitettu"),
    "opiskelijoidenMaara_SisaoppilaitosmainenMajoitus_VOSRahoitteisia" -> Column("opiskelijoidenMaara_SisaoppilaitosmainenMajoitus_VOSRahoitteisia"),
    "nuortenOppimaaranSuorittajia" -> Column("nuortenOppimaaranSuorittajia"),
    "nuortenOppimaaranSuorittajia_VOSRahoitteisia" -> Column("nuortenOppimaaranSuorittajia_VOSRahoitteisia"),
    "nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> Column("nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu"),
    "nuortenOppimaaranSuorittajia_EiKotikuntaa" -> Column("nuortenOppimaaranSuorittajia_EiKotikuntaa"),
    "nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> Column("nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa"),
    "aikuistenOppimaaranSuorittajia" -> Column("aikuistenOppimaaranSuorittajia"),
    "aikuistenOppimaaranSuorittajia_VOSRahoitteisia" -> Column("aikuistenOppimaaranSuorittajia_VOSRahoitteisia"),
    "aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> Column("aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu"),
    "aikuistenOppimaaranSuorittajia_EiKotikuntaa" -> Column("aikuistenOppimaaranSuorittajia_EiKotikuntaa"),
    "aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> Column("aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa")
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
