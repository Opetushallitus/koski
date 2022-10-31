package fi.oph.koski.raportit.lukio

import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.DB
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat
import fi.oph.koski.raportit._
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate
import scala.concurrent.duration.DurationInt


case class LukioDiaIbInternationalESHOpiskelijamaaratRaportti(db: DB) extends QueryMethods {
  def build(oppilaitosOids: List[String], päivä: LocalDate, t: LocalizationReader): DataSheet = {
    DataSheet(
      title = t.get("raportti-excel-opiskelijamäärä-sheet-name"),
      rows = runDbSync(
        query(oppilaitosOids, päivä, t.language).as[LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow],
        timeout = 5.minutes
      ),
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOids: Seq[String], päivä: LocalDate, lang: String)  = {
    val organisaatioNimiSarake = if(lang == "sv") "nimi_sv" else "nimi"
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
                                          when suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondaryupper' and koulutusmoduuli_koodiarvo = 'S7' then 8
                                          when suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondaryupper' and koulutusmoduuli_koodiarvo = 'S6' then 9
                                          when suorituskieli_koodiarvo != null then 10
                                          else 11
                                     end
                           )) [1] paatason_suoritus_id
                         from r_paatason_suoritus
                         where r_paatason_suoritus.suorituksen_tyyppi in (
                           'lukionoppiaineenoppimaara',
                           'lukionaineopinnot',
                           'lukionoppimaara',
                           'ibtutkinto',
                           'preiboppimaara',
                           'diatutkintovaihe',
                           'diavalmistavavaihe',
                           'europeanschoolofhelsinkivuosiluokkasecondaryupper'
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
                  'internationalschool',
                  'europeanschoolofhelsinki'
                )
                and r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOids)
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
    count(case when lasna_paivia_yhteensa > 3 * 365 and opintojen_rahoitus = '1' then 1 end) yli_kolme_vuotta_valtionosuus_rahoitteinen,
    count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
    count(case when ulkomainen_vaihto_opiskelija then 1 end) ulkomainen_vaihto_opiskelija,
    count(case when suorituskieli_koodiarvo = 'FI' then 1 end) opetuskieli_suomi,
    count(case when suorituskieli_koodiarvo = 'SV' then 1 end) opetuskieli_ruotsi,
    count(case when suorituskieli_koodiarvo not in ('FI', 'SV') then 1 end) opetuskieli_muu,
    count(case when sisaoppilaitosmainen_majoitus and opintojen_rahoitus = '1' then 1 end) sisaoppilaitosmainen_majoitus_valtionosuus_rahoitteinen,
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
    'internationalschooldiplomavuosiluokka',
    'internationalschoolmypvuosiluokka',
    'ibtutkinto',
    'preiboppimaara',
    'diatutkintovaihe',
    'diavalmistavavaihe',
    'europeanschoolofhelsinkivuosiluokkasecondaryupper'
  )
  group by oppilaitos_oid
), nuorten_oppimaara as (
  select
    oppilaitos_oid,
    count(*) yhteensa,
    count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
    count(case when lasna_paivia_yhteensa > 3 * 365 and opintojen_rahoitus = '1' then 1 end) yli_kolme_vuotta_valtionosuus_rahoitteinen,
    count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
    count(case when suorituskieli_koodiarvo = 'FI' then 1 end) opetuskieli_suomi,
    count(case when suorituskieli_koodiarvo = 'SV' then 1 end) opetuskieli_ruotsi,
    count(case when suorituskieli_koodiarvo not in ('FI', 'SV') then 1 end) opetuskieli_muu,
    count(case when kotikunta isnull then 1 end) ei_kotikuntaa,
    count(case when kotikunta = any($ahvenanmaanKunnat) then 1 end) kotikunta_ahvenanmaa
  from oppija
  where (oppimaara_koodiarvo = 'nuortenops' and suorituksen_tyyppi = 'lukionoppimaara')
    or suorituksen_tyyppi in (
    'internationalschooldiplomavuosiluokka',
    'internationalschoolmypvuosiluokka',
    'ibtutkinto',
    'preiboppimaara',
    'diatutkintovaihe',
    'diavalmistavavaihe',
    'europeanschoolofhelsinkivuosiluokkasecondaryupper'
  )
  group by oppilaitos_oid
), aikuisten_oppimaara as (
    select
      oppilaitos_oid,
      count(*) yhteensa,
      count(case when opintojen_rahoitus = '1' then 1 end) valtionosuus_rahoitteinen,
      count(case when lasna_paivia_yhteensa > 3 * 365 and opintojen_rahoitus = '1' then 1 end) yli_kolme_vuotta_valtionosuus_rahoitteinen,
      count(case when opintojen_rahoitus = '6' then 1 end) muuta_kautta_rahoitettu,
      count(case when suorituskieli_koodiarvo = 'FI' then 1 end) opetuskieli_suomi,
      count(case when suorituskieli_koodiarvo = 'SV' then 1 end) opetuskieli_ruotsi,
      count(case when suorituskieli_koodiarvo not in ('FI', 'SV') then 1 end) opetuskieli_muu,
      count(case when kotikunta isnull then 1 end) ei_kotikuntaa,
      count(case when kotikunta = any($ahvenanmaanKunnat) then 1 end) kotikunta_ahvenanmaa
    from oppija
    where (oppimaara_koodiarvo = 'aikuistenops' and suorituksen_tyyppi = 'lukionoppimaara')
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
      'lukionaineopinnot'
    )
  group by oppilaitos_oid
) select
    r_organisaatio.organisaatio_oid oppilaitos_oid,
    r_organisaatio.#$organisaatioNimiSarake oppilaitos_nimi,
    kaikki.yhteensa kaikki_yhteensa,
    kaikki.valtionosuus_rahoitteinen kaikki_valtionosuus_rahoitteinen,
    kaikki.muuta_kautta_rahoitettu kaikki_muuta_kautta_rahoitettu,
    kaikki.ulkomainen_vaihto_opiskelija kaikki_ulkomainen_vaihto_opiskelija,

    oppimaara.yhteensa oppimaara_yhteensa,
    oppimaara.valtionosuus_rahoitteinen oppimaara_valtionosuus_rahoitteinen,
    oppimaara.yli_kolme_vuotta_valtionosuus_rahoitteinen oppimaara_yli_kolme_vuotta_valtionosuus_rahoitteinen,
    oppimaara.muuta_kautta_rahoitettu oppimaara_muuta_kautta_rahoitettu,
    oppimaara.ulkomainen_vaihto_opiskelija oppimaara_ulkomainen_vaihto_opiskelija,
    oppimaara.opetuskieli_suomi oppimaara_opetuskieli_suomi,
    oppimaara.opetuskieli_ruotsi oppimaara_opetuskieli_ruotsi,
    oppimaara.opetuskieli_muu oppimaara_opetuskieli_muu,
    oppimaara.sisaoppilaitosmainen_majoitus_valtionosuus_rahoitteinen oppimaara_sisaoppilaitosmainen_majoitus_vos_rahoitteinen,

    nuorten_oppimaara.yhteensa nuorten_oppimaara_yhteensa,
    nuorten_oppimaara.valtionosuus_rahoitteinen nuorten_oppimaara_valtionosuus_rahoitteinen,
    nuorten_oppimaara.yli_kolme_vuotta_valtionosuus_rahoitteinen nuorten_oppimaara_yli_kolme_vuotta_valtionosuus_rahoitteinen,
    nuorten_oppimaara.muuta_kautta_rahoitettu nuorten_oppimaara_muuta_kautta_rahoitettu,
    nuorten_oppimaara.opetuskieli_suomi nuorten_oppimaara_opetuskieli_suomi,
    nuorten_oppimaara.opetuskieli_ruotsi nuorten_oppimaara_opetuskieli_ruotsi,
    nuorten_oppimaara.opetuskieli_muu nuorten_oppimaara_opetuskieli_muu,
    nuorten_oppimaara.kotikunta_ahvenanmaa nuorten_oppimaara_kotikunta_ahvenanmaa,
    nuorten_oppimaara.ei_kotikuntaa nuorten_oppimaara_ei_kotikuntaa,


    aikuisten_oppimaara.yhteensa aikuisten_oppimaara_yhteensa,
    aikuisten_oppimaara.valtionosuus_rahoitteinen aikuisten_oppimaara_valtionosuus_rahoitteinen,
    aikuisten_oppimaara.yli_kolme_vuotta_valtionosuus_rahoitteinen aikuisten_oppimaara_yli_kolme_vuotta_valtionosuus_rahoitteinen,
    aikuisten_oppimaara.muuta_kautta_rahoitettu aikuisten_oppimaara_muuta_kautta_rahoitettu,
    aikuisten_oppimaara.opetuskieli_suomi aikuisten_oppimaara_opetuskieli_suomi,
    aikuisten_oppimaara.opetuskieli_ruotsi aikuisten_oppimaara_opetuskieli_ruotsi,
    aikuisten_oppimaara.opetuskieli_muu aikuisten_oppimaara_opetuskieli_muu,
    aikuisten_oppimaara.kotikunta_ahvenanmaa aikuisten_oppimaara_kotikunta_ahvenanmaa,
    aikuisten_oppimaara.ei_kotikuntaa aikuisten_oppimaara_ei_kotikuntaa,

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
    erityinen_koulutustehtava_211,

    aineopiskelija.yhteensa aineopiskelija_yhteensa,
    aineopiskelija.valtionosuus_rahoitteinen aineopiskelija_valtionosuus_rahoitteinen,
    aineopiskelija.muuta_kautta_rahoitettu aineopiskelija_muuta_kautta_rahoitettu,
    aineopiskelija.ulkomainen_vaihto_opiskelija aineopiskelija_ulkomainen_vaihto_opiskelija
  from kaikki
  join r_organisaatio on kaikki.oppilaitos_oid = r_organisaatio.organisaatio_oid
  full join oppimaara on kaikki.oppilaitos_oid = oppimaara.oppilaitos_oid
  full join nuorten_oppimaara on kaikki.oppilaitos_oid = nuorten_oppimaara.oppilaitos_oid
  full join aikuisten_oppimaara on kaikki.oppilaitos_oid = aikuisten_oppimaara.oppilaitos_oid
  full join aineopiskelija on kaikki.oppilaitos_oid = aineopiskelija.oppilaitos_oid
    """
 }

  implicit private val getResult: GetResult[LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitosNimi = rs.getString("oppilaitos_nimi"),
      opiskelijoidenMaara = rs.getInt("kaikki_yhteensa"),
      opiskelijoidenMaara_VOSRahoitteisia = rs.getInt("kaikki_valtionosuus_rahoitteinen"),
      opiskelijoidenMaara_MuutaKauttaRahoitettu = rs.getInt("kaikki_muuta_kautta_rahoitettu"),
      opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita = rs.getInt("kaikki_ulkomainen_vaihto_opiskelija"),

      oppimaaranSuorittajia = rs.getInt("oppimaara_yhteensa"),
      oppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("oppimaara_valtionosuus_rahoitteinen"),
      oppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia = rs.getInt("oppimaara_yli_kolme_vuotta_valtionosuus_rahoitteinen"),
      oppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("oppimaara_muuta_kautta_rahoitettu"),
      oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita = rs.getInt("oppimaara_ulkomainen_vaihto_opiskelija"),
      oppimaaranSuorittajia_OpetuskieliSuomi = rs.getInt("oppimaara_opetuskieli_suomi"),
      oppimaaranSuorittajia_OpetuskieliRuotsi = rs.getInt("oppimaara_opetuskieli_ruotsi"),
      oppimaaranSuorittajia_OpetuskieliMuu = rs.getInt("oppimaara_opetuskieli_muu"),
      oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus_VOSRahoitteisia = rs.getInt("oppimaara_sisaoppilaitosmainen_majoitus_vos_rahoitteinen"),

      nuortenOppimaaranSuorittajia = rs.getInt("nuorten_oppimaara_yhteensa"),
      nuortenOppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("nuorten_oppimaara_valtionosuus_rahoitteinen"),
      nuortenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia = rs.getInt("nuorten_oppimaara_yli_kolme_vuotta_valtionosuus_rahoitteinen"),
      nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("nuorten_oppimaara_muuta_kautta_rahoitettu"),
      nuortenOppimaaranSuorittajia_OpetuskieliSuomi = rs.getInt("nuorten_oppimaara_opetuskieli_suomi"),
      nuortenOppimaaranSuorittajia_OpetuskieliRuotsi = rs.getInt("nuorten_oppimaara_opetuskieli_ruotsi"),
      nuortenOppimaaranSuorittajia_OpetuskieliMuu = rs.getInt("nuorten_oppimaara_opetuskieli_muu"),
      nuortenOppimaaranSuorittajia_EiKotikuntaa = rs.getInt("nuorten_oppimaara_ei_kotikuntaa"),
      nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa = rs.getInt("nuorten_oppimaara_kotikunta_ahvenanmaa"),

      aikuistenOppimaaranSuorittajia = rs.getInt("aikuisten_oppimaara_yhteensa"),
      aikuistenOppimaaranSuorittajia_VOSRahoitteisia = rs.getInt("aikuisten_oppimaara_valtionosuus_rahoitteinen"),
      aikuistenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia = rs.getInt("aikuisten_oppimaara_yli_kolme_vuotta_valtionosuus_rahoitteinen"),
      aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu = rs.getInt("aikuisten_oppimaara_muuta_kautta_rahoitettu"),
      aikuistenOppimaaranSuorittajia_OpetuskieliSuomi = rs.getInt("aikuisten_oppimaara_opetuskieli_suomi"),
      aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi = rs.getInt("aikuisten_oppimaara_opetuskieli_ruotsi"),
      aikuistenOppimaaranSuorittajia_OpetuskieliMuu = rs.getInt("aikuisten_oppimaara_opetuskieli_muu"),
      aikuistenOppimaaranSuorittajia_EiKotikuntaa = rs.getInt("aikuisten_oppimaara_ei_kotikuntaa"),
      aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa = rs.getInt("aikuisten_oppimaara_kotikunta_ahvenanmaa"),

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
      oppimaaranSuorittajia_ErityinenKoulutustehtava_211 = rs.getInt("erityinen_koulutustehtava_211"),

      aineopiskelija = rs.getInt("aineopiskelija_yhteensa"),
      aineopiskelija_VOSRahoitteisia = rs.getInt("aineopiskelija_valtionosuus_rahoitteinen"),
      aineopiskelija_MuutaKauttaRahoitettu = rs.getInt("aineopiskelija_muuta_kautta_rahoitettu"),
      aineopiskeija_UlkomaisiaVaihtoOpiskelijoita = rs.getInt("aineopiskelija_ulkomainen_vaihto_opiskelija")
    )}
  )

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Columns.flattenGroupingColumns(Seq(
    "oppilaitosOid" -> Column(t.get("raportti-excel-kolumni-oppilaitosOid")),
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    t.get("raportti-excel-kolumni-opiskelijatYhteensä") -> GroupColumnsWithTitle(List(
      "opiskelijoidenMaara" -> CompactColumn(t.get("raportti-excel-kolumni-opiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-opiskelijoita-lukio-comment"))),
      "opiskelijoidenMaara_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-oppilaidenMääräVOS"), comment = Some(t.get("raportti-excel-kolumni-oppilaidenMääräVOS-comment"))),
      "opiskelijoidenMaara_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-oppilaidenMääräMuuKuinVOS"), comment = Some(t.get("raportti-excel-kolumni-oppilaidenMääräMuuKuinVOS-comment"))),
      "opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita" -> CompactColumn(t.get("raportti-excel-kolumni-opiskelijoidenMaaraUlkomaisiaVaihtoOpiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-opiskelijoidenMaaraUlkomaisiaVaihtoOpiskelijoita-comment")))
    )),
    t.get("raportti-excel-kolumni-lukionSuorittajia") -> GroupColumnsWithTitle(List(
      "oppimaaranSuorittajia" -> CompactColumn(t.get("raportti-excel-kolumni-lukionSuorittajia"), comment = Some(t.get("raportti-excel-kolumni-lukionSuorittajia-comment"))),
      "oppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaVOSRahoitteisia-comment"))),
      "oppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaYliKolmeVuottaVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaYliKolmeVuottaVOSRahoitteisia-comment"))),
      "oppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaMuutaKauttaRahoitettu"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaMuutaKauttaRahoitettu-comment"))),
      "oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaUlkomaisiaVaihtoOpiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaUlkomaisiaVaihtoOpiskelijoita-comment"))),
      "oppimaaranSuorittajia_OpetuskieliSuomi" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaOpetuskieliSuomi"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaOpetuskieliSuomi-comment"))),
      "oppimaaranSuorittajia_OpetuskieliRuotsi" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaOpetuskieliRuotsi"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaOpetuskieliRuotsi-comment"))),
      "oppimaaranSuorittajia_OpetuskieliMuu" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaOpetuskieliMuu"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaOpetuskieliMuu-comment"))),
      "oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaSisaoppilaitosmainenMajoitusVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-oppimaaranSuorittajiaSisaoppilaitosmainenMajoitusVOSRahoitteisia-comment")))
    )),
    t.get("raportti-excel-kolumni-lukioNuortenOps") -> GroupColumnsWithTitle(List(
      "nuortenOppimaaranSuorittajia" -> CompactColumn(t.get("raportti-excel-kolumni-lukioNuortenOps"), comment = Some(t.get("raportti-excel-kolumni-lukioNuortenOps-comment"))),
      "nuortenOppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaVOSRahoitteisia-comment"))),
      "nuortenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaYliKolmeVuottaVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaYliKolmeVuottaVOSRahoitteisia-comment"))),
      "nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaMuutaKauttaRahoitettu"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaMuutaKauttaRahoitettu-comment"))),
      "nuortenOppimaaranSuorittajia_OpetuskieliSuomi" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaOpetuskieliSuomi"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaOpetuskieliSuomi-comment"))),
      "nuortenOppimaaranSuorittajia_OpetuskieliRuotsi" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaOpetuskieliRuotsi"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaOpetuskieliRuotsi-comment"))),
      "nuortenOppimaaranSuorittajia_OpetuskieliMuu" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaOpetuskieliMuu"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaOpetuskieliMuu-comment"))),
      "nuortenOppimaaranSuorittajia_EiKotikuntaa" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaEiKotikuntaa"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaEiKotikuntaa-comment"))),
      "nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> CompactColumn(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaKotikuntaAhvenanmaa"), comment = Some(t.get("raportti-excel-kolumni-nuortenOppimaaranSuorittajiaKotikuntaAhvenanmaa-comment")))
    )),
    t.get("raportti-excel-kolumni-lukioAikuistenOps") -> GroupColumnsWithTitle(List(
      "aikuistenOppimaaranSuorittajia" -> CompactColumn(t.get("raportti-excel-kolumni-lukioAikuistenOps"), comment = Some(t.get("raportti-excel-kolumni-lukioAikuistenOps-comment"))),
      "aikuistenOppimaaranSuorittajia_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaVOSRahoitteisia-comment"))),
      "aikuistenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaYliKolmeVuottaVOSRahoitteisia"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaYliKolmeVuottaVOSRahoitteisia-comment"))),
      "aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaMuutaKauttaRahoitettu"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaMuutaKauttaRahoitettu-comment"))),
      "aikuistenOppimaaranSuorittajia_OpetuskieliSuomi" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaOpetuskieliSuomi"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaOpetuskieliSuomi-comment"))),
      "aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaOpetuskieliRuotsi"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaOpetuskieliRuotsi-comment"))),
      "aikuistenOppimaaranSuorittajia_OpetuskieliMuu" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaOpetuskieliMuu"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaOpetuskieliMuu-comment"))),
      "aikuistenOppimaaranSuorittajia_EiKotikuntaa" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaEiKotikuntaa"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaEiKotikuntaa-comment"))),
      "aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa" -> CompactColumn(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaKotikuntaAhvenanmaa"), comment = Some(t.get("raportti-excel-kolumni-aikuistenOppimaaranSuorittajiaKotikuntaAhvenanmaa-comment")))
    )),
    t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtävä") -> GroupColumnsWithTitle(List(
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_101" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväIB"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväIB-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_102" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväIlmaisutaito"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväIlmaisutaito-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_103" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväKielet"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväKielet-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_104" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväKuvataide"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväKuvataide-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_105" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväMusiikki"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväMusiikki-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_106" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväLiikunta"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväLiikunta-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_107" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväMatematiikka"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväMatematiikka-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_108" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväYrittäjyys"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväYrittäjyys-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_109" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväSteiner"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväSteiner"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_208" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväLuma"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväLuma-comment"))),
      "oppimaaranSuorittajia_ErityinenKoulutustehtava_211" -> CompactColumn(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväYmpäristö"), comment = Some(t.get("raportti-excel-kolumni-lukioErityinenKoulutustehtäväYmpäristö-comment")))
    )),
    t.get("raportti-excel-kolumni-aineopiskelijat") -> GroupColumnsWithTitle(List(
      "aineopiskelija" -> CompactColumn(t.get("raportti-excel-kolumni-aineopiskelija"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelija-comment"))),
      "aineopiskelija_VOSRahoitteisia" -> CompactColumn(t.get("raportti-excel-kolumni-aineopiskelijoitaVOS"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelijoitaVOS-lukio-comment"))),
      "aineopiskelija_MuutaKauttaRahoitettu" -> CompactColumn(t.get("raportti-excel-kolumni-aineopiskelijoitaMuuKuinVOS"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelijoitaMuuKuinVOS-lukio-comment"))),
      "aineopiskeija_UlkomaisiaVaihtoOpiskelijoita" -> CompactColumn(t.get("raportti-excel-kolumni-aineopiskelijaUlkomaisiaVaihtoOpiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelijaUlkomaisiaVaihtoOpiskelijoita-comment")))
    ))
  ))
}

case class LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow(
  oppilaitosOid: String,
  oppilaitosNimi: String,
  opiskelijoidenMaara: Int,
  opiskelijoidenMaara_VOSRahoitteisia: Int,
  opiskelijoidenMaara_MuutaKauttaRahoitettu: Int,
  opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita: Int,
  oppimaaranSuorittajia: Int,
  oppimaaranSuorittajia_VOSRahoitteisia: Int,
  oppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia: Int,
  oppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita: Int,
  oppimaaranSuorittajia_OpetuskieliSuomi: Int,
  oppimaaranSuorittajia_OpetuskieliRuotsi: Int,
  oppimaaranSuorittajia_OpetuskieliMuu: Int,
  oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus_VOSRahoitteisia: Int,
  nuortenOppimaaranSuorittajia: Int,
  nuortenOppimaaranSuorittajia_VOSRahoitteisia: Int,
  nuortenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia: Int,
  nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  nuortenOppimaaranSuorittajia_OpetuskieliSuomi: Int,
  nuortenOppimaaranSuorittajia_OpetuskieliRuotsi: Int,
  nuortenOppimaaranSuorittajia_OpetuskieliMuu: Int,
  nuortenOppimaaranSuorittajia_EiKotikuntaa: Int,
  nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa: Int,
  aikuistenOppimaaranSuorittajia: Int,
  aikuistenOppimaaranSuorittajia_VOSRahoitteisia: Int,
  aikuistenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia: Int,
  aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu: Int,
  aikuistenOppimaaranSuorittajia_OpetuskieliSuomi: Int,
  aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi: Int,
  aikuistenOppimaaranSuorittajia_OpetuskieliMuu: Int,
  aikuistenOppimaaranSuorittajia_EiKotikuntaa: Int,
  aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa: Int,
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
  oppimaaranSuorittajia_ErityinenKoulutustehtava_211: Int,
  aineopiskelija: Int,
  aineopiskelija_VOSRahoitteisia: Int,
  aineopiskelija_MuutaKauttaRahoitettu: Int,
  aineopiskeija_UlkomaisiaVaihtoOpiskelijoita: Int
)
