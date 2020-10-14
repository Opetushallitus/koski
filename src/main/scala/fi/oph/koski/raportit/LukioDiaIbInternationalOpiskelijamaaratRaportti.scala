package fi.oph.koski.raportit

import java.sql.{Date, ResultSet}
import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import slick.jdbc.GetResult
import fi.oph.koski.util.SQL
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat

import scala.concurrent.duration._


case class LukioDiaIbInternationalOpiskelijamaaratRaportti(db: DB) extends KoskiDatabaseMethods {
  def build(oppilaitosOids: List[String], päivä: LocalDate): DataSheet = {
    DataSheet(
      title = "opiskelijamäärät",
      rows = runDbSync(query(oppilaitosOids, SQL.toSqlDate(päivä)).as[LukioDiaIbInternationalOpiskelijaMaaratRaporttiRow], timeout = 5.minutes),
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOids: List[String], päivä: Date)  = {
   sql"""
with oppija as (select
                  r_opiskeluoikeus.opiskeluoikeus_oid,
                  r_opiskeluoikeus.oppilaitos_oid,
                  r_opiskeluoikeus_aikajakso.opintojen_rahoitus,
                  r_opiskeluoikeus_aikajakso.ulkomainen_vaihto_opiskelija,
                  r_opiskeluoikeus_aikajakso.sisaoppilaitosmainen_majoitus,
                  r_opiskeluoikeus_aikajakso.erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo,
                  lasna_paivia.lasna_paivia_yhteensa,
                  r_henkilo.kotikunta,
                  r_paatason_suoritus.paatason_suoritus_id,
                  r_paatason_suoritus.oppimaara_koodiarvo,
                  r_paatason_suoritus.suorituksen_tyyppi,
                  r_paatason_suoritus.suorituskieli_koodiarvo
                from r_opiskeluoikeus
                  join r_opiskeluoikeus_aikajakso on (r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid)
                  join (
                      select
                        opiskeluoikeus_oid,
                        sum(case when loppu > now()::date then now()::date - alku
                                 else loppu - alku
                              end
                        ) lasna_paivia_yhteensa
                      from r_opiskeluoikeus_aikajakso
                      group by opiskeluoikeus_oid
                  ) as lasna_paivia on r_opiskeluoikeus.opiskeluoikeus_oid = lasna_paivia.opiskeluoikeus_oid
                  join r_henkilo on (r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid)
                  join (
                         select
                           opiskeluoikeus_oid,
                           (array_agg(paatason_suoritus_id
                            order by case when suorituksen_tyyppi = 'internationalschooldiplomavuosiluokka' and koulutusmoduuli_koodiarvo = '12' then 1
                                          when suorituksen_tyyppi = 'internationalschooldiplomavuosiluokka' and koulutusmoduuli_koodiarvo = '11' then 2
                                          when suorituksen_tyyppi = 'internationalschoolmypvuosiluokka' and koulutusmoduuli_koodiarvo = '10' then 3
                                          when suorituksen_tyyppi = 'diatutkintovaihe' then  4
                                          when suorituksen_tyyppi = 'diavalmistavavaihe' then  5
                                          when suorituksen_tyyppi = 'ibtutkinto' then 6
                                          when suorituksen_tyyppi = 'preiboppimaara' then 7
                                          when suorituskieli_koodiarvo != null then 8
                                          else 9
                                     end
                           )) [1] paatason_suoritus_id
                         from r_paatason_suoritus
                         where r_paatason_suoritus.suorituksen_tyyppi in (
                           'lukionoppiaineenoppimaara',
                           'lukionoppiaineidenoppimaarat2019',
                           'lukionoppimaara',
                           'lukionoppimaara2019',
                           'ibtutkinto',
                           'preiboppimaara',
                           'diatutkintovaihe',
                           'diavalmistavavaihe'
                         ) or (
                                 r_paatason_suoritus.suorituksen_tyyppi in (
                                   'internationalschooldiplomavuosiluokka',
                                   'internationalschoolmypvuosiluokka'
                                 ) and r_paatason_suoritus.koulutusmoduuli_koodiarvo in ('10', '11', '12')
                               )
                         group by opiskeluoikeus_oid
                  ) as paatason_suoritus on (r_opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid)
                  join r_paatason_suoritus on (paatason_suoritus.paatason_suoritus_id = r_paatason_suoritus.paatason_suoritus_id)
                where r_opiskeluoikeus.koulutusmuoto in (
                  'lukiokoulutus',
                  'ibtutkinto',
                  'diatutkinto',
                  'internationalschool'
                )
                and r_opiskeluoikeus.oppilaitos_oid in (#${SQL.toSqlListUnsafe(oppilaitosOids)})
                and r_opiskeluoikeus_aikajakso.tila = 'lasna'
                and r_opiskeluoikeus_aikajakso.alku <= $päivä
                and r_opiskeluoikeus_aikajakso.loppu >= $päivä
), kaikki as (
  select
    oppilaitos_oid,
    count(*) yhteensa,
    count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
    count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
    count(case when ulkomainen_vaihto_opiskelija then 1 end) ulkomainen_vaihto_opiskelija
  from oppija
  group by oppilaitos_oid
), oppimaara as (
  select
    oppilaitos_oid,
    count(*) yhteensa,
    count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
    count(case when lasna_paivia_yhteensa > 3 * 365 then 1 end) neljannen_vuoden_opiskelija,
    count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
    count(case when ulkomainen_vaihto_opiskelija then 1 end) ulkomainen_vaihto_opiskelija,
    count(case when suorituskieli_koodiarvo = 'FI' then 1 end) opetuskieli_suomi,
    count(case when suorituskieli_koodiarvo = 'SV' then 1 end) opetuskieli_ruotsi,
    count(case when suorituskieli_koodiarvo not in ('FI', 'SV') then 1 end) opetuskieli_muu,
    count(case when sisaoppilaitosmainen_majoitus then 1 end) sisaoppilaitosmainen_majoitus,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '101' then 1 end) erityinen_koulutustehtava_101,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '102' then 1 end) erityinen_koulutustehtava_102,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '103' then 1 end) erityinen_koulutustehtava_103,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '104' then 1 end) erityinen_koulutustehtava_104,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '105' then 1 end) erityinen_koulutustehtava_105,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '106' then 1 end) erityinen_koulutustehtava_106,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '107' then 1 end) erityinen_koulutustehtava_107,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '108' then 1 end) erityinen_koulutustehtava_108,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '109' then 1 end) erityinen_koulutustehtava_109,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '208' then 1 end) erityinen_koulutustehtava_208,
    count(case when erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo = '211' then 1 end) erityinen_koulutustehtava_211
  from oppija
  where suorituksen_tyyppi in (
    'lukionoppimaara',
    'lukionoppimaara2019',
    'internationalschooldiplomavuosiluokka',
    'internationalschoolmypvuosiluokka',
    'ibtutkinto',
    'preiboppimaara',
    'diatutkintovaihe',
    'diavalmistavavaihe'
  )
  group by oppilaitos_oid
), nuorten_oppimaara as (
  select
    oppilaitos_oid,
    count(*) yhteensa,
    count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
    count(case when lasna_paivia_yhteensa > 3 * 365 then 1 end) neljannen_vuoden_opiskelija,
    count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
    count(case when suorituskieli_koodiarvo = 'FI' then 1 end) opetuskieli_suomi,
    count(case when suorituskieli_koodiarvo = 'SV' then 1 end) opetuskieli_ruotsi,
    count(case when suorituskieli_koodiarvo not in ('FI', 'SV') then 1 end) opetuskieli_muu,
    count(case when kotikunta isnull then 1 end) ei_kotikuntaa,
    count(case when kotikunta in (#${SQL.toSqlListUnsafe(ahvenanmaanKunnat)})  then 1 end) kotikunta_ahvenanmaa
  from oppija
  where oppimaara_koodiarvo = 'nuortenops'
    or suorituksen_tyyppi in (
    'internationalschooldiplomavuosiluokka',
    'internationalschoolmypvuosiluokka',
    'ibtutkinto',
    'preiboppimaara',
    'diatutkintovaihe',
    'diavalmistavavaihe'
  )
  group by oppilaitos_oid
), aikuisten_oppimaara as (
    select
      oppilaitos_oid,
      count(*) yhteensa,
      count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
      count(case when lasna_paivia_yhteensa > 3 * 365 then 1 end) neljannen_vuoden_opiskelija,
      count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
      count(case when suorituskieli_koodiarvo = 'FI' then 1 end) opetuskieli_suomi,
      count(case when suorituskieli_koodiarvo = 'SV' then 1 end) opetuskieli_ruotsi,
      count(case when suorituskieli_koodiarvo not in ('FI', 'SV') then 1 end) opetuskieli_muu,
      count(case when kotikunta isnull then 1 end) ei_kotikuntaa,
      count(case when kotikunta in (#${SQL.toSqlListUnsafe(ahvenanmaanKunnat)})  then 1 end) kotikunta_ahvenanmaa
    from oppija
    where oppimaara_koodiarvo = 'aikuistenops'
    group by oppilaitos_oid
), aineopiskelija as (
    select
      oppilaitos_oid,
      count(*) yhteensa,
      count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
      count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
      count(case when ulkomainen_vaihto_opiskelija then 1 end) ulkomainen_vaihto_opiskelija
    from oppija where suorituksen_tyyppi in (
      'lukionoppiaineenoppimaara',
      'lukionoppiaineidenoppimaarat2019'
    )
  group by oppilaitos_oid
) select
    r_organisaatio.organisaatio_oid oppilaitos_oid,
    r_organisaatio.nimi oppilaitos_nimi,
    kaikki.yhteensa kaikki_yhteensa,
    kaikki.valtionosuus_rahoitteinen kaikki_valtionosuus_rahoitteinen,
    kaikki.muuta_kautta_rahoitettu kaikki_muuta_kautta_rahoitettu,
    kaikki.ulkomainen_vaihto_opiskelija kaikki_ulkomainen_vaihto_opiskelija,

    oppimaara.yhteensa oppimaara_yhteensa,
    oppimaara.valtionosuus_rahoitteinen oppimaara_valtionosuus_rahoitteinen,
    oppimaara.muuta_kautta_rahoitettu oppimaara_muuta_kautta_rahoitettu,
    oppimaara.neljannen_vuoden_opiskelija oppimaara_neljannen_vuoden_opiskelija,
    oppimaara.ulkomainen_vaihto_opiskelija oppimaara_ulkomainen_vaihto_opiskelija,
    oppimaara.opetuskieli_suomi oppimaara_opetuskieli_suomi,
    oppimaara.opetuskieli_ruotsi oppimaara_opetuskieli_ruotsi,
    oppimaara.opetuskieli_muu oppimaara_opetuskieli_muu,
    oppimaara.sisaoppilaitosmainen_majoitus oppimaara_sisaoppilaitosmainen_majoitus,

    nuorten_oppimaara.yhteensa nuorten_oppimaara_yhteensa,
    nuorten_oppimaara.valtionosuus_rahoitteinen nuorten_oppimaara_valtionosuus_rahoitteinen,
    nuorten_oppimaara.muuta_kautta_rahoitettu nuorten_oppimaara_muuta_kautta_rahoitettu,
    nuorten_oppimaara.neljannen_vuoden_opiskelija nuorten_oppimaara_neljannen_vuoden_opiskelija,
    nuorten_oppimaara.opetuskieli_suomi nuorten_oppimaara_opetuskieli_suomi,
    nuorten_oppimaara.opetuskieli_ruotsi nuorten_oppimaara_opetuskieli_ruotsi,
    nuorten_oppimaara.opetuskieli_muu nuorten_oppimaara_opetuskieli_muu,
    nuorten_oppimaara.kotikunta_ahvenanmaa nuorten_oppimaara_kotikunta_ahvenanmaa,
    nuorten_oppimaara.ei_kotikuntaa nuorten_oppimaara_ei_kotikuntaa,


    aikuisten_oppimaara.yhteensa aikuisten_oppimaara_yhteensa,
    aikuisten_oppimaara.valtionosuus_rahoitteinen aikuisten_oppimaara_valtionosuus_rahoitteinen,
    aikuisten_oppimaara.muuta_kautta_rahoitettu aikuisten_oppimaara_muuta_kautta_rahoitettu,
    aikuisten_oppimaara.neljannen_vuoden_opiskelija aikuisten_oppimaara_neljannen_vuoden_opiskelija,
    aikuisten_oppimaara.opetuskieli_suomi aikuisten_oppimaara_opetuskieli_suomi,
    aikuisten_oppimaara.opetuskieli_ruotsi aikuisten_oppimaara_opetuskieli_ruotsi,
    aikuisten_oppimaara.opetuskieli_muu aikuisten_oppimaara_opetuskieli_muu,
    aikuisten_oppimaara.kotikunta_ahvenanmaa aikuisten_oppimaara_kotikunta_ahvenanmaa,
    aikuisten_oppimaara.ei_kotikuntaa aikuisten_oppimaara_ei_kotikuntaa,

    aineopiskelija.yhteensa aineopiskelija_yhteensa,
    aineopiskelija.valtionosuus_rahoitteinen aineopiskelija_valtionosuus_rahoitteinen,
    aineopiskelija.muuta_kautta_rahoitettu aineopiskelija_muuta_kautta_rahoitettu,
    aineopiskelija.ulkomainen_vaihto_opiskelija aineopiskelija_ulkomainen_vaihto_opiskelija,

    erityinen_koulutustehtava_101,
    erityinen_koulutustehtava_102,
    erityinen_koulutustehtava_103,
    erityinen_koulutustehtava_104,
    erityinen_koulutustehtava_105,
    erityinen_koulutustehtava_106,
    erityinen_koulutustehtava_107,
    erityinen_koulutustehtava_108,
    erityinen_koulutustehtava_109,
    erityinen_koulutustehtava_208,
    erityinen_koulutustehtava_211
  from kaikki
  join r_organisaatio on kaikki.oppilaitos_oid = r_organisaatio.organisaatio_oid
  full join oppimaara on kaikki.oppilaitos_oid = oppimaara.oppilaitos_oid
  full join nuorten_oppimaara on kaikki.oppilaitos_oid = nuorten_oppimaara.oppilaitos_oid
  full join aikuisten_oppimaara on kaikki.oppilaitos_oid = aikuisten_oppimaara.oppilaitos_oid
  full join aineopiskelija on kaikki.oppilaitos_oid = aineopiskelija.oppilaitos_oid
    """
 }

  implicit private val getResult: GetResult[LukioDiaIbInternationalOpiskelijaMaaratRaporttiRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioDiaIbInternationalOpiskelijaMaaratRaporttiRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitosNimi = rs.getString("oppilaitos_nimi"),
      opiskelijoidenMaara = rs.getInt("kaikki_yhteensa"),
      opiskelijoidenMaara_VOSRahoitteisia = rs.getInt("kaikki_valtionosuus_rahoitteinen"),
      opiskelijoidenMaara_MuutaKauttaRahoitettu = rs.getInt("kaikki_muuta_kautta_rahoitettu"),
      opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita = rs.getInt("kaikki_ulkomainen_vaihto_opiskelija"),

      oppimaaranSuorittajia = rs.getInt("oppimaara_yhteensa"),
      oppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("oppimaara_valtionosuus_rahoitteinen"),
      oppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("oppimaara_muuta_kautta_rahoitettu"),
      oppimaaranSuorittajia_NeljannenVuodenOpiskelijoita = rs.getInt("oppimaara_neljannen_vuoden_opiskelija"),
      oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita = rs.getInt("oppimaara_ulkomainen_vaihto_opiskelija"),
      oppimaaranSuorittajia_OpetuskieliSuomi = rs.getInt("oppimaara_opetuskieli_suomi"),
      oppimaaranSuorittajia_OpetuskieliRuotsi = rs.getInt("oppimaara_opetuskieli_ruotsi"),
      oppimaaranSuorittajia_OpetuskieliMuu = rs.getInt("oppimaara_opetuskieli_muu"),
      oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus = rs.getInt("oppimaara_sisaoppilaitosmainen_majoitus"),

      nuortenOppimaaranSuorittajia = rs.getInt("nuorten_oppimaara_yhteensa"),
      nuortenOppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("nuorten_oppimaara_valtionosuus_rahoitteinen"),
      nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("nuorten_oppimaara_muuta_kautta_rahoitettu"),
      nuortenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita = rs.getInt("nuorten_oppimaara_neljannen_vuoden_opiskelija"),
      nuortenOppimaaranSuorittajia_OpetuskieliSuomi = rs.getInt("nuorten_oppimaara_opetuskieli_suomi"),
      nuortenOppimaaranSuorittajia_OpetuskieliRuotsi = rs.getInt("nuorten_oppimaara_opetuskieli_ruotsi"),
      nuortenOppimaaranSuorittajia_OpetuskieliMuu = rs.getInt("nuorten_oppimaara_opetuskieli_muu"),
      nuortenOppimaaranSuorittajia_EiKotikuntaa = rs.getInt("nuorten_oppimaara_ei_kotikuntaa"),
      nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa = rs.getInt("nuorten_oppimaara_kotikunta_ahvenanmaa"),

      aikuistenOppimaaranSuorittajia = rs.getInt("aikuisten_oppimaara_yhteensa"),
      aikuistenOppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("aikuisten_oppimaara_valtionosuus_rahoitteinen"),
      aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("aikuisten_oppimaara_muuta_kautta_rahoitettu"),
      aikuistenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita = rs.getInt("aikuisten_oppimaara_neljannen_vuoden_opiskelija"),
      aikuistenOppimaaranSuorittajia_OpetuskieliSuomi = rs.getInt("aikuisten_oppimaara_opetuskieli_suomi"),
      aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi = rs.getInt("aikuisten_oppimaara_opetuskieli_ruotsi"),
      aikuistenOppimaaranSuorittajia_OpetuskieliMuu = rs.getInt("aikuisten_oppimaara_opetuskieli_muu"),
      aikuistenOppimaaranSuorittajia_EiKotikuntaa = rs.getInt("aikuisten_oppimaara_ei_kotikuntaa"),
      aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa = rs.getInt("aikuisten_oppimaara_kotikunta_ahvenanmaa"),

      aineopiskelija = rs.getInt("aineopiskelija_yhteensa"),
      aineopiskelija_VOSRahoitteisia = rs.getInt("aineopiskelija_valtionosuus_rahoitteinen"),
      aineopiskelija_MuutaKauttaRahoitettu = rs.getInt("aineopiskelija_muuta_kautta_rahoitettu"),
      aineopiskeija_UlkomaisiaVaihtoOpiskelijoita = rs.getInt("aineopiskelija_ulkomainen_vaihto_opiskelija"),

      oppimaaranSuorittajia_ErityinenKoulutustehtava_101 = rs.getInt("erityinen_koulutustehtava_101"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_102 = rs.getInt("erityinen_koulutustehtava_102"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_103 = rs.getInt("erityinen_koulutustehtava_103"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_104 = rs.getInt("erityinen_koulutustehtava_104"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_105 = rs.getInt("erityinen_koulutustehtava_105"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_106 = rs.getInt("erityinen_koulutustehtava_106"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_107 = rs.getInt("erityinen_koulutustehtava_107"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_108 = rs.getInt("erityinen_koulutustehtava_108"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_109 = rs.getInt("erityinen_koulutustehtava_109"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_208 = rs.getInt("erityinen_koulutustehtava_208"),
      oppimaaranSuorittajia_ErityinenKoulutustehtava_211 = rs.getInt("erityinen_koulutustehtava_211")
    )}
  )

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column("Oppilaitos oid"),
    "oppilaitosNimi" -> Column("Oppilaitos"),
    "opiskelijoidenMaara" -> CompactColumn("opiskelijoidenMaara"),
    "opiskelijoidenMaara_VOSRahoitteisia" -> CompactColumn("opiskelijoidenMaara_VOSRahoitteisia"),
    "opiskelijoidenMaara_MuutaKauttaRahoitettu" -> CompactColumn("opiskelijoidenMaara_MuutaKauttaRahoitettu"),
    "opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita" -> CompactColumn("opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita"),
    "oppimaaranSuorittajia" -> CompactColumn("oppimaaranSuorittajia"),
    "oppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn("oppimaaranSuorittajia_VOSRahoitteisia"),
    "oppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn("oppimaaranSuorittajia_MuutaKauttaRahoitettu"),
    "oppimaaranSuorittajia_NeljannenVuodenOpiskelijoita" -> CompactColumn("oppimaaranSuorittajia_NeljannenVuodenOpiskelijoita"),
    "oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita" -> CompactColumn("oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita"),
    "oppimaaranSuorittajia_OpetuskieliSuomi" -> CompactColumn("oppimaaranSuorittajia_OpetuskieliSuomi"),
    "oppimaaranSuorittajia_OpetuskieliRuotsi" -> CompactColumn("oppimaaranSuorittajia_OpetuskieliRuotsi"),
    "oppimaaranSuorittajia_OpetuskieliMuu" -> CompactColumn("oppimaaranSuorittajia_OpetuskieliMuu"),
    "oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus" -> CompactColumn("opiskelijoidenMaara_SisaoppilaitosmainenMajoitus"),
    "nuortenOppimaaranSuorittajia" -> CompactColumn("nuortenOppimaaranSuorittajia"),
    "nuortenOppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn("nuortenOppimaaranSuorittajia_VOSRahoitteisia"),
    "nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn("nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu"),
    "nuortenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita" -> CompactColumn("nuortenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita"),
    "nuortenOppimaaranSuorittajia_OpetuskieliSuomi" -> CompactColumn("nuortenOppimaaranSuorittajia_OpetuskieliSuomi"),
    "nuortenOppimaaranSuorittajia_OpetuskieliRuotsi" -> CompactColumn("nuortenOppimaaranSuorittajia_OpetuskieliRuotsi"),
    "nuortenOppimaaranSuorittajia_OpetuskieliMuu" -> CompactColumn("nuortenOppimaaranSuorittajia_OpetuskieliMuu"),
    "nuortenOppimaaranSuorittajia_EiKotikuntaa" -> CompactColumn("nuortenOppimaaranSuorittajia_EiKotikuntaa"),
    "nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> CompactColumn("nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa"),
    "aikuistenOppimaaranSuorittajia" -> CompactColumn("aikuistenOppimaaranSuorittajia"),
    "aikuistenOppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn("aikuistenOppimaaranSuorittajia_VOSRahoitteisia"),
    "aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn("aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu"),
    "aikuistenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita" -> CompactColumn("aikuistenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita"),
    "aikuistenOppimaaranSuorittajia_OpetuskieliSuomi" -> CompactColumn("aikuistenOppimaaranSuorittajia_OpetuskieliSuomi"),
    "aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi" -> CompactColumn("aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi"),
    "aikuistenOppimaaranSuorittajia_OpetuskieliMuu" -> CompactColumn("aikuistenOppimaaranSuorittajia_OpetuskieliMuu"),
    "aikuistenOppimaaranSuorittajia_EiKotikuntaa" -> CompactColumn("aikuistenOppimaaranSuorittajia_EiKotikuntaa"),
    "aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> CompactColumn("aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa"),
    "aineopiskelija" -> CompactColumn("aineopiskelija"),
    "aineopiskelija_VOSRahoitteisia" -> CompactColumn("aineopiskelija_VOSRahoitteisia"),
    "aineopiskelija_MuutaKauttaRahoitettu" -> CompactColumn("aineopiskelija_MuutaKauttaRahoitettu"),
    "aineopiskeija_UlkomaisiaVaihtoOpiskelijoita" -> CompactColumn("aineopiskeija_UlkomaisiaVaihtoOpiskelijoita"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_101" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_101"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_102" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_102"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_103" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_103"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_104" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_104"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_105" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_105"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_106" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_106"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_107" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_107"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_108" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_108"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_109" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_109"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_208" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_208"),
    "oppimaaranSuorittajia_ErityinenKoulutustehtava_211" -> CompactColumn("opiskelijoidenMaara_ErityinenKoulutustehtava_211")
  )
}

case class LukioDiaIbInternationalOpiskelijaMaaratRaporttiRow(
  oppilaitosOid: String,
  oppilaitosNimi: String,
  opiskelijoidenMaara: Int,
  opiskelijoidenMaara_VOSRahoitteisia: Int,
  opiskelijoidenMaara_MuutaKauttaRahoitettu: Int,
  opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita: Int,
  oppimaaranSuorittajia: Int,
  oppimaaranSuorittajia_VOSRahoitteisia: Int,
  oppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  oppimaaranSuorittajia_NeljannenVuodenOpiskelijoita: Int,
  oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita: Int,
  oppimaaranSuorittajia_OpetuskieliSuomi: Int,
  oppimaaranSuorittajia_OpetuskieliRuotsi: Int,
  oppimaaranSuorittajia_OpetuskieliMuu: Int,
  oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus: Int,
  nuortenOppimaaranSuorittajia: Int,
  nuortenOppimaaranSuorittajia_VOSRahoitteisia: Int,
  nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  nuortenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita: Int,
  nuortenOppimaaranSuorittajia_OpetuskieliSuomi: Int,
  nuortenOppimaaranSuorittajia_OpetuskieliRuotsi: Int,
  nuortenOppimaaranSuorittajia_OpetuskieliMuu: Int,
  nuortenOppimaaranSuorittajia_EiKotikuntaa: Int,
  nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa: Int,
  aikuistenOppimaaranSuorittajia: Int,
  aikuistenOppimaaranSuorittajia_VOSRahoitteisia: Int,
  aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  aikuistenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita: Int,
  aikuistenOppimaaranSuorittajia_OpetuskieliSuomi: Int,
  aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi: Int,
  aikuistenOppimaaranSuorittajia_OpetuskieliMuu: Int,
  aikuistenOppimaaranSuorittajia_EiKotikuntaa: Int,
  aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa: Int,
  aineopiskelija: Int,
  aineopiskelija_VOSRahoitteisia: Int,
  aineopiskelija_MuutaKauttaRahoitettu: Int,
  aineopiskeija_UlkomaisiaVaihtoOpiskelijoita: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_101: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_102: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_103: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_104: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_105: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_106: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_107: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_108: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_109: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_208: Int,
  oppimaaranSuorittajia_ErityinenKoulutustehtava_211: Int
)
